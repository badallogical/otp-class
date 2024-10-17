package com.harekrishna.otpClasses.ui.registeration

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.local.repos.StudentRepository
import com.harekrishna.otpClasses.data.models.RegistrationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
class RegistrationViewModel(private val studentRepository: StudentRepository) : ViewModel() {

    // Mutable state flow holding the list of registrations
    private val _registrations = MutableStateFlow<List<RegistrationStatus>>(emptyList())

    // Publicly exposed immutable state
    val registrations: StateFlow<List<RegistrationStatus>> = _registrations.asStateFlow()

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository =
                    application.container.studentRepository // Assuming container contains the repository
                RegistrationViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }
    }

    init {
        viewModelScope.launch {
            getRegistration() // Fetch registrations when ViewModel is created
            Log.d("Registration", "ViewModel Init Registration called")
        }
    }

        // get all the registrations from the local
        @RequiresApi(Build.VERSION_CODES.O)
        fun getRegistration() {
            viewModelScope.launch {

                // fetch the registrations from the local <registration status>
                studentRepository.getRegistrationList().collect { registrationList ->
                    _registrations.value = registrationList ?: emptyList() // Update state
                    Log.d("Registrations","Registrations loaded  ${registrations.value.size}")
                }
            }
        }

        // Sync the local registrations to Remote.
        fun syncRegistrations() {
            // Set syncing to true at the start
            _syncing.value = true

            viewModelScope.launch {
                try {
                    // Fetch dates in IO dispatcher
                    val dates = withContext(Dispatchers.IO) {
                        AttendanceDataStore.getDates.first() // Use first() safely
                    }

                    Log.d("registrations", "to sync dates: $dates")

                    // Sync local registrations
                    withContext(Dispatchers.IO) {
                        dates.forEach { date ->
                            studentRepository.syncLocalRegistrations(date) // Sync registrations for each date
                            Log.d("registration", "Sync Completed for date: $date")
                            AttendanceDataStore.removeDate(date) // Remove synced date
                        }

                        // update registrations
                        getRegistration()
                    }
                } catch (e: Exception) {
                    // Handle any errors that occur during the sync process
                    e.printStackTrace()
                } finally {
                    // Ensure syncing is set to false even if an error occurs
                    _syncing.value = false
                }
            }
        }
    }

