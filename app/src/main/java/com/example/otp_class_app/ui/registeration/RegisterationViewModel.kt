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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository = application.container.studentRepository // Assuming container contains the repository
                RegistrationViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }
    }

    init {
        getRegistration() // Fetch registrations when ViewModel is created
        Log.d("Registration","ViewModel Init Registration called")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getRegistration() {
        viewModelScope.launch {
            studentRepository.getRegistrationList()
                .onEach { registrationList ->
                    _registrations.value = registrationList ?: emptyList() // Update state
                }
                .catch { e ->
                    e.printStackTrace() // Handle errors appropriately
                }
                .launchIn(this) // Collect the flow in this coroutine scope
        }
    }

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

