package com.warmerhammer.personalnotes.GDrive

import android.util.Log
import androidx.lifecycle.*
import com.google.api.services.drive.Drive
import com.warmerhammer.personalnotes.Data.FirebaseRepo
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GDriveViewModel @Inject constructor(
    private val gDriveRepo: GDriveRepo,
    private val fRepo: FirebaseRepo
) : ViewModel() {

    private val _currDoc = MutableLiveData<Project>()
    val currDoc: LiveData<Project> = _currDoc

    private val _fileId = MutableLiveData<String>()
    val fileId : LiveData<String> = _fileId

    fun getNote(noteFolder: String, id: String) {
        viewModelScope.launch {
            fRepo.getNote(noteFolder, id).collect { note ->
                Log.d(TAG, "note :: $note")
                _currDoc.postValue(note)
            }
        }
    }

    fun deleteDoc() {
        try {
            Log.d(TAG, "deleteDoc() > _fileId.value = ${_fileId.value}")
            gDriveRepo.deleteDoc(_fileId.value!!)
        } catch (e : Throwable) {
            Log.e(TAG, "deleteDoc() > Error deleting doc with fileId :: ${_fileId.value}")
        }
    }

    fun storeDriveService(drive: Drive) {
        gDriveRepo.setDriveService(drive)
    }

    fun syncDoc(doc: Project) {
        viewModelScope.launch {
            try {
                gDriveRepo.uploadDocs(doc).collect {
                    Log.d(TAG, "syncDoc() > _fileId.value = $it")
                    _fileId.value = it
                    gDriveRepo.syncDoc(_fileId.value).collect { doc ->
                        if (doc.category == "notes") saveNote(doc as Note)
                    }
                }
            } catch (e: Throwable) {
                throw e
            }
        }
    }

    fun saveNote(note: Note) {
        fRepo.saveNote(note)
    }

    companion object {
        private val TAG = "GDriveViewModel.kt"
    }
}
