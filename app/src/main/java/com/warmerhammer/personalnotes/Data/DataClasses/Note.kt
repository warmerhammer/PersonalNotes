package com.warmerhammer.personalnotes.Data.DataClasses

import androidx.room.*
import com.warmerhammer.personalnotes.R
import kotlinx.coroutines.flow.Flow

@Entity
data class Note (
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    override var timestamp: Long? = System.currentTimeMillis(),
    override var folderId: Long? = null,
    override var title: String? = "",
    override var synced : Boolean? = false,
    override var image: Int? = R.drawable.ic_baseline_edit_note_24,
    override var category: String? = "notes",
    var text: String? = ""
) : Project()

@Dao
interface NoteDao {
    @Query("SELECT * FROM note")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM note WHERE id = :id")
    fun loadNoteById(id: Long) : Flow<Note>

    @Query("SELECT * FROM note WHERE folderId = :folderId")
    fun loadNotesByFolderId(folderId: Long) : Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(vararg note: Note)

    @Delete
    suspend fun deleteNotes(project: Array<Note>) : Int
}