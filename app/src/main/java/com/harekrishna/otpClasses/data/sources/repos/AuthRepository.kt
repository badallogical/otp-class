package com.harekrishna.otpClasses.data.sources.repos

import com.harekrishna.otpClasses.data.models.User

interface AuthRepository{
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInAnonymously(): Result<User>
    fun getCurrentUser(): User?
}