package com.warmerhammer.personalnotes.Data;

import android.util.Log
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.Data.DataClasses.User
import com.warmerhammer.personalnotes.IoDispatcher
import com.warmerhammer.personalnotes.MainActivity.toDoListConverter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepo @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher,
) {

    // firestoreDB
    private val firestoreDB = Firebase.firestore
    private var fbUser: User? = null
    private val usersRef = firestoreDB.collection("users")
    private val externalScope: CoroutineScope = CoroutineScope(dispatcherIO)
    private var homeDocsCache = mutableListOf<Project>()
    private var homePageProjectsRef: CollectionReference? = null
    private lateinit var projectRef: DocumentReference

    private fun addToCache(project: Project) {
        when (project.category) {
            "notes" -> {
                val note = project as Note
                externalScope.launch {
                    // todo
                }
            }

            "todoLists" -> {
                val todoList = project as TodoList
                // todo
            }
        }
    }

    fun setProjectRef(project_id: Int) = run {
        projectRef =
            usersRef
                .document(fbUser!!.id.toString())
                .collection("IndividualProjects")
                .document(project_id.toString())
    }

    fun getDocs(): Flow<List<Note>> = callbackFlow {

        var noteRef: CollectionReference? = null
        try {
            noteRef = projectRef.collection("notes")
        } catch (e: Throwable) {
            Log.e(TAG, "Error with Firebase connection (notes)")
        }

        val subscription = noteRef?.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                return@addSnapshotListener
            }
            val notes = mutableListOf<Note>()
            try {
                for (document in snapshot) {
                    notes.add(document.toObject<Note>())
                }
                trySend(notes)
            } catch (e: Throwable) {
                Log.e(TAG, "Error retrieving notes from Firebase.")
            }
        }

        awaitClose { subscription?.remove() }
    }

    fun getTodoLists(): Flow<List<TodoList>> = callbackFlow {

        var todoListRef: CollectionReference? = null
        try {
            todoListRef = projectRef.collection("todoLists")
        } catch (e: Throwable) {
            Log.e(TAG, "Error connecting to Firebase collection todoLists.")
        }

        val subscription = todoListRef?.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                return@addSnapshotListener
            }

            val todoLists = mutableListOf<TodoList>()
            for (doc in snapshot) {
                val todoList = toDoListConverter().convert(doc)
                todoLists.add(todoList)
            }
            trySend(todoLists)
        }

        awaitClose { subscription?.remove() }
    }

    fun saveNewProject(folder: Folder) {
        homePageProjectsRef?.document(folder.id.toString())?.set(folder)

        usersRef
            .document(fbUser!!.id.toString())
            .collection("IndividualProjects")
            .document(folder.id.toString())
            .set(folder, SetOptions.merge())
    }

    fun saveNote(note: Note) {
        // todo
    }

    fun getNote(noteFolder: String, id: String): Flow<Note?> = callbackFlow {

        Log.d(TAG, "homePageProjectsRef id :: ${homePageProjectsRef!!.id}")
        var noteRef: DocumentReference? = null

        // assign noteRef
        when (noteFolder) {
            "HomePage" -> {
                try {
                    noteRef = homePageProjectsRef!!.document(id)
                } catch (e: Throwable) {
                    Log.e(TAG, "(HomePageProjects) Error retrieving note id :: $id")
                }
            }

            else -> {
                try {
                    noteRef = projectRef.collection("notes").document(id)
                } catch (e: Throwable) {
                    Log.e(TAG, "(IndividualProjects) Error retrieving note id : $id")
                    close(e)
                }
            }
        }

        // assign subscription
        val subscription =
            noteRef?.addSnapshotListener { snapshot, _ ->
                if (snapshot == null) {
                    trySend(null)
                    return@addSnapshotListener
                }


                try {
                    Log.d(TAG, "note :: ${snapshot.toObject<Note>()}")
                    trySend(snapshot.toObject<Note>()!!)
                } catch (e: Throwable) {
                    Log.w(TAG, "getNote() > snaphot :: $snapshot returned a blank list of notes.")
                    trySend(null)
                }
            }

        awaitClose { subscription?.remove() }
    }

    fun saveList(list: TodoList): TodoList {

        // todo

        return list
    }

    suspend fun deleteProject(project: Project): Boolean {
        // todo
        return true
    }

    fun getHomePageDocs(): Flow<List<Project>> = channelFlow {
        while (homePageProjectsRef == null) {
            delay(500)
        }


        if (fbUser?.email != null) {
            usersRef.whereEqualTo("email", fbUser!!.email).get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot == null) {
                        return@addOnSuccessListener
                    }

                    if (snapshot.isEmpty) {
                        Log.d(TAG, "getHomePageDocs() snapshot.isEmpty")
                        return@addOnSuccessListener
                    }

                    for (doc in snapshot) {
                        val user = doc.toObject<User>()
                        val homePageReference =
                            usersRef.document(user.id.toString()).collection("HomePageProjects")
                        externalScope.launch {
                            homePageDocsQuery(homePageReference).collectLatest { projectList ->
                                send(projectList)
                            }
                        }

                    }
                }.await()


        } else {
            homePageDocsQuery(homePageProjectsRef!!).collectLatest { projectList ->
                send(projectList)
            }
        }

        awaitClose()
    }

    private fun homePageDocsQuery(homePageReference: CollectionReference) = callbackFlow {
        var noteLock = true
        val noteSubscription =
            homePageReference
                .whereEqualTo("category", "notes")
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, _ ->
                    Log.i(TAG, "getHomePageDocs() snapshot :: $snapshot")
                    if (snapshot == null) {
                        trySend(listOf())
                        return@addSnapshotListener
                    }

                    if (noteLock || snapshot.metadata.hasPendingWrites()) {
                        noteLock = false
                        try {
                            for (doc in snapshot) {
                                val note = doc.toObject<Note>()
                                val project: Project? =
                                    homeDocsCache.firstOrNull { project -> project.id == note.id }
                                if (project != null) {
                                    val idx = homeDocsCache.indexOf(project)
                                    homeDocsCache[idx] = note
                                } else {
                                    homeDocsCache.add(note)
                                }
                            }

                            trySend(homeDocsCache)
                        } catch (e: Throwable) {
                            Log.e(TAG, "Unable to retrieve HomePageProjects", e)
                        }
                    }
                }

        var todoLock = true
        val todoListSubscription =
            homePageReference
                .whereEqualTo("category", "todoLists")
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, _ ->
                    if (snapshot == null) {
                        trySend(listOf())
                        return@addSnapshotListener
                    }

                    if (todoLock || snapshot.metadata.hasPendingWrites()) {
                        todoLock = false
                        try {
                            for (doc in snapshot) {
                                val newTodoList = toDoListConverter().convert(doc)
                                Log.d(TAG, "newTodoList :: $newTodoList")
                                val currentList =
                                    homeDocsCache.firstOrNull { project -> project.id == newTodoList.id }
                                if (currentList != null) {
                                    val idx = homeDocsCache.indexOf(currentList)
                                    homeDocsCache[idx] = newTodoList
                                } else {
                                    homeDocsCache.add(newTodoList)
                                }
                            }
                            Log.d(TAG, "homeDocsCache :: $homeDocsCache")
                            trySend(homeDocsCache)

                        } catch (exception: Throwable) {
                            Log.w(
                                TAG,
                                "Unable to retrieve todoLists for user :: ${fbUser!!.id.toString()}",
                                exception
                            )
                        }
                    }
                }

        awaitClose {
            noteSubscription.remove()
            todoListSubscription.remove()
        }
    }

    fun getProject(projectId: Int): Flow<Folder> = callbackFlow {
        val subscription =
            homePageProjectsRef!!.document(projectId.toString())
                .addSnapshotListener { document, _ ->
                    if (document == null) {
                        return@addSnapshotListener
                    }
                    try {
                        trySend(document.toObject<Folder>()!!)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error retrieving document for Folder id :: $projectId")
                    }

                }

        awaitClose {
            subscription.remove()
        }
    }

    fun moveProject(
        projectId: String,
        category: String,
        ParentFolder: String,
        newFolder: String
    ) {
        when (ParentFolder) {
            "HomePage" -> {
                homePageProjectsRef!!
                    .document(projectId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document == null) {
                            return@addOnSuccessListener
                        }

                        externalScope.launch {
                            val movingProject =
                                if (category == "todoLists") toDoListConverter().convert(
                                    document
                                )
                                else document.toObject<Note>()

                            when (newFolder) {
                                "HomePage" -> {
                                    homePageProjectsRef!!
                                        .document(projectId)
                                        .set(movingProject!!)
                                }

                                else -> {
                                    usersRef
                                        .document(fbUser!!.id.toString())
                                        .collection("IndividualProjects")
                                        .document(newFolder)
                                        .collection(category)
                                        .document(projectId)
                                        .set(movingProject!!)
                                }
                            }

                            withContext(dispatcherIO) { deleteProject(movingProject) }

                        }

                    }
            }

            else -> {
                Log.i(TAG, "category :: $category, projectId :: $projectId")
                projectRef
                    .collection(category)
                    .document(projectId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document == null) {
                            return@addOnSuccessListener
                        }
                        externalScope.launch {
                            val movingProject =
                                if (category == "todoLists") toDoListConverter().convert(
                                    document
                                )
                                else document.toObject<Note>()

                            when (newFolder) {
                                "HomePage" -> {
                                    homePageProjectsRef!!
                                        .document(projectId)
                                        .set(movingProject!!)
                                }

                                else -> {
                                    usersRef
                                        .document(fbUser!!.id.toString())
                                        .collection("IndividualProjects")
                                        .document(newFolder)
                                        .collection(category)
                                        .document(projectId)
                                        .set(movingProject!!)
                                }
                            }

                            withContext(dispatcherIO) { deleteProject(movingProject) }
                        }
                    }
            }
        }
    }

    suspend fun setUser(user: User): User {
        if (fbUser == null || fbUser?.id.toString() != user.id.toString()) {
            fbUser = user
        } else {
            fbUser?.apply {
                email = user.email
                name = user.name
                photoUrl = user.photoUrl
                id = user.id
            }
        }

        homePageProjectsRef =
            usersRef.document(fbUser!!.id.toString()).collection("HomePageProjects")

        Log.d(TAG, "1. homePageProjectsRef id :: ${homePageProjectsRef!!.id}")
        Log.d(TAG, "setUser() user :: $user")

        usersRef
            .document(fbUser!!.id.toString())
            .get()
            .addOnSuccessListener {
                usersRef
                    .document(fbUser!!.id.toString())
                    .set(fbUser!!)
                    .addOnSuccessListener {
                        Log.i(
                            TAG,
                            "New user set with id: ${fbUser!!.id}"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            TAG,
                            "1. FirebaseRepo.kt setUser() Failed to connect to firestoreDB",
                            e
                        )
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "2. FirebaseRepo.kt setUser() Failed to connect to firestoreDB", e)
            }
            .await()

        return fbUser!!
    }

    companion object {
        private val TAG = FirebaseRepo::class.java.simpleName + ".kt"
    }
}
