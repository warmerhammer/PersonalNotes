package com.warmerhammer.personalnotes.MainActivity

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.widget.LinearLayout
import com.warmerhammer.personalnotes.Utils.DragListener

class DrawerNavigationDragListener(context: Context) : View.OnDragListener {

    private val dropListener = context as DragListener

    override fun onDrag(v: View?, ev: DragEvent?): Boolean {
        return when (ev?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                // boolean true or false
                ev.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                Log.i(TAG, "Entered :: ${v?.tag}")
                (v as? LinearLayout)?.setBackgroundColor(Color.LTGRAY)
                v?.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                Log.i(TAG, "Location :: ${ev.x}, ${ev.y}")
                true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                (v as? LinearLayout)?.setBackgroundColor(Color.WHITE)
                v?.invalidate()
                true
            }
            DragEvent.ACTION_DROP -> {
                val item: ClipData.Item = ev.clipData.getItemAt(0)
                val (_id, category, parentFolder) = item.text.split(":")
                dropListener.sendDrawerDropData(_id, category, parentFolder, v?.tag as String)
                (v as? LinearLayout)?.setBackgroundColor(Color.WHITE)
                v.invalidate()
                true
            }
            else -> {
                Log.e(
                    TAG,
                    "Unknown action type received by View.OnDragListener."
                )
                false
            }
        }
    }


    companion object {
        val TAG = DrawerNavigationDragListener::class.java.simpleName + ".kt"
    }

}