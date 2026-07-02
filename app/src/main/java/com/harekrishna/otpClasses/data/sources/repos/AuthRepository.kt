package com.harekrishna.otpClasses.data.sources.repos

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.harekrishna.otpClasses.data.models.UserEntity

interface AuthRepository{
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun signInAnonymously(): Result<FirebaseUser>
    suspend fun getCurrentUser(): FirebaseUser?
    suspend fun signOut(context: Context): Result<Unit>
    suspend fun linkAnonymousWithGoogle(idToken: String): Result<FirebaseUser>
}