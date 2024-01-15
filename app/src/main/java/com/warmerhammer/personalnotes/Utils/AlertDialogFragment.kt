package com.warmerhammer.personalnotes.Utils

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment

class AlertDialogFragment(
    private val message: String,
    private val positiveClickListener : (positive: Boolean) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Yes"
            ) { _, _ ->
                positiveClickListener(true)
            }
            .setNegativeButton("No"){
                _ , _ -> positiveClickListener(false)
            }
            .create()

    companion object {
        const val TAG = "AlertDialogFragment"
    }
}