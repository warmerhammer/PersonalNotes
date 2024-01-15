package com.warmerhammer.personalnotes.GDrive

import android.accounts.AccountManager
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.R
import com.warmerhammer.personalnotes.Utils.AlertDialogFragment
import com.warmerhammer.personalnotes.databinding.ActivityGdriveBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.*
import javax.inject.Inject

@ActivityScoped
@AndroidEntryPoint
class GDriveActivity @Inject constructor() : AppCompatActivity() {

    private val viewModel: GDriveViewModel by viewModels()
    private lateinit var credential: GoogleAccountCredential
    private val REQUEST_GOOGLE_PLAY_SERVICES = 0
    val uploadScope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var binding: ActivityGdriveBinding
    private var doc: Project? = null
    private var driveService: Drive? = null
    private val startForResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.i(TAG, "result :: $result")
            if (result.data != null) {
                val accountName =
                    result.data!!.extras?.getString(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    Log.i(TAG, "registerForActivityResult() accountName :: $accountName")
                    credential.selectedAccountName = accountName
                }

            } else {
                Log.i(TAG, "regsisterForActivityResult :: result.data == null")
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)
        binding = ActivityGdriveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (driveService == null) {
            credential = GoogleAccountCredential.usingOAuth2(
                applicationContext, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_METADATA)
            )

            GoogleSignIn.getLastSignedInAccount(applicationContext)?.let { googleAccount ->
                credential.selectedAccount = googleAccount.account
                driveService =
                    Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory.getDefaultInstance(),
                        credential
                    )
                        .setApplicationName(R.string.app_name.toString())
                        .build()

                if (driveService != null) viewModel.storeDriveService(driveService!!)
            }
        }

        observeCurrDoc()
        observeFileId()

        binding.ClickToStop.setOnClickListener {
            closeActivity(true)
        }

        binding.ClickToSendLink.setOnClickListener {
            sendLink()
        }

        binding.closeInfoBox.setOnClickListener {
            binding.infoBox.visibility = View.GONE
            binding.minimizedInfoBox.visibility = View.VISIBLE
            binding.ClickToStopTwo.setOnClickListener {
                closeActivity(true)
            }
            binding.ClickToSendLinkTwo.setOnClickListener {
                sendLink()
            }
        }
    }


    private fun observeCurrDoc() {
        val loadingAnimator = LoadingAnimator { animatedText ->
            binding.GDriveTextContent.text = ""
            binding.GDriveTextContent.text = animatedText
        }

        viewModel.currDoc.observe(this as LifecycleOwner) { project ->
            if (doc == null) {
                doc = project
                syncDoc()
            } else {
                doc = project
            }

            binding.GDriveTitle.text = project.title
            val textContent = if (project.category == "notes") (project as Note).text else ""

            loadingAnimator.stop()

            loadingAnimator.start(
                textContent!!
            )

            if (checkGooglePlayServicesAvailable()) {
                haveGooglePlayServices()
            }
        }
    }

    private fun observeFileId() {
        viewModel.fileId.observe(this as LifecycleOwner) {
            binding.gDriveLoader.visibility = View.GONE
            binding.gDriveAcivityButtons1.visibility = View.VISIBLE
        }
    }

    // send email with link
    private fun sendLink() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/html"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(Firebase.auth.currentUser!!.email!!))
        intent.putExtra(Intent.EXTRA_SUBJECT, "Link to PersonalDoc Note")
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "Here is the link to share ${doc!!.title} : https://docs.google.com/document/d/"
                    + "${viewModel.fileId.value}"
        )
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        val type = intent.getStringExtra("type")
        val id = intent.getStringExtra("id")
        val parentFolder = intent.getStringExtra("folder")

        viewModel.getNote(parentFolder!!, id!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "viewModel :: $viewModel")
        viewModel.deleteDoc()
        if (doc!!.category == "notes") viewModel.saveNote(doc as Note)
    }

    private fun syncDoc() {
        try {
            viewModel.syncDoc(doc!!)
        } catch (e: Throwable) {
            if (e is UserRecoverableAuthIOException) {
                startForResult.launch(e.intent)
                Log.e(
                    "1. GDriveActivity.kt > uploadFile()",
                    "Error uploading to GoogleDrive",
                    e.cause
                )
            } else {
                Log.e(
                    "2. GDriveActivity.kt > uploadFile()",
                    "Error uploading to GoogleDrive",
                    e
                )
            }
        }
    }

    private fun closeActivity(success: Boolean) {
        val i = Intent()

        if (success) {
            AlertDialogFragment(
                "Are You Sure You Would Like To Stop Syncing " +
                        "Your Note With Google Drive?"
            ) { positiveClick ->
                if (positiveClick) {
                    i.putExtra("OPERATION", "GDrive()")
                    setResult(RESULT_OK, i)
                    this.finish()
                }
            }.show(
                supportFragmentManager, TAG
            )

        } else {
            i.putExtra("OPERATION", "GDrive()")
            setResult(RESULT_CANCELED)
            this.finish()
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        runOnUiThread {
            val dialog: Dialog? = GoogleApiAvailability.getInstance().getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES
            )
            dialog?.show()
        }
    }

    private fun checkGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (googleApiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
            return false
        }
        return true
    }

    private fun haveGooglePlayServices() {
        // check if there is already an account selected
        if (credential.selectedAccountName == null) {
            // ask user to choose account
            chooseAccount()
        }
    }

    private fun chooseAccount() {
        startForResult.launch(credential.newChooseAccountIntent())
    }

    companion object {
        val TAG = GDriveActivity::class.java.simpleName + ".kt"
    }
}