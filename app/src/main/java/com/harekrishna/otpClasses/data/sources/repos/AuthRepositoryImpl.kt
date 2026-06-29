package com.harekrishna.otpClasses.data.sources.repos

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.harekrishna.otpClasses.data.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()

            val user = authResult.user ?: throw Exception("User is null")
            Result.success(mapToDomainUser(user, isGuest = false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<User> {
        return try {
            val authResult = auth.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Guest creation failed")

            Result.success(mapToDomainUser(user, isGuest = true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return mapToDomainUser(firebaseUser, firebaseUser.isAnonymous)
    }

    override suspend fun linkAnonymousWithGoogle(idToken: String): Result<User> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("No anonymous user found to link.")

            // 1. Create the Google credential from the token
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            // 2. Link the credential to the existing anonymous user
            val authResult = currentUser.linkWithCredential(credential).await()
            val linkedUser = authResult.user ?: throw Exception("Linking failed: User is null")

            // 3. Return the updated user (no longer a guest)
            Result.success(mapToDomainUser(linkedUser, isGuest = false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun updateUserProfile(name: String, phone: String) {
        TODO("Not yet implemented")
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

    private fun mapToDomainUser(firebaseUser: com.google.firebase.auth.FirebaseUser, isGuest: Boolean): User {
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName?: "NA",
            phone = firebaseUser.phoneNumber?: "NA",
            photoURL = firebaseUser.photoUrl?.toString() ?: "NA",
            email = firebaseUser.email?: "NA",
            isGuest = isGuest
        )
    }
}