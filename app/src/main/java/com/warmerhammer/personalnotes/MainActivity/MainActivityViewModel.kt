package com.warmerhammer.personalnotes.MainActivity

import androidx.lifecycle.*
import com.warmerhammer.personalnotes.Data.*
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItem
import com.warmerhammer.personalnotes.Data.DataClasses.User
import com.warmerhammer.personalnotes.DefaultDispatcher
import com.warmerhammer.personalnotes.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val repository: FirebaseRepo,
    private val roomDBRepo: RoomDatabaseRepo,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    val actionBarTitle = MutableLiveData<String>()

    private val _currUser = MutableLiveData<User>()
    val currUser: LiveData<User> = _currUser

    private val _allFolders = MutableLiveData<List<Folder>>()
    val allFolders: LiveData<List<Folder>> = _allFolders

    private val _currentFolder = MutableLiveData<Folder>()
    val currentFolder: LiveData<Folder> = _currentFolder

    private val _isSignOn = MutableLiveData<Boolean>()
    val isSignOn: LiveData<Boolean> = _isSignOn

    private val _checkedSet = MutableLiveData<Set<Project>>(setOf())
    val checkedSet: LiveData<Set<Project>> = _checkedSet

    private val _itemsToDelete = MutableLiveData<Set<Project>>(setOf())
    val itemsToDelete: LiveData<Set<Project>> = _itemsToDelete

    // functions for checking items to delete in HomePage RecyclerView
    fun markChecked(project: Project) {
        _checkedSet.value = _checkedSet.value?.plus(project)
    }

    fun removeCheck(project: Project) {
        _checkedSet.value = _checkedSet.value?.minus(project)
    }

    fun clearCheckedSet() {
        _checkedSet.value = setOf()
    }

    // functions for folders
    fun getHomePageFolder() = viewModelScope.launch {
        roomDBRepo.getFolderByName("Home").collectLatest {
            _currentFolder.postValue(it)
        }
    }

    fun getAllFolders() = viewModelScope.launch {
        roomDBRepo.getAllFolders().collectLatest { folders ->
            _allFolders.value = folders
        }
    }

    fun saveNewFolder(folder: Folder) {
        viewModelScope.launch {
            roomDBRepo.saveFolder(folder)
        }
    }

    fun setCurrentFolder(folder: Folder) {
        _currentFolder.value = folder
    }

    fun moveItem() {
        //todo
    }

//    fun observeUser() {
//        viewModelScope.launch(ioDispatcher) {
//            roomDBRepo.getUser().collect { usr ->
//                when {
//                    usr == null || usr.id == 0 -> {
//                        Log.d(TAG, "1. observeUser() user :: $usr")
//                        val newUser = User()
//                        _currUser.postValue(newUser)
//                        roomDBRepo.saveUser(newUser)
//                        repository.setUser(newUser)
//                    }
//                    else -> {
//                        _currUser.postValue(usr)
//                        repository.setUser(usr)
//                        Log.d(TAG, "2. observeUser() user :: $usr")
//                    }
//                }
//            }
//        }
//    }

    fun isSignOn() = _isSignOn.postValue(true)
    fun addUser(email: String? = null, name: String? = null, photoUrl: String? = null) {
        viewModelScope.launch {
            val userToAdd =
                User(email = email, name = name, photoUrl = photoUrl)
            roomDBRepo.saveUser(userToAdd)
            repository.setUser(userToAdd)
        }
    }

    fun deleteUserCache(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            roomDBRepo.deleteUser(user)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            val deleted = async { repository.deleteProject(project) }
            if (deleted.await()) {
                // changed to only Individual Project Fragment
                when (project.folderId) {
                    //todo
                }
            }
        }
    }

    fun deleteSelectedProjects() = _itemsToDelete.postValue(checkedSet.value)

//    fun retrieveDocs(project_id: Int) {
//        viewModelScope.launch {
//            repository.setProjectRef(project_id)
//
//            repository.getDocs().collectLatest { noteList ->
//                repository.getTodoLists().collectLatest { todoList ->
//                    _currentDocs.postValue(noteList + todoList)
//                }
//            }
//        }
//    }

//    fun clearCurrentDocs() = _currentDocs.postValue(listOf())

    fun getProject(projectId: Int): LiveData<Folder> = liveData {
        repository.getProject(projectId).collect { folder ->
            emit(folder)
        }
    }

    fun moveProject(
        projectId: String,
        category: String,
        parentFolder: String,
        newFolder: String
    ) = repository.moveProject(
        projectId,
        category,
        parentFolder,
        newFolder
    )


    companion object {
        val TAG = MainActivityViewModel::class.java.simpleName
    }
}
