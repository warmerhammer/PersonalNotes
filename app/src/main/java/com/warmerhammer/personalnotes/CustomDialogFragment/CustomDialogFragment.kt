package com.warmerhammer.personalnotes.CustomDialogFragment

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.warmerhammer.personalnotes.R

class CustomDialogFragment(
    private val message : String,
    private val callback : (text: String, result: Boolean) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.HORIZONTAL
        val etParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        etParams.setMargins(75, 10, 50, 10)

        val editText = EditText(requireContext())
        editText.hint = "Please enter project title..."

        linearLayout.addView(editText, etParams)

        builder.setView(linearLayout)
            .setTitle(message)
            .setPositiveButton(R.string.ok) { _, _ ->
                callback(editText.text.toString(), true)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                callback(editText.text.toString(), false)
            }

        return builder.create()
    }
}
