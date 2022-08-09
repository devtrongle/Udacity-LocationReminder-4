package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.android.synthetic.main.activity_authentication.*
import java.util.*

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val viewModel by viewModels<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        viewModel.authenticationState.observe(this) { auth ->
            when(auth){
                AuthenticationViewModel.AuthenticationState.AUTHENTICATED -> {
                    startActivity(Intent(this@AuthenticationActivity, RemindersActivity::class.java))
                    finish()
                }
                AuthenticationViewModel.AuthenticationState.UNAUTHENTICATED -> { }
            }
        }

        btnLogin.setOnClickListener {
            // https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())

            // Create and launch sign-in intent
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            signInLauncher.launch(signInIntent)
        }
    }

    // See: https://developer.android.com/training/basics/intents/result
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { }
}
