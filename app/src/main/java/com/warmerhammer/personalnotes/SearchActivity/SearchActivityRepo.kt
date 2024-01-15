package com.warmerhammer.personalnotes.SearchActivity

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.warmerhammer.personalnotes.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
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

    fun searchUser(query: String): Boolean {
        return usersTrie.search(query)
    }


}