package com.example.otp_class_app.ui.registeration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.data.local.repos.CallingReportRepository
import com.example.otp_class_app.data.models.CallingReportPOJO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


data class CallingListUiState(
    val registrations: List<CallingReportPOJO> = emptyList(),
    val updatingStatus: Boolean = false
)

class CallingListViewModel(private val callingReportRepository: CallingReportRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(CallingListUiState())
    val uiState: StateFlow<CallingListUiState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository =
                    application.container.callingReportRepository // Assuming container contains the repository
                CallingListViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }
    }


    fun getCallingRegistrations(date: String = "") {
        viewModelScope.launch {
            callingReportRepository.getCallingReportsByDate(date).collect { callingReports ->
                _uiState.update { current ->
                    current.copy(
                        registrations = callingReports ?: emptyList(),
                    )
                }
            }
        }
    }

    fun updateStudentStatus(phone: String, status: String) {

        val viewStatus = status.split(",").firstOrNull()?.trim() ?: status


        // Update the UI, registrations list with the new status
        _uiState.value = _uiState.value.copy(
            registrations = _uiState.value.registrations.map { student ->
                if (student.phone == phone) student.copy(status = viewStatus) else student
            }
        )

        // update the database
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                callingReportRepository.updateCallingReportStatus(phone, status)
            }
        }
    }

}