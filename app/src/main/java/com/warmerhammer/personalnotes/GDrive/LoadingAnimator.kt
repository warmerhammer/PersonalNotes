package com.warmerhammer.personalnotes.GDrive

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan

import android.view.animation.LinearInterpolator
import com.warmerhammer.personalnotes.R

class LoadingAnimator(private val callback : (chars: CharSequence) -> Unit) {

    private var animator: ValueAnimator? = null


    fun start(strng: String) {
        animator = getAnimator(getCountList(strng))
        animator?.cancel()
        animator?.start()
    }

    private fun getCountList(strng: String): List<SpannableString> {

        val dotText = '.'

        val resultText = StringBuilder(strng).apply {
            repeat(DOT_COUNT) { append(dotText) }
        }.toString()

        val textList = mutableListOf<SpannableString>()
        for (i in 0 until DOT_COUNT + 1) {
            val spannable = SpannableString(resultText)

            val start = resultText.length - (DOT_COUNT - i)
            val end = resultText.length
            val flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            spannable.setSpan(ForegroundColorSpan(Color.TRANSPARENT), start, end, flag)

            textList.add(spannable)
        }

        return textList
    }

    private fun getAnimator(
        list: List<CharSequence>,
    ): ValueAnimator {
        val valueTo = list.size

        return ValueAnimator.ofInt(0, valueTo).apply {
            this.interpolator = LinearInterpolator()
            this.duration = 1000L
            this.repeatCount = ObjectAnimator.INFINITE
            this.repeatMode = ObjectAnimator.RESTART

            addUpdateListener {
                val value = it.animatedValue as? Int

                /**
                 * Sometimes [ValueAnimator] give a corner value which equals valueTo.
                 */
                if (value == null || value == valueTo) return@addUpdateListener

                val text = list.getOrNull(value)
                if (text != null) {
                    callback(text)
                }
            }
        }
    }

    fun stop() {
        animator?.cancel()
        animator = null
    }


    companion object {
        const val DOT_COUNT = 3
    }
}