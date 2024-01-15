package com.warmerhammer.personalnotes.Utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.view.View

class DragShadowBuilder(v: View) : View.DragShadowBuilder(v) {
    private val shadow = ColorDrawable(Color.LTGRAY)

    override fun onProvideShadowMetrics(size: Point, touch: Point) {
        val width: Int = view.width - 75
        val height: Int = view.height - 900

        // match shadows dimensions with the canvas that the system provides
        shadow.setBounds(width - 375, 0, 0, height - 150)
        size.set(width, height)
        touch.set(width / 2, height - 225)
    }

    override fun onDrawShadow(canvas: Canvas) {
        shadow.draw(canvas)
    }

}