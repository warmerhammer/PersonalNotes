package com.warmerhammer.personalnotes.Utils

import android.content.ClipData
import android.view.View
import android.view.ViewGroup

interface DragListener {
    fun startDrawerDrag(
        clipData: ClipData,
        shadowBuilder: View.DragShadowBuilder,
        view: View
    )

    fun startFabDrag(
        clipData: ClipData,
        shadowBuilder: View.DragShadowBuilder,
        viewGroup: ViewGroup
    )

    fun sendDrawerDropData(
        projectId: String,
        category: String,
        parentFolder: String,
        newFolder: String
    )
}