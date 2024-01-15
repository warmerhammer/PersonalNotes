package com.warmerhammer.personalnotes.Data.DataClasses

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity
data class TodoListItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val todoListId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    var text: String,
    var checked: Boolean = false
)

@Dao
interface TodoListItemDao {
    @Query("SELECT * FROM TodoListItem where todoListId = (:todoListId)")
    fun loadAllTodoListItemsByTodolistId(todoListId: Long): Flow<List<TodoListItem>>

    @Query("SELECT * FROM TodoListItem where todoListId = (:todoListId) AND checked = 0")
    fun loadUncheckedItemsByTodoListId(todoListId: Long): Flow<List<TodoListItem>>

    @Query("SELECT * FROM TodoListItem where todoListId = (:todoListId) AND checked = 1")
    fun loadCheckedItemsByTodoListId(todoListId: Long): Flow<List<TodoListItem>>

    @Query("SELECT * FROM TodoListItem WHERE id IN (:id)")
    fun loadTodoListItemById(id: Long): Flow<TodoListItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodoListItem(vararg todoListItem: TodoListItem)

    @Delete
    suspend fun deleteTodoListItem(vararg todoListItem: TodoListItem)
}