package com.warmerhammer.personalnotes.Data.DataClasses

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.warmerhammer.personalnotes.R
import kotlinx.coroutines.flow.Flow

@Entity
data class Folder (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String? = null,
    val image: Int? = R.drawable.baseline_folder_24,
    var timestamp: Long? = System.currentTimeMillis(),
)

@Dao
interface FolderDao {
    @Query("SELECT * FROM Folder")
    fun getAllFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM Folder where name = (:name)")
    fun getFolderByName(name: String) : Flow<Folder>

    @Query("SELECT * FROM Folder WHERE id = :id")
    fun loadFolderById(id: Long) : Flow<Folder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(vararg folder: Folder)
}
