package com.warmerhammer.personalnotes.FolderListFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.Data.RoomDatabaseRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val roomDBRepo: RoomDatabaseRepo
) : ViewModel() {

    fun getAllDocs() = liveData<List<Project>> {
        roomDBRepo.getAllNotes().collectLatest { noteList ->
            roomDBRepo.getAllTodoLists().collectLatest {tdList ->
                emit(noteList + tdList)
            }
        }
    }

}