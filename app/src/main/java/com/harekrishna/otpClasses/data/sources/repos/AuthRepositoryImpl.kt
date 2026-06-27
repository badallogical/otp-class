package com.harekrishna.otpClasses.data.sources.repos

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.harekrishna.otpClasses.data.models.User

class AuthRepositoryImpl(
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