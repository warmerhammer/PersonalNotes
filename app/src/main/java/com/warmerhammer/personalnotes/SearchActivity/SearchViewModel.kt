package com.warmerhammer.personalnotes.SearchActivity

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: SearchActivityRepo
) : ViewModel() {

    fun searchUser(query: String) {
        when (repo.initConstructTrie) {
            true -> {
                viewModelScope.launch {
                    repo.constructTrie().collect { success ->
                        Log.i("SearchViewModel.kt", "success :: $success")
                        repo.initConstructTrie = false
                        repo.searchUser(query)
                    }
                }
            }
            false -> repo.searchUser(query)
        }
    }

}