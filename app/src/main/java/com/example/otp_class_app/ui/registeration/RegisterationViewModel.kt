package com.example.otp_class_app.ui.registeration

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.data.api.AttendanceDataStore
import com.example.otp_class_app.data.local.repos.StudentRepository
import com.example.otp_class_app.data.models.RegistrationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationViewModel(private val studentRepository: StudentRepository) : ViewModel() {
    // Mutable state flow directly holding the list of registrations
    private val _registrations = MutableStateFlow<List<RegistrationStatus>>(emptyList())
    // Publicly exposed immutable state
    val registrations: StateFlow<List<RegistrationStatus>> = _registrations.asStateFlow()

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository = application.container.studentRepository // Assuming container contains the repository
                RegistrationViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRegistration() {
        viewModelScope.launch {
            try {
                // Fetch the registration list from the repository
                val registrationList = withContext(Dispatchers.IO) {
                    studentRepository.getRegistrationList()
                }

                // If registrationList is not null, map each registration to fetch the sync status
                val updatedList = registrationList?.map { registration ->
                    async(Dispatchers.IO) {
                        val syncStatus = AttendanceDataStore.dateExists(registration.date)
                        registration.copy(synced = !syncStatus)
                    }
                }?.awaitAll() ?: emptyList()

                // Safely update the state in the main thread
                withContext(Dispatchers.Main) {
                    _registrations.update { current ->
                        updatedList
                    }
                }
            } catch (e: Exception) {
                // Handle any errors that might occur (e.g., network issues, etc.)
                e.printStackTrace()
            }
        }
    }



    fun syncRegistrations(){
        // Set syncing to true at the start
        _syncing.update { true }

        viewModelScope.launch {
            try {
                // Switch to IO dispatcher to fetch dates and perform the sync
                val dates = withContext(Dispatchers.IO) {
                    AttendanceDataStore.getDates.first()
                }

                Log.d("registrations", "to sync dates ${dates}")

                // Sync local registrations
                withContext(Dispatchers.IO) {
                    dates.forEach { date ->
                        studentRepository.syncLocalRegisterations(date)
                        Log.d("registration","Sync Completed");
                        Log.d("registration", "dates : ${AttendanceDataStore.getDates.first()}")
                        AttendanceDataStore.removeDate(date)
                        Log.d("registration","for ${date} its synced and data removed.")
                    }
                }
            } catch (e: Exception) {
                // Handle any errors that occur during the sync process
                e.printStackTrace()
            } finally {
                // Ensure syncing is set to false even if an error occurs
                _syncing.update { false }
            }
        }
    }
}

