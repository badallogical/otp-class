package com.example.otp_class_app.ui.registeration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegistrationViewModel : ViewModel() {
    // Mutable state flow directly holding the list of registrations
    private val _registrations = MutableStateFlow<List<Pair<String, Int>>>(emptyList())

    // Publicly exposed immutable state
    val registrations: StateFlow<List<Pair<String, Int>>> = _registrations.asStateFlow()

    init {
        viewModelScope.launch {
            getRegistration() // Fetch initial registration data
        }
    }

    // Function to fetch registration data and update the registrations state
    private fun getRegistration() {
        // Simulating a data fetch (you would replace this with your actual data source)
        val dummyRegistrations = listOf(
            "2024-09-01" to 10,
            "2024-09-02" to 15,
            "2024-09-03" to 8,
            "2024-09-04" to 12,
            "2024-09-05" to 20
        )

        // Update the registrations state with the fetched data
        _registrations.value = dummyRegistrations
    }
}

