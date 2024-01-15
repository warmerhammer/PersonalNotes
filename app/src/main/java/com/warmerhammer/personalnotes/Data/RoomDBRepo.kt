package com.warmerhammer.personalnotes.Data

import android.util.Log
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItem
import com.warmerhammer.personalnotes.Data.DataClasses.User
import com.warmerhammer.personalnotes.IOScope
import com.warmerhammer.personalnotes.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomDatabaseRepo @Inject constructor(
    @RoomDB private val roomDB: AppDatabase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @IOScope private val ioScope: CoroutineScope,
) {

    private val noteDao = roomDB.noteDao()
    private val userDao = roomDB.userDao()
    private val folderDao = roomDB.folderDao()
    private val todoListDao = roomDB.todoListDao()
    private val todoListItemDao = roomDB.todoListItemDao()

    // Room database functions for User
//    suspend fun getUser(): Flow<User?> =
//        withContext(ioDispatcher) {
//            val user = userDao.getUser()
//            Log.i("RoomDBRepo.kt", "getUser() user :: $user")
//            user
//        }

    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    suspend fun saveUser(user: User) = withContext(ioDispatcher) {
        Log.d("RoomDBRepo.kt", "user :: $user")
        userDao.saveUser(user)
    }

    // Room database functions for Folder
    suspend fun saveFolder(folder: Folder) = withContext(ioDispatcher) {
        folderDao.insertFolder(folder)
    }

    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()
    fun getFolderById(id: Long): Flow<Folder> = folderDao.loadFolderById(id)
    fun getFolderByName(name: String): Flow<Folder> = folderDao.getFolderByName(name)

    // Room database functions for Note
    suspend fun saveNote(note: Note) = noteDao.insertNote(note)
    fun loadNoteById(id: Long): Flow<Note> = noteDao.loadNoteById(id)
    fun loadNotesByFolderId(folderId: Long): Flow<List<Note>> =
        noteDao.loadNotesByFolderId(folderId)

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAllNotes()
    suspend fun deleteNotes(note: Array<Note>): Int = withContext(ioDispatcher) {
        noteDao.deleteNotes(note)
    }

    // Room database functions for TodoList
    suspend fun saveTodoList(todoList: TodoList) = withContext(ioDispatcher) {
        todoListDao.insertTodoList(todoList)
    }

    fun loadTdListsByFolderId(folderId: Long): Flow<List<TodoList>> =
        todoListDao.loadTdListsByFolderId(folderId)

    fun getAllTodoLists(): Flow<List<TodoList>> = todoListDao.getAllTodoLists()
    fun loadTodoListById(id: Long): Flow<TodoList> = todoListDao.loadTodoListById(id)
    suspend fun deleteTodoLists(todoLists: Array<TodoList>): Int =
        todoListDao.deleteTodoLists(todoLists)

    // Room database functions for TodoListItem
    suspend fun insertTodoListItem(todoListItem: TodoListItem) =
        todoListItemDao.insertTodoListItem(todoListItem)

    fun loadUncheckedItemsByTodoListId(todoListId: Long): Flow<List<TodoListItem>> =
        todoListItemDao.loadUncheckedItemsByTodoListId(todoListId)

    fun loadCheckedItemsByTodoListId(todoListId: Long): Flow<List<TodoListItem>> =
        todoListItemDao.loadCheckedItemsByTodoListId(todoListId)

    suspend fun deleteTodoListItem(todoListItem: TodoListItem) = withContext(ioDispatcher) {
        todoListItemDao.deleteTodoListItem(todoListItem)
    }
}