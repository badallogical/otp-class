package com.harekrishna.otpClasses.data.sources.repos

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.harekrishna.otpClasses.data.models.UserEntity
import com.harekrishna.otpClasses.domain.model.Role
import com.harekrishna.otpClasses.domain.model.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()

            val user = authResult.user ?: throw Exception("User is null")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Guest creation failed")

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): FirebaseUser? {
        val user = auth.currentUser ?: return null

        return try {
            user.reload().await()      // Fetch latest user data from Firebase
            auth.currentUser           // Return refreshed user
        } catch (e: Exception) {
            null
        }
    }




    override suspend fun linkAnonymousWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("No anonymous user found to link.")

            // 1. Create the Google credential from the token
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // 2. Link the credential to the existing anonymous user
            val authResult = currentUser.linkWithCredential(credential).await()
            val linkedUser = authResult.user ?: throw Exception("Linking failed: User is null")

            // 3. Return the updated user (no longer a guest)
            Result.success(linkedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun signOut(context: Context): Result<Unit> {
        return try {
            // 1. Sign out from Firebase Auth
            auth.signOut()

            // 2. Clear Google Credential Manager Active Session State
            val credentialManager = CredentialManager.create(context)
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}