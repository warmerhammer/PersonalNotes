package com.warmerhammer.personalnotes

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.warmerhammer.personalnotes.databinding.ActivityGoogleSignInBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GoogleSignInActivity @Inject constructor() : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityGoogleSignInBinding
    private var user: FirebaseUser? = null
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleSignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //launch signin
        signIn()
    }

    private fun signIn() {
        // build signInRequest object
        user = auth.currentUser
        if (user == null) {
            // launch sign in intent
            createSignInIntent()
        } else {
            // send pertinent info back to MainActivity
            setResult()
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private fun createSignInIntent() {
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setAndroidPackageName(
                "com.warmerhammer.personalnotes",
                true,
                null
            )
            .setHandleCodeInApp(true)
            .setUrl("https://google.com")
            .build()

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder()
                .enableEmailLinkSignIn()
                .setActionCodeSettings(actionCodeSettings)
                .build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.mipmap.clipboard_icon)
            .setTheme(R.style.Theme_PersonalNotes)
            .setIsSmartLockEnabled(false)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        when {
            result.resultCode == RESULT_OK -> {
                // successfully signed in
                user = auth.currentUser
                Log.i(TAG, "User login successful :: $user")
                setResult()
            }
            response == null -> {
                Log.e(TAG, "User cancelled sign in flow")
            }
            else -> {
                Log.e(TAG, "Sign In Error :: ${response.error?.errorCode}")
            }
        }
    }

    private fun setResult() {
        if (user != null) {
            val i = intent
            Log.w("GoogleSignInActivity", "signInResultSuccessful code = ${user?.photoUrl}")
            i.putExtra("PHOTO_URL", user!!.photoUrl)
            i.putExtra("ACCOUNT_EMAIL", user!!.email)
            i.putExtra("NAME", user!!.displayName)
            i.putExtra("UID", user!!.uid)
            i.putExtra("OPERATION", "GoogleSignInActivity()")

            setResult(RESULT_OK, i)
            this.finish()
        }
    }

    fun signOut() {
        auth.signOut()
    }


    companion object {
        private const val TAG = "GoogleSignInActivity.kt"
    }
}


