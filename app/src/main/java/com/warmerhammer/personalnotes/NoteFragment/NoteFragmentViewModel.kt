package com.warmerhammer.personalnotes.NoteFragment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.FirebaseRepo
import com.warmerhammer.personalnotes.Data.RoomDatabaseRepo
import com.warmerhammer.personalnotes.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@HiltViewModel
class NoteFragmentViewModel @Inject constructor(
    private val repository: FirebaseRepo,
    private val roomDBRepo: RoomDatabaseRepo,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    fun saveNote(note: Note) {
        viewModelScope.launch(ioDispatcher) {
            note.timestamp = System.currentTimeMillis()
            roomDBRepo.saveNote(note)
        }
    }

    fun loadNoteById(id: Long) = liveData {
        roomDBRepo.loadNoteById(id).collectLatest { note -> emit(note) }
    }


    fun loadFolderById(id: Long) = liveData {
        viewModelScope.launch(ioDispatcher) {
            roomDBRepo.getFolderById(id).collectLatest {
                emit(it)
            }
        }
    }

    companion object {
        private val TAG = NoteFragmentViewModel::class.java.simpleName + ".kt"
    }
}