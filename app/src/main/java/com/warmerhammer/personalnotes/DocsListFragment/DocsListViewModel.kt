package com.warmerhammer.personalnotes.DocsListFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.FirebaseRepo
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.Data.RoomDatabaseRepo
import com.warmerhammer.personalnotes.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DocsListViewModel @Inject constructor(
    private val repository: FirebaseRepo,
    private val roomDBRepo: RoomDatabaseRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _homePageDocs = MutableLiveData<List<Project>>()
    val homePageDocs: LiveData<List<Project>> = _homePageDocs

    fun getDocsByFolderId(folderId: Long) {
        viewModelScope.launch {
            roomDBRepo.loadNotesByFolderId(folderId).collectLatest { notesList ->
                roomDBRepo.loadTdListsByFolderId(folderId).collectLatest { todoListList ->
                    _homePageDocs.postValue(notesList + todoListList)
                }
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            withContext(ioDispatcher) {
                when (project.category) {
                    "notes" -> roomDBRepo.deleteNotes(arrayOf(project as Note))
                    else -> roomDBRepo.deleteTodoLists(arrayOf(project as TodoList))
                }
            }
        }
    }

    fun deleteSelectedProjects(currFolderId: Long, projects: Set<Project>) {
        val notes = ArrayList<Note>()
        val todoLists = ArrayList<TodoList>()
        projects.forEach { project ->
            when (project.category) {
                "notes" -> notes.add(project as Note)
                else -> todoLists.add(project as TodoList)
            }
        }

        // ensure deleting both is successful before updating HomeProjectFragment
        viewModelScope.launch {
            val noteRowsDeleted =
                if (notes.isNotEmpty()) {
                    async {
                        roomDBRepo.deleteNotes(notes.toTypedArray())
                    }
                } else {
                    async { 0 }
                }

            val todoListRowsDeleted =
                if (todoLists.isNotEmpty()) {
                    async {
                        roomDBRepo.deleteTodoLists(todoLists.toTypedArray())
                    }
                } else {
                    async { 0 }
                }

            if (noteRowsDeleted.await() > 0 || todoListRowsDeleted.await() > 0) {
                getDocsByFolderId(currFolderId)
            }
        }
    }

    companion object {
        val TAG: String = DocsListViewModel::class.java.simpleName
    }
}