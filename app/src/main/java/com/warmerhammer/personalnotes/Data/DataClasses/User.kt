package com.warmerhammer.personalnotes.Data.DataClasses

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity
data class User(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var email: String? = null,
    var name: String? = null,
    var photoUrl: String? = null,
)

@Dao
interface UserDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun saveUser(vararg user: User)

    @Query("SELECT * FROM user")
    fun getUser() : Flow<User?>

    @Delete
    fun deleteUser(vararg user: User)
}
