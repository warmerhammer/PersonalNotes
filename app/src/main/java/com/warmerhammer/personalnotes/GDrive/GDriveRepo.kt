package com.warmerhammer.personalnotes.GDrive

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.warmerhammer.personalnotes.Data.DataClasses.Note
import com.warmerhammer.personalnotes.Data.DataClasses.Project
import com.warmerhammer.personalnotes.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GDriveRepo @Inject constructor(
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    DefaultLifecycleObserver {

    @ApplicationContext
    @Inject
    lateinit var context: Context
    private var driveService: Drive? = null
    private var savedLastPageToken: String? = null
    private val externalScope: CoroutineScope = CoroutineScope(dispatcherIO)
    private lateinit var repoDoc: Project

    fun setDriveService(gDrive: Drive) {
        driveService = gDrive
    }

    fun driveService(): Drive? {

//        GoogleSignIn.getLastSignedInAccount(context)?.let { googleAccount ->
//            val credential = GoogleAccountCredential.usingOAuth2(
//                context, listOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_METADATA)
//            )
//            credential.selectedAccount = googleAccount.account
//            if (driveService == null) {
//                driveService =
//                    Drive.Builder(
//                        AndroidHttp.newCompatibleTransport(),
//                        JacksonFactory.getDefaultInstance(),
//                        credential
//                    )
//                        .setApplicationName(R.string.app_name.toString())
//                        .build()
//            }
//        }
//
//        externalScope.launch {
//            kotlin.runCatching {
//                driveService!!.changes().startPageToken.execute()
//            }.onSuccess {
//                savedLastPageToken = it.startPageToken
//            }.onFailure { e ->
//                if (e is UserRecoverableAuthIOException) {
//                    Log.w(TAG, "driveService() :: UserRecoverableAuthIOException")
//                    GDriveActivity().startActivity(e.intent)
//                    Log.e(
//                        "1. GDriveActivity.kt > uploadFile()",
//                        "Error uploading to GoogleDrive",
//                        e.cause
//                    )
//                } else {
//                    Log.e(
//                        "2. GDriveActivity.kt > uploadFile()",
//                        "Error uploading to GoogleDrive",
//                        e.cause
//                    )
//                }
//            }
//        }

        return driveService
    }

    fun syncDoc(fileId: String?): Flow<Project> = flow {

        while (true) {
            kotlin.runCatching {
                val baos = ByteArrayOutputStream()
                withContext(dispatcherIO) {
                    Log.d(TAG, "fileId :: $fileId")
                    driveService?.files()?.export(fileId, "text/plain")
                        ?.executeMediaAndDownloadTo(baos)
                }
                baos.close()
                baos.toString()
            }.onSuccess { strng ->
                Log.i(TAG, "outputStream :: $strng")
                when (repoDoc.category) {
                    "notes" -> {
                        (repoDoc as Note).apply {
                            if (strng != this.text) {
                                Log.i(TAG, "this.text :: ${this.text}")
                                this.text = strng
                                emit(this)
                            }
                        }
                    }
                }
                delay(5000)
            }.onFailure { e ->
                Log.e("Unable to download file:", e.toString(), e.cause)
                throw e
            }
        }
    }

    fun deleteDoc(fileId: String) {
        externalScope.launch {
            kotlin.runCatching {
                Log.d(TAG, "delete() id :: $fileId")
                Log.d(TAG, "driveService :: $driveService")
                driveService?.files()?.delete(fileId)?.execute()
            }.onFailure { e ->
                Log.e(TAG, "deleteDoc() :: Error Deleting Doc with id $fileId")
                throw(e)
            }
        }
    }

    fun uploadDocs(doc: Project): Flow<String?> = flow {
        repoDoc = doc
        val docText = if (doc.category == "notes") (doc as Note).text else ""
        val gFileMetadata = com.google.api.services.drive.model.File()
        gFileMetadata.name = "Syncing With PersonalNotes App (temporary) ..."
        gFileMetadata.mimeType = ("application/vnd.google-apps.document")
        val inputStreamContent = InputStreamContent("text/plain", docText?.byteInputStream())

        driveService?.let { service ->
            kotlin.runCatching {
                val file =
                    withContext(dispatcherIO) {
                    service.files().create(gFileMetadata, inputStreamContent).setFields("id")
                        .execute()
                }

                Log.i(TAG, "GdriveRepo.kt > uploadDocs() :: fileId :: ${file.id}")
                file.id
            }.onFailure { e: Throwable ->
                Log.e(TAG, e.message, e.cause)
                throw e
            }.onSuccess { fileId ->
                emit(fileId)
            }
        } ?: Log.e(TAG, "Sign In Error - not logged in")
    }

    companion object {
        val TAG = GDriveRepo::class.java.simpleName + ".kt"
    }
}

