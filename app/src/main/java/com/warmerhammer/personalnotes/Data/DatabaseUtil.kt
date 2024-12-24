package com.warmerhammer.personalnotes.Data

import android.content.Context
import java.io.File

object DatabaseUtil {

    @Throws(Exception::class)
    fun getRoomDatabaseSize(context: Context, dbName: String): Long {
        val dbFolderPath = context.filesDir.absolutePath.replace("files", "databases")
        val dbFile = File("$dbFolderPath/$dbName")

        // Check if database file exist.
        if (!dbFile.exists()) throw Exception("${dbFile.absolutePath} doesn't exist")

        return dbFile.length()
    }

}