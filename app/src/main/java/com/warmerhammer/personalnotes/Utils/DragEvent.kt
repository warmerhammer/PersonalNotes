package com.warmerhammer.personalnotes.Utils

import android.content.ClipDescription
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.warmerhammer.personalnotes.R

class OnDragEvent(private val context: Context) : View.OnDragListener {

    private lateinit var v: View
    private lateinit var ev: DragEvent
    private lateinit var draggableItem: View
    private var outOfBounds: String? = null
    private lateinit var displayMetrics: DisplayMetrics


    override fun onDrag(view: View?, event: DragEvent?): Boolean {
        v = view!!
        ev = event!!
        displayMetrics = context.resources.displayMetrics
        draggableItem = event.localState as View

        val isValid =
            when (ev.action) {
                DragEvent.ACTION_DRAG_STARTED -> actionDragStarted()
                DragEvent.ACTION_DRAG_ENTERED -> {
                    Log.i("DragEvent", "ACTION_DRAG_ENTERED")
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> actionDragLocation()
                DragEvent.ACTION_DRAG_EXITED -> {
                    view.alpha = 1.0f
                    draggableItem.visibility = View.VISIBLE
                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DROP -> actionDrop()
                DragEvent.ACTION_DRAG_ENDED -> actionDragEnded()
                else -> {
                    Log.e("DragEvent", "Unknown action type received by OnDragListener")
                    false
                }
            }

        return isValid
    }

    private fun actionDragStarted(): Boolean =
        if (ev.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            Log.i("DragEvent()", "This is draggable")
            // returns true to indicate that the View can accept the dragged data
            true
        } else {
            Log.i("DragEvent()", "Not draggable")
            false
        }

    private fun actionDragLocation(): Boolean {
        v.foreground = getDrawable(context, R.drawable.drag_border_red)

        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        val dx = screenWidth - (screenWidth - ev.x)
        val dy = screenHeight - (screenHeight - ev.y)

        Log.i("DragEvent", "dx $dx")

        // this means item is out of bounds
        return if (dx <= START_PADDING) {
            outOfBounds = "left"
            false
        } else if (dx >= screenWidth - END_PADDING) {
            outOfBounds = "right"
            false
        } else if (dy <= TOP_PADDING) {
            outOfBounds = "top"
            false
        } else if (dy >= screenHeight - BOTTOM_PADDING) {
            outOfBounds = "bottom"
            false
        } else { // drag is inbounds
            outOfBounds = null
            v.foreground = getDrawable(context, R.drawable.drag_border_green)
            true
        }
    }

    private fun actionDrop(): Boolean {

        Log.i("DragEvent", "draggableItem.x :: ${actionDragLocation()}")

        v.foreground = null

        return if (actionDragLocation()) {
            draggableItem = ev.localState as View
            draggableItem.y = ev.y - (draggableItem.height - 55)
            draggableItem.x = ev.x - (draggableItem.width - 100)

            // update view
            val parent = draggableItem.parent as RelativeLayout
            parent.removeView(draggableItem)

            val dropArea = v as RelativeLayout
            dropArea.addView(draggableItem)

            v.foreground = null
            v.invalidate()

            true
        } else {
            v.foreground = null
            false
        }
    }

    private fun actionDragEnded(): Boolean {
        // turn off any color tinting
        (v as? ImageView)?.clearColorFilter()

        v.invalidate()

        when (ev.result) {
            true ->
                Log.i("DragEvent", "The drop was handled true.")
            else ->
                Log.i("DragEvent", "The drop was handled false.")
        }

        return true
    }

    companion object {
        private const val END_PADDING = 100
        private const val START_PADDING = 475
        private const val BOTTOM_PADDING = 70
        private const val TOP_PADDING = 890
    }

}