package com.warmerhammer.personalnotes.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.warmerhammer.personalnotes.Data.DataClasses.Folder
import com.warmerhammer.personalnotes.Data.DataClasses.FolderDao
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.NoteDao
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListDao
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItem
import com.warmerhammer.personalnotes.Data.DataClasses.TodoListItemDao
import com.warmerhammer.personalnotes.Data.DataClasses.User
import com.warmerhammer.personalnotes.Data.DataClasses.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Database(
    entities = [Note::class, User::class, Folder::class, TodoList::class, TodoListItem::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao
    abstract fun todoListDao(): TodoListDao
    abstract fun todoListItemDao(): TodoListItemDao
}

@Module
@InstallIn(SingletonComponent::class)
object RoomDBModule {

    // construct App Room database
    @Singleton
    @RoomDB
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return (
                Room.databaseBuilder(context, AppDatabase::class.java, "app-database")
                    .createFromAsset("defaultData.db")
                    .build()
                )
    }
}

annotation class RoomDB