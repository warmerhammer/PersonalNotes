package com.warmerhammer.personalnotes.MainActivity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.warmerhammer.personalnotes.GoogleSignInActivity
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.*
import javax.inject.Inject

@ActivityScoped
class SignOnForActivityResult @Inject constructor(
    @ActivityContext private val context: Context,
    private val registry: ActivityResultRegistry,
    private val viewModel: MainActivityViewModel,
    private val signOnComplete: () -> Unit
) :
    DefaultLifecycleObserver {
    private lateinit var signOnForActivityResult: ActivityResultLauncher<Intent>
    private val addUserScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(owner: LifecycleOwner) {
        signOnForActivityResult =
            registry.register(
                "key",
                owner,
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result != null) {
                    if (result.resultCode == Activity.RESULT_OK) {
                        val intent = result.data
                        val photoUrl =
                            intent?.extras?.getParcelable<Parcelable?>("PHOTO_URL").toString()
                        val name = intent?.getStringExtra("NAME")
                        val uid = intent?.getStringExtra("UID") ?: ""
                        val email = intent?.getStringExtra("ACCOUNT_EMAIL")

                        val operation = intent?.getStringExtra("OPERATION")
                        Log.d(TAG, "operation :: $operation")
                        when (operation) {
                            "GoogleSignInActivity()" -> {
                                viewModel.addUser(email, name, photoUrl)
                                signOnComplete()
                            }
                        }

                    } else {
                        Log.i(TAG, "signOnActivityForResult() failed.")
                    }
                }
            }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        addUserScope.cancel()
    }

    fun launchSignOn() {
        // prompt user to sign in
        val i = Intent(
            context,
            GoogleSignInActivity::class.java
        )
        Log.i(TAG, "SignOnForActivityResult.kt > launchSignOn()")
        signOnForActivityResult.launch(i)
    }

    companion object {
        val TAG = SignOnForActivityResult::class.java.simpleName
    }
}