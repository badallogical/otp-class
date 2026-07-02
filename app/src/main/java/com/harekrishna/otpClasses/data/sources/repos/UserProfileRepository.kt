package com.harekrishna.otpClasses.data.sources.repos

import com.google.firebase.auth.FirebaseUser
import com.harekrishna.otpClasses.data.models.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {

    fun observeUser(): Flow<UserEntity>

    suspend fun updateProfileName(id: String, name: String)

    suspend fun updateProfilePhone(id: String, phone : String )

    suspend fun syncUserProfile(firebaseUser : FirebaseUser)

    suspend fun initializeUser(firebaseUser: FirebaseUser): Result<Unit>

    // To Firestore
    suspend fun syncUser() : Result<Unit>

    // From Firestore
    suspend fun refreshUser() : Result<Unit>

    suspend fun deleteGuestUser() : Result<Unit>

    suspend fun updateGuestUser(currentUser : FirebaseUser) : Result<Unit>
}