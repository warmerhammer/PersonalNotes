package com.warmerhammer.personalnotes.SearchActivity

import android.content.ContentResolver
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warmerhammer.personalnotes.Data.DataClasses.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: SearchActivityRepo
) : ViewModel() {

    private val _suggestions = MutableLiveData<List<User>>()
    val suggestions: LiveData<List<User>> = _suggestions

    @RequiresApi(Build.VERSION_CODES.O)
    fun search(query: String?) {
        viewModelScope.launch {
            query?.let {
                repo.searchFriends(it).collectLatest { users ->
                    _suggestions.postValue(users)
                }
            }
        }

    }
}