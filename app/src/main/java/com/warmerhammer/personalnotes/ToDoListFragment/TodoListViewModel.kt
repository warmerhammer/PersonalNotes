package com.warmerhammer.personalnotes.ToDoListFragment

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItem
import com.warmerhammer.personalnotes.Data.RoomDatabaseRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TodoListViewModel @Inject constructor(
    private val roomDBRepo: RoomDatabaseRepo,
) : ViewModel() {

    private val _uncheckedItems = MutableLiveData<List<TodoListItem>>()
    val uncheckedItems: MutableLiveData<List<TodoListItem>> = _uncheckedItems

    private val _checkedItems = MutableLiveData<List<TodoListItem>>()
    val checkedItems: MutableLiveData<List<TodoListItem>> = _checkedItems

    private val _todoListId = MutableLiveData<Long>()
    val todoListId: MutableLiveData<Long> = _todoListId

    // todoList functions
    fun initTodoListId(id: Long, folderId: Long) = viewModelScope.launch {
        if (id == -1L) {
            val newId = roomDBRepo.saveTodoList(TodoList(folderId=folderId))
            _todoListId.value = newId.first()
        } else {
            _todoListId.value = id
        }
    }

    fun saveTodoList(todoList: TodoList) = viewModelScope.launch {
        Log.d("TodoListViewModel", "saveTodoList()")
        roomDBRepo.saveTodoList(todoList)
    }

    fun loadTodoListById(id: Long) = liveData {
        roomDBRepo.loadTodoListById(id).collectLatest { todoList ->
            emit(todoList)
        }
    }

    fun deleteTodoList(todoList: TodoList) = viewModelScope.launch {
        roomDBRepo.deleteTodoLists(arrayOf(todoList))
    }

    // todoListItem functions
    fun loadAllTodoListItemsByTodoListId(todoListId: Long) = viewModelScope.launch {
        // first load checked items
        roomDBRepo.loadCheckedItemsByTodoListId(todoListId).collectLatest { checkedItems ->
            _checkedItems.value = checkedItems
            // then load unchecked items
            roomDBRepo.loadUncheckedItemsByTodoListId(todoListId).collectLatest { uncheckedItems ->
                _uncheckedItems.value = uncheckedItems
            }
        }
    }

    fun saveTodoListItem(todoListItem: TodoListItem) = viewModelScope.launch {
        roomDBRepo.insertTodoListItem(todoListItem)
    }

    fun deleteTodoListItem(todoListItem: TodoListItem) = viewModelScope.launch {
        roomDBRepo.deleteTodoListItem(todoListItem)
    }
}