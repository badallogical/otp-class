package com.harekrishna.otpClasses.ui.events

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.data.api.ApiService
import com.harekrishna.otpClasses.data.models.SangkirtanStudentDTO
import com.harekrishna.otpClasses.data.sources.repos.AttendancePreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.MessageType
import com.harekrishna.otpClasses.data.sources.repos.SangkirtanStudentRepository
import com.harekrishna.otpClasses.data.sources.repos.UserPreferencesRepository
import com.harekrishna.otpClasses.domain.PrepareWhatsappMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HarinaamFormUiState(
    val name: String = "",
    val phone: String = "",
    val category: String = "Youth",
    val location: String = "",
    val showDropdown: Boolean = false,
    val isSubmitting: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val isInvited: Boolean = false
)

@HiltViewModel
class HarinaamFormViewModel @Inject constructor(
    private val repository: SangkirtanStudentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val attendancePreferencesRepository: AttendancePreferencesRepository,
    private val prepareWhatsappMessageUseCase: PrepareWhatsappMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HarinaamFormUiState())
    val uiState: StateFlow<HarinaamFormUiState> = _uiState.asStateFlow()

    private var userPhone: String = "+919807726801"

    init {
        viewModelScope.launch {
            val userData = userPreferencesRepository.getUserData().first()
            userPhone = userData.second ?: "+919807726801"
        }
    }

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name) }
    fun onPhoneChange(phone: String) = _uiState.update { it.copy(phone = phone) }
    fun onCategoryChange(category: String) = _uiState.update { it.copy(category = category, showDropdown = false) }
    fun onLocationChange(location: String) = _uiState.update { it.copy(location = location) }
    fun toggleDropdown() = _uiState.update { it.copy(showDropdown = !it.showDropdown) }
    fun onInvited() = _uiState.update { it.copy(isInvited = true) }

    private fun registerStudent(student: SangkirtanStudentDTO, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                // Local first
                withContext(Dispatchers.IO) {
                    repository.insertStudent(student)
                    attendancePreferencesRepository.addDate(student.date)
                }
                
                // Update UI state
                _uiState.update { it.copy(isSubmitting = false, showSuccessDialog = true) }
                onSuccess()

                // Background sync
                launch(Dispatchers.IO) {
                    try {
                        val response = ApiService.registerSangkirtanStudent(student, updated = false)
                        if (response.isSuccessful) {
                            repository.updateStudentToSynced(student.phone)
                        }
                    } catch (e: Exception) {
                        Log.e("RemoteSync", "Failed to sync student to remote", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("HarinaamFormViewModel", "Error submitting", e)
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun submitRegistration(onSuccess: () -> Unit) {
        val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val student = SangkirtanStudentDTO(
            name = _uiState.value.name,
            phone = _uiState.value.phone,
            category = _uiState.value.category,
            location = _uiState.value.location,
            date = currentDate,
            byDev = userPhone
        )

        registerStudent(student, onSuccess)
    }

    suspend fun getWhatsAppMessage(phoneNumber: String, type: MessageType): String {
        return prepareWhatsappMessageUseCase(phoneNumber, type)
    }

    fun onDismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false, name = "", phone = "", location = "", isInvited = false) }
    }
}
