package com.example.otp_class_app.ui.registeration

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.data.local.db.dao.RegistrationCount
import com.example.otp_class_app.data.local.repos.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel(private val studentRepository: StudentRepository) : ViewModel() {
    // Mutable state flow directly holding the list of registrations
    private val _registrations = MutableStateFlow<List<RegistrationCount>>(emptyList())

    // Publicly exposed immutable state
    val registrations: StateFlow<List<RegistrationCount>> = _registrations.asStateFlow()

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
            val registrationList = studentRepository.getRegistrationList()

            // update the state
            if( registrationList != null ) {
                _registrations.update { current ->
                    registrationList
                }
            }
        }
    }
}

