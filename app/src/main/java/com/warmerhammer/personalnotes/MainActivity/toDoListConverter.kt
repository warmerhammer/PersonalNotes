package com.warmerhammer.personalnotes.MainActivity

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.warmerhammer.personalnotes.Data.DataClasses.TodoList

class toDoListConverter {
    fun convert(document: DocumentSnapshot): TodoList {
        Log.d("todoListConverter", "documentId :: ${document.data}")
        val tdList = document.data!!
        return (
                TodoList(
                    id = -1,
                    folderId = tdList["folder"] as Long?,
                    timestamp = tdList["timestamp"] as Long?,
                    title = tdList["title"] as String
                ))
    }
}