package com.warmerhammer.personalnotes.FolderListFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.Data.RoomDatabaseRepo
import com.warmerhammer.personalnotes.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderListViewModel @Inject constructor(
    private val roomDBRepo: RoomDatabaseRepo,
) : ViewModel() {
    fun getAllFolders() = liveData {
        roomDBRepo.getAllFolders().collectLatest { folders ->
            emit(folders)
        }
    }
}