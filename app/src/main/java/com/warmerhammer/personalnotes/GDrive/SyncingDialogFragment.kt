package com.warmerhammer.personalnotes.GDrive

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment

class SyncingDialogFragment(
    private val message: String,
    private val positiveClickListener : (positive: Boolean) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Done"
            ) { _, _ ->
                positiveClickListener(true)
            }.create()

    companion object {
        const val TAG = "AlertDialogFragment"
    }
}