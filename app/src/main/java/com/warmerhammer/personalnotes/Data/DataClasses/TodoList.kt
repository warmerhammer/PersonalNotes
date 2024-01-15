package com.warmerhammer.personalnotes.Data.DataClasses

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.warmerhammer.personalnotes.R
import kotlinx.coroutines.flow.Flow

@Entity
data class TodoList(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    override var timestamp: Long? = System.currentTimeMillis(),
    override var folderId: Long? = null,
    override var image: Int? = R.drawable.ic_baseline_checklist_24,
    override var category: String? = "todoLists",
    override var title: String? = "",
    override var synced : Boolean? = false,
) : Project()

@Dao
interface TodoListDao {
    @Query("SELECT * FROM todolist")
    fun getAllTodoLists(): Flow<List<TodoList>>

    @Query("SELECT * FROM TodoList WHERE id = :id")
    fun loadTodoListById(id: Long) : Flow<TodoList>

    @Query("SELECT * FROM TodoList WHERE folderId = :folderId")
    fun loadTdListsByFolderId(folderId: Long) : Flow<List<TodoList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTodoList(vararg todoList: TodoList) : LongArray

    @Delete
    suspend fun deleteTodoLists(todoLists: Array<TodoList>) : Int
}