package com.example.otp_class_app.ui.registeration

import android.os.Build
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationViewModel(private val studentRepository: StudentRepository) : ViewModel() {
    // Mutable state flow directly holding the list of registrations
    private val _registrations = MutableStateFlow<List<RegistrationStatus>>(emptyList())

    private val _syncing = MutableStateFlow<Boolean>(false)

    // Publicly exposed immutable state
    val registrations: StateFlow<List<RegistrationStatus>> = _registrations.asStateFlow()

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

    // Function to fetch registration data and update the registrations state
    @RequiresApi(Build.VERSION_CODES.O)
    fun getRegistration() {
        viewModelScope.launch {
            // Fetch the registration list from the repository
            val registrationList = studentRepository.getRegistrationList()

            // Map each registration to fetch the sync status asynchronously
            registrationList?.map { registration ->
                async {
                    val syncStatus = AttendanceDataStore.dateExists(registration.date)
                    registration.copy(synced = !syncStatus)
                }
            }?.awaitAll() ?: emptyList()

            // update the state
            if( registrationList != null ) {
                _registrations.update { current ->
                    registrationList
                }
            }
        }
    }

    fun syncRegistrations(){
        _syncing.value = true

        viewModelScope.launch {
            val dates = AttendanceDataStore.getDates();

            withContext(Dispatchers.IO){
                dates.forEach { date ->
                    studentRepository.syncLocalRegisterations(date)
                    AttendanceDataStore.removeDate(date)
                }
            }

           withContext(Dispatchers.Main){
               _syncing.value = false
           }
        }

    }
}

