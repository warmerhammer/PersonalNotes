package com.warmerhammer.personalnotes.SearchActivity

import android.content.ContentResolver
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.warmerhammer.personalnotes.Data.DataClasses.User
import com.warmerhammer.personalnotes.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchActivityRepo @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    private val firestoreDB = Firebase.firestore
    private val usersTrie = UserTrie()
    private val externalScope: CoroutineScope = CoroutineScope(ioDispatcher)
    var initConstructTrie = true

    fun constructTrie() = flow {
        externalScope.launch {
            Log.i("SearchActivityRepo.kt", "constructTrie()")
            withContext(ioDispatcher) {
                firestoreDB.collection("users").get().addOnSuccessListener { documents ->
                    Log.i("SearchActivityRepo.kt", "documents :: $documents")
                    for (document in documents) {
                        Log.i("SearchActivityRepo.kt", "document.name :: ${document.data["name"]}")
                        val userName: String? = document.data["name"] as String?
                        userName?.apply { usersTrie.insert(this) }
                    }

                }.await().apply {
                    emit(true)
                }
            }
        }
    }

    fun searchUser(query: String, contentResolver: ContentResolver): Boolean {
        return usersTrie.search(query)
    }


    fun searchFriends(queryString: String): Flow<List<User>> = callbackFlow {
        var usersRef: CollectionReference? = null
        try {
            usersRef = firestoreDB.collection("users")
        } catch (e: Throwable) {
            Log.e(TAG, "Error with Firebase connection searching users)")
        }

        val subscription =
            usersRef?.orderBy("lowercaseName")?.startAt(queryString)?.endAt("$queryString~")
                ?.addSnapshotListener { snapshot, _ ->
                    if (snapshot == null) {
                        Log.w(TAG, "Firebase :: Friend search snapshot is null")
                        return@addSnapshotListener
                    }
                    val users = mutableListOf<User>()
                    try {
                        for (document in snapshot) {
                            users.add(document.toObject())
                        }
                        Log.i(TAG, "users :: $users")
                        trySend(users)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error retrieving notes from Firebase.")
                    }
                }

        awaitClose { subscription?.remove() }
    }

    companion object {
        val TAG = "${SearchActivityRepo::class.java.simpleName}.kt"
    }
}