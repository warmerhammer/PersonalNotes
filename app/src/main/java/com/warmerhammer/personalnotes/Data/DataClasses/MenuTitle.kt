package com.warmerhammer.personalnotes.Data.DataClasses

import java.net.URI

data class MenuTitle(
    var title : String,
    var imageURL: String?,
    var showImage: Boolean = false
)
