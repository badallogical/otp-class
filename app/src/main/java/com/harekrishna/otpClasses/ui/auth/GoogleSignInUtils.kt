package com.harekrishna.otpClasses.ui.auth

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.harekrishna.otpClasses.R
import io.jsonwebtoken.Jwts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class Role(val role: String) {
    ADMIN("admin"),
    FACILITATOR("facilitator")
}


class GoogleSignInUtils {

    companion object {

        /**
         * Perform Google Sign-In.
         */
        fun doGoogleSignIn(
            context: Context,
            scope: CoroutineScope,
            launcher: ManagedActivityResultLauncher<Intent, ActivityResult>?,
            onSignInSuccess: (String, String, String?) -> Unit, // Returns name, email, photoUrl
            onSignInError: (String) -> Unit, // Returns error message
            onInvalidUser: (String) -> Unit
        ) {
            val credentialManager = CredentialManager.create(context)

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(getCredentialOptions(context))
                .build()

            scope.launch {
                try {
                    // Fetch credentials from the Credential Manager
                    val result = credentialManager.getCredential(context, request)
                    when (result.credential) {
                        is CustomCredential -> {
                            if (result.credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                                val googleTokenId = googleIdTokenCredential.idToken
                                Log.d("GoogleSignIn", googleTokenId)

                                signInWithFirebase(googleIdTokenCredential.idToken, onSignInSuccess, onSignInError, onInvalidUser)
                            }
                        }
                        else -> {
                            onSignInError("Unsupported credential type.")
                        }
                    }
                } catch (e: NoCredentialException) {
                    launcher?.launch(getIntent()) // Launch account picker if no credential is found
                } catch (e: GetCredentialException) {
                    onSignInError("Credential fetch failed: ${e.message}")
                }
            }
        }

        /**
         * Sign in with Firebase using Google ID Token.
         */
        private fun signInWithFirebase(
            idToken: String,
            onSignInSuccess: (String, String, String?) -> Unit,
            onSignInError: (String) -> Unit,
            onInvalidUser: (String) -> Unit
        ) {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            Firebase.auth.signInWithCredential(authCredential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = task.result?.user
                        if (user != null) {
                            val name = user.displayName ?: "Unknown User"
                            val email = user.email ?: "Unknown Email"
                            val photoUrl = user.photoUrl?.toString()



                            getUserRoleByEmail(email, onRoleRetrieved = { role ->
                                if (role != null && Role.entries.any { it.role == role }) {
                                    Log.d("GoogleSignIn", "${name} is an admin")
                                    // If admin only then sign-in.
                                    onSignInSuccess(email, name, photoUrl)
                                }
                                else{
                                    onInvalidUser(name)
                                }
                            })

                        } else {
                            onSignInError("Failed to retrieve user details after sign-in.")
                        }
                    } else {
                        onSignInError("Firebase sign-in failed: ${task.exception?.message}")
                    }
                }
        }


        fun getUserRoleByEmail(providedEmail: String, onRoleRetrieved: (String?) -> Unit) {
            val database = FirebaseDatabase.getInstance()
            val usersRef = database.getReference("users")

            // Query to find the child node where email matches the provided email
            val query = usersRef.orderByChild("email").equalTo(providedEmail)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        Log.d("GoogleSignIn", snapshot.children.toString())
                        // Loop through matching children (there should be only one in this schema)
                        for (userSnapshot in snapshot.children) {
                            val role = userSnapshot.child("role").getValue(String::class.java)
                            onRoleRetrieved(role) // Pass the role to the callback
                            return
                        }
                    } else {
                        onRoleRetrieved(null) // No matching email found
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database query cancellation
                    onRoleRetrieved(null)
                }
            })
        }



        private fun getIntent():Intent{
            return Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
        }

        private fun getCredentialOptions(context: Context):CredentialOption{
            return GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .build()
        }
    }
}