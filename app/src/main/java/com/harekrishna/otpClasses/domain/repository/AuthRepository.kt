package com.harekrishna.otpClasses.domain.repository

import android.content.Context
import com.harekrishna.otpClasses.domain.model.User

interface AuthRepository {
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInAnonymously(): Result<User>
    fun getCurrentUser(): User?
    suspend fun signOut(context: Context): Result<Unit>
    suspend fun linkAnonymousWithGoogle(idToken: String): Result<User>
    fun updateUserProfile(name: String, phone: String)
}
