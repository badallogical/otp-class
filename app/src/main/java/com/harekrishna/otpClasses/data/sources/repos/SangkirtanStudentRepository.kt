package com.harekrishna.otpClasses.data.sources.repos

import android.content.Context
import android.util.Log
import com.harekrishna.otpClasses.core.utils.NetworkChecker
import com.harekrishna.otpClasses.data.api.ApiService
import com.harekrishna.otpClasses.data.models.SangkirtanStudentDTO
import com.harekrishna.otpClasses.data.models.SangkirtanStudentPOJO
import com.harekrishna.otpClasses.data.models.SangkirtanRegistrationStatus
import com.harekrishna.otpClasses.data.sources.db.dao.SangkirtanStudentDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SangkirtanStudentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sangkirtanStudentDao: SangkirtanStudentDao,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val TAG = "SangkirtanStudentRepository"

    // Insert a student into the database and then sync to remote
    suspend fun insertStudent(sangkirtanStudent: SangkirtanStudentDTO) = withContext(Dispatchers.IO) {
        // Save to local database
        sangkirtanStudentDao.insert(sangkirtanStudent.copy(sync = false))
    }
    
    suspend fun updateStudentToSynced(phone: String) = withContext(Dispatchers.IO) {
        sangkirtanStudentDao.updateToSync(phone)
    }

    // Fetch all students as a Flow, with automatic remote sync if empty
    fun getAllSangkirtanStudents(): Flow<List<SangkirtanStudentPOJO>> = flow {
        // 1. Emit local data first
        val localStudents = sangkirtanStudentDao.getAllSangkirtanStudents().first()
        if (localStudents.isNotEmpty()) {
            emit(localStudents)
        }

        // 2. If local is empty, try to fetch from remote
        if (localStudents.isEmpty() && NetworkChecker.isInternetAvailable(context)) {
            try {
                syncRemoteDataToLocal()
                val updatedStudents = sangkirtanStudentDao.getAllSangkirtanStudents().first()
                emit(updatedStudents)
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing remote data: ${e.message}")
            }
        }
    }

    private suspend fun syncRemoteDataToLocal() {
        val remoteStudents = ApiService.getSangkirtanStudents()
        remoteStudents.forEach { student ->
            sangkirtanStudentDao.insert(student.copy(sync = true))
        }
    }

    fun getRegistrationList(): Flow<List<SangkirtanRegistrationStatus>> = flow {
        val userData = userPreferencesRepository.getUserData().first()

        // Attempt to get the data from the local database (Room)
        val localRegistrationCounts = userData.second?.let {
            sangkirtanStudentDao.getRegistrationList(it).first()
        }
        Log.d(TAG, localRegistrationCounts.toString())

        // Emit local data if available
        if (localRegistrationCounts != null && localRegistrationCounts.isNotEmpty()) {
            emit(localRegistrationCounts)
        }

        // If local data is empty, check for internet and fetch from remote
        if (localRegistrationCounts.isNullOrEmpty()) {
            if (NetworkChecker.isInternetAvailable(context)) {
                try {
                    // Sync to get the remote data
                    syncRemoteDataToLocal()

                    // Get the updated data from the local database
                    val remoteRegistrationCounts = userData.second?.let {
                        sangkirtanStudentDao.getRegistrationList(it).first()
                    }
                    Log.d(TAG, remoteRegistrationCounts.toString())
                    emit(remoteRegistrationCounts ?: emptyList())
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    emit(emptyList())
                }
            } else {
                throw Exception("No internet connection")
            }
        }
    }

    suspend fun syncFullLocalRegistrations(date: String) {
        withContext(Dispatchers.IO) {
            val userData = userPreferencesRepository.getUserData().first()
            userData.second?.let { userId ->
                sangkirtanStudentDao.getFullRegistrationsByDate(date, userId)
                    .take(1)
                    .collect { registrations ->
                        registrations.forEach { registration ->
                            try {
                                Log.d(TAG, "syncing sangkirtan: ${registration.toString()}")
                                val response = ApiService.registerSangkirtanStudent(registration)
                                if (response.isSuccessful) {
                                    sangkirtanStudentDao.updateToSync(registration.phone)
                                    Log.d(TAG, "Response : ${response.isSuccessful}")
                                } else {
                                    Log.e(TAG, "Failed to sync registration for ${registration.phone}")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
        }
    }

    suspend fun syncLocalRegistrations(date: String) {
        withContext(Dispatchers.IO) {
            val userData = userPreferencesRepository.getUserData().first()
            userData.second?.let { userId ->
                sangkirtanStudentDao.getFullRegistrationsByDateNotSynced(date, userId)
                    .take(1)
                    .collect { registrations ->
                        registrations.forEach { registration ->
                            try {
                                Log.d(TAG, "syncing sangkirtan: ${registration.toString()}")
                                val response = ApiService.registerSangkirtanStudent(registration)
                                if (response.isSuccessful) {
                                    sangkirtanStudentDao.updateToSync(registration.phone)
                                    Log.d(TAG, "Response : ${response.isSuccessful}")
                                } else {
                                    Log.e(TAG, "Failed to sync registration for ${registration.phone}")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
        }
    }

    suspend fun deleteRegistrationByDate(date: String) {
        withContext(Dispatchers.IO) {
            val userData = userPreferencesRepository.getUserData().first()
            userData.second?.let { userId ->
                sangkirtanStudentDao.getFullRegistrationsByDate(date, userId)
                    .take(1)
                    .collect { registrations ->
                        registrations.forEach { registration ->
                            try {
                                sangkirtanStudentDao.deleteByPhone(registration.phone)
                                Log.d(TAG, "deleting sangkirtan: ${registration.toString()}")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
            }
        }
    }
}
