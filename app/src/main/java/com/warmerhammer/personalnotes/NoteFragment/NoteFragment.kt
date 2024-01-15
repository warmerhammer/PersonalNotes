package com.warmerhammer.personalnotes.NoteFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.navArgs
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.User
import com.warmerhammer.personalnotes.GDrive.GDriveActivity
import com.warmerhammer.personalnotes.MainActivity.MainActivityViewModel
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.AlertDialogFragment
import com.warmerhammer.personalnotes.Utils.TypingListener
import com.warmerhammer.personalnotes.databinding.FragmentAddNoteBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped

@FragmentScoped
@AndroidEntryPoint
class NoteFragment : Fragment() {

    private val mainViewModel: MainActivityViewModel by activityViewModels()
    private val noteViewModel: NoteFragmentViewModel by viewModels()
    private var binding: FragmentAddNoteBinding? = null
    private lateinit var uploadButton: ImageButton
    private lateinit var note: Note
    private val args: NoteFragmentArgs by navArgs()
    private lateinit var currUser: User
    private var operation: String? = null
    private var folderId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)

        return inflater.inflate(R.layout.fragment_add_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel.actionBarTitle.postValue("Note")
        uploadButton = view.findViewById(R.id.uploadNoteToCloudButton)
        folderId = args.folderId

        //  Retrieves note initially
        noteViewModel.loadNoteById(args.id)
            .observe(viewLifecycleOwner) { retrievedNote ->
                note = retrievedNote ?: Note(folderId = if (folderId != null) folderId else 0L)
                initNote()
            }

        syncClickListener()

        mainViewModel.currUser.observe(this as LifecycleOwner) { usr ->
            currUser = usr

            // after sign in trigger upload
            if (operation == "upload") {
                Log.d(TAG, "upload()")
                launchGDriveSyncActivity()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "args.id = ${args.id}")
    }

    private fun initNote() {
        val title: EditText = requireView().findViewById(R.id.noteTitle)
        val txt: EditText = requireView().findViewById(R.id.noteText)

        title.setText(note.title ?: "")
        txt.setText(note.text ?: "")

        title.addTextChangedListener(TypingListener { typing ->
            if (!typing) {
                note.title = title.text.toString()
            }
        })

        txt.addTextChangedListener(TypingListener { typing ->
            if (!typing) {
                note.text = txt.text.toString()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        if (note.title != null || note.text != null) {
            noteViewModel.saveNote(note)
        }
    }

    private fun syncClickListener() {
        uploadButton.setOnClickListener {
            // check if user is logged in
            if (currUser.email != null) {
                launchGDriveSyncActivity()
            } else {
                AlertDialogFragment("Sign On To Sync With Google Drive?") { positiveClick ->
                    if (positiveClick) {
                        operation = "upload"
                        mainViewModel.isSignOn()
                    }
                }.show(
                    childFragmentManager, TAG
                )
            }
        }
    }

    private fun launchGDriveSyncActivity() {
        Log.d(TAG, "launchGDriveSyncActivity()")
        val i = Intent(requireContext(), GDriveActivity::class.java)
        i.putExtra("type", "note")
        i.putExtra("id", note.id)
        i.putExtra("folder", note.folderId)
        startActivity(i)
    }


    companion object {
        private val TAG = "NoteFragment.kt"
    }
}