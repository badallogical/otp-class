package com.harekrishna.otpClasses.data.sources.repos

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.harekrishna.otpClasses.data.models.UserEntity
import com.harekrishna.otpClasses.data.sources.db.dao.UserEntityDao
import com.harekrishna.otpClasses.domain.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val userEntityDao: UserEntityDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserProfileRepository {


    override suspend fun syncUserProfile(firebaseUser : FirebaseUser) {
        //  its for the firest time only.
        if (firebaseUser.isAnonymous) {
            // First login
            val localUser = UserEntity(
                id = firebaseUser.uid,
                name = firebaseUser.displayName.orEmpty(),
                phone = firebaseUser.phoneNumber.orEmpty(),
                photoURL = firebaseUser.photoUrl?.toString().orEmpty(),
                email = firebaseUser.email.orEmpty(),
                isGuest = firebaseUser.isAnonymous,
                role = Role.DEVOTEE.name
            )

            userEntityDao.insert(localUser)
            Log.d("login", "user inserted")
        } else
            initializeUser(auth.currentUser!!)
    }

    override suspend fun initializeUser(firebaseUser: FirebaseUser): Result<Unit> {
        return try {
            val snapshot = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (snapshot.exists()) {

                // Existing user
                val remoteUser = snapshot.toObject(UserEntity::class.java)
                    ?: return Result.failure(Exception("Invalid user document"))

                userEntityDao.insert(remoteUser)

            } else {

                // First login
                val localUser = UserEntity(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName.orEmpty(),
                    phone = firebaseUser.phoneNumber.orEmpty(),
                    photoURL = firebaseUser.photoUrl?.toString().orEmpty(),
                    email = firebaseUser.email.orEmpty(),
                    isGuest = firebaseUser.isAnonymous,
                    role = Role.DEVOTEE.name
                )

                userEntityDao.insert(localUser)

                firestore.collection("users")
                    .document(localUser.id)
                    .set(localUser)
                    .await()
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun updateProfileName(id: String, name: String) {
        userEntityDao.updateName(id, name)
        syncUser()

    }

    override suspend fun updateProfilePhone(id: String, phone: String) {
        userEntityDao.updatePhone(id, phone)
        syncUser()
    }

    override fun observeUser(): Flow<UserEntity> {
        val uid: String = auth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        return userEntityDao.observeCurrentUser(uid)
    }

    override suspend fun syncUser(): Result<Unit> {
        return try {
            val uid: String = auth.currentUser?.uid
                ?: throw IllegalStateException("User not authenticated")

            val user = userEntityDao.observeCurrentUser(uid).first()

            // save to firestore
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // fetch the firebase user data from firestore and update userEntityDao
    override suspend fun refreshUser(): Result<Unit> {
        return try {
            val uid: String = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val snapshot = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val remoteUser = snapshot.toObject(UserEntity::class.java)
                ?: return Result.failure(Exception("User profile not found"))

            userEntityDao.update(remoteUser)

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGuestUser(): Result<Unit> {
        val currentUser = auth.currentUser
            ?: return Result.failure(IllegalStateException("User not authenticated"))

        if (!currentUser.isAnonymous) {
            Log.d("UserProfileRepository", "User is not a guest.")
            return Result.success(Unit)
        }

        return try {
            userEntityDao.deleteById(currentUser.uid)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGuestUser(currentUser: FirebaseUser): Result<Unit> {
        return try {

            val oldUser = userEntityDao.getUser(currentUser.uid).first()

            userEntityDao.update(
                UserEntity(
                    id = currentUser.uid,
                    isGuest = currentUser.isAnonymous,
                    name = currentUser.displayName.orEmpty(),
                    phone = oldUser?.phone.orEmpty(),
                    email = currentUser.email.orEmpty(),
                    photoURL = currentUser.photoUrl?.toString().orEmpty(),
                    role = oldUser?.role ?: Role.DEVOTEE.name
                )
            )

            // update to firestore
            syncUser()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
