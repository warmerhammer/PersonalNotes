package com.warmerhammer.personalnotes.Data.DataClasses


// master data class for notes, To Do lists, and folders
abstract class Project {
    abstract val id: Long
    abstract var title: String?
    abstract var image: Int?
    abstract var category: String?
    abstract var folderId: Long?
    abstract var timestamp: Long?
    abstract var synced: Boolean?
}
