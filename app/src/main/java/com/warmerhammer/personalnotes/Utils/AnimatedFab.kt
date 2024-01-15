package com.warmerhammer.personalnotes.Utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.marginRight
import com.warmerhammer.personalnotes.R

open class AnimatedFab(
    activity: Activity,
    context: Context,
) {

    private var fabContainer: ViewGroup = activity.findViewById(R.id.fab_container)

    interface FabActionListener {
        fun onFabAction1Clicked()
        fun onFabAction2Clicked()
        fun onFabAction3Clicked()
    }


    // declares the item being used when onDragEvent() is called
    private lateinit var item: ClipData.Item
    private var fab: FrameLayout = activity.findViewById(R.id.fab_frame)
    private var fabButton: ImageButton = activity.findViewById(R.id.fab)
    private var fabAction1: View = activity.findViewById(R.id.fab_action_1_ll)
    private var fabAction2: View
    private var fabAction3: View
    private var enableButton3 = false

    // item hint text
    private var fabAction1TextView: TextView =
        activity.findViewById(R.id.fab_action_1_instructions_tv)
    private var fabAction2TextView: TextView
    private var fabAction3TextView: TextView

    // amount to offset buttons when expanded
    private var offset1: Float? = null
    private var offset2: Float? = null
    private var offset3: Float? = null
    private lateinit var animatorSet: AnimatorSet

    // instantiate listener
    private var listener = context as FabActionListener
    private var dragListener = context as DragListener
    private val animationDuration = 350L

    var expanded = false


    init {
        // setup Action 1
        val fabAction1Button = activity.findViewById<ImageButton>(R.id.fab_action_1)
        fabAction1Button.setOnClickListener {
            fabAction1(it)
        }
        // setup Action 2
        fabAction2 = activity.findViewById(R.id.fab_action_2_ll)
        fabAction2TextView = activity.findViewById(R.id.fab_action_2_instructions_tv)
        val fabAction2Button = activity.findViewById<ImageButton>(R.id.fab_action_2)
        fabAction2Button.setOnClickListener {
            listener.onFabAction2Clicked()
        }
        // setup Action 3
        fabAction3 = activity.findViewById(R.id.fab_action_3_ll)
        fabAction3TextView = activity.findViewById(R.id.fab_action_3_instructions_tv)
        val fabAction3Button = activity.findViewById<ImageButton>(R.id.fab_action_3)
        fabAction3Button.setOnClickListener { fabAction3() }

        fabButton.setOnClickListener {
            expanded = if (expanded) {
                collapseFab()
                false
            } else {
                expandFab()
                true
            }
        }


        fabButton.setOnLongClickListener { v ->
            // initializes item
            item = ClipData.Item(v.tag as? CharSequence)

            val dragData = ClipData(
                v.tag as? CharSequence,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item
            )

            val myShadow = DragShadowBuilder(fabContainer)


            dragListener.startFabDrag(dragData, myShadow, fabContainer)

            true

//            fabContainer.startDragAndDrop(
//                dragData,
//                myShadow,
//                fabContainer,
//                0
//            )
        }

        fabContainer.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                fabContainer.viewTreeObserver.removeOnPreDrawListener(this)
                offset1 = fab.y - fabAction1.y
                fabAction1.translationY = offset1 as Float
                offset2 = fab.y - fabAction2.y
                fabAction2.translationY = offset2 as Float
                offset3 = fab.y - fabAction3.y
                fabAction3.translationY = offset3 as Float

                fabAction1.visibility = View.GONE
                fabAction2.visibility = View.GONE
                fabAction3.visibility = View.GONE

                return true
            }
        })
    }

    fun hide() {
        fabContainer.visibility = View.INVISIBLE
        collapseFab()
        expanded = false
    }

    fun show() {
        fabContainer.visibility = View.VISIBLE
    }

    fun enableButton3(yes: Boolean) {
        enableButton3 = yes
    }

    private fun collapseFab() {
        animatorSet = AnimatorSet().apply {
            createCollapseAnimator(fabAction1, offset1!!)
            createCollapseAnimator(fabAction2, offset2!!)
            createCollapseAnimator(fabAction3, offset3!!)
            unrotateX()
            // fade item hints
            fadeInstructionView(fabAction1TextView)
            fadeInstructionView(fabAction2TextView)
            fadeInstructionView(fabAction3TextView)

        }
        animateFab()
    }

    private fun expandFab() {
        animatorSet = AnimatorSet().apply {
            rotateX()
            createExpandAnimator(fabAction1, offset1!!)
            createExpandAnimator(fabAction2, offset2!!)
            createExpandAnimator(fabAction3, offset3!!)
            // fade in hints
            showInstructionView(fabAction1TextView)
            showInstructionView(fabAction2TextView)
            showInstructionView(fabAction3TextView)

        }
        animateFab()
    }

    private fun rotateX(): Animator {
        return ObjectAnimator.ofFloat(fab, "rotation", 0f, 45f).apply {
            duration = animationDuration
            start()
        }
    }

    private fun unrotateX(): Animator {
        return ObjectAnimator.ofFloat(fab, "rotation", 0f).apply {
            duration = animationDuration
            start()
        }
    }

    private fun createCollapseAnimator(view: View, offset: Float): Animator {
        return ObjectAnimator.ofFloat(view, "translationY", 0f, offset).apply {
            duration = animationDuration
            start()
        }
    }

    private fun showInstructionView(view: View): Animator {
        return ObjectAnimator.ofFloat(view, "alpha", 1f).apply {
            duration = 750L
            start()
        }
    }

    private fun fadeInstructionView(view: View): Animator {
        return ObjectAnimator.ofFloat(view, "alpha", 0f).apply {
            duration = 300L
            start()
        }
    }

    private fun createExpandAnimator(view: View, offset: Float): Animator {
        return ObjectAnimator.ofFloat(view, "translationY", offset, 0f).apply {
            duration = animationDuration
            start()
        }
    }

    @SuppressLint("Recycle")
    private fun animateFab() {
        val drawable = fabButton.drawable
        if (drawable is Animatable) {
            AnimatorSet().apply {
                play(animatorSet)
                start()
            }
        }
        fabAction3.visibility = if (enableButton3) View.VISIBLE else View.INVISIBLE
        fabAction1.visibility = View.VISIBLE
        fabAction2.visibility = View.VISIBLE
    }

    private fun fabAction1(view: View) {
        listener.onFabAction1Clicked()
    }

    private fun fabAction3() {
        listener.onFabAction3Clicked()
    }


}