package com.example.otp_class_app.ui.registeration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistrationReport(
    val name: String,
    val phone: String,
    val status: String = "status"
)

data class CallingListUiState(
    val registrations : List<RegistrationReport> = emptyList(),
    val updateStatusClicked : Boolean = false
)

class CallingListViewModel : ViewModel(){
    private val _uiState = MutableStateFlow(CallingListUiState())
    val uiState : StateFlow<CallingListUiState> = _uiState.asStateFlow()

    init{
        viewModelScope.launch{
            getCallingList()
        }
    }

    fun getCallingList(date : String = ""){
        val dummyRegistrationReports = listOf(
            RegistrationReport(name = "John Doe", phone = "123-456-7890", status = "yes"),
            RegistrationReport(name = "Jane Smith", phone = "987-654-3210", status = "no"),
            RegistrationReport(name = "Emily Johnson", phone = "555-123-4567", status = "will try"),
            RegistrationReport(name = "Michael Brown", phone = "444-987-6543", status = "yes"),
            RegistrationReport(name = "Sarah Davis", phone = "333-222-1111", status = "no")
        )

        _uiState.update{ current ->
            current.copy(registrations = dummyRegistrationReports)
        }
    }

    fun updateStudentStatus(updatedStudent: RegistrationReport) {
        // Update the registrations list with the new status
        _uiState.value = _uiState.value.copy(
            registrations = _uiState.value.registrations.map { student ->

                if (student.phone == updatedStudent.phone) updatedStudent else student
            }
        )
    }

}