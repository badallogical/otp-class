package com.harekrishna.otpClasses.ui.registration2

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.harekrishna.otpClasses.MyApplication.Companion.toCamelCase
import com.harekrishna.otpClasses.domain.model.InterestLevel
import com.harekrishna.otpClasses.domain.model.Student
import com.harekrishna.otpClasses.domain.model.StudentCategory
import com.harekrishna.otpClasses.domain.model.StudentStatus
import com.harekrishna.otpClasses.domain.repository.RegistrationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PhoneCheckStatus {
    IDLE,
    CHECKING,
    EXISTING,
    NEW
}

enum class RegDialogState {
    NONE,
    SENDING_INVITE,
    SUCCESS
}

data class RegistrationUiState(
    val photoUri: Uri? = null,
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val education: String = "",
    val occupation: String = "Student",
    val category: StudentCategory = StudentCategory.YOUTH,
    val interestLevel: InterestLevel = InterestLevel.MEDIUM,

    // Status & Flow controls
    val phoneCheckStatus: PhoneCheckStatus = PhoneCheckStatus.IDLE,
    val existingStudentId: String? = null,
    val dialogState: RegDialogState = RegDialogState.NONE,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,

    // Captures generated/updated document ID
    val registeredUserID: String? = null
) {
    val isExistingStudent: Boolean
        get() = phoneCheckStatus == PhoneCheckStatus.EXISTING

    val isValid: Boolean
        get() = name.isNotBlank() && phone.filter { it.isDigit() }.length >= MIN_PHONE_DIGITS && address.isNotBlank()

    companion object {
        const val MIN_PHONE_DIGITS = 10
    }
}

@HiltViewModel
class RegistrationScreenViewModel @Inject constructor(
    private val repository: RegistrationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    private var phoneCheckJob: Job? = null

    companion object {
        private const val PHONE_DEBOUNCE_MS = 600L
        private const val MIN_PHONE_DIGITS = 10
    }

    // Direct state updates for form controls
    fun onPhotoChange(uri: Uri?) = update { it.copy(photoUri = uri) }
    fun onNameChange(v: String) = update { it.copy(name = v, errorMessage = null) }
    fun onAddressChange(v: String) = update { it.copy(address = v, errorMessage = null) }
    fun onEducationChange(v: String) = update { it.copy(education = v) }
    fun onOccupationChange(v: String) = update { it.copy(occupation = v) }
    fun onCategoryChange(v: StudentCategory) = update { it.copy(category = v) }
    fun onInterestLevelChange(v: InterestLevel) = update { it.copy(interestLevel = v) }

    fun onPhoneChange(v: String) {
        phoneCheckJob?.cancel()

        update {
            it.copy(
                phone = v,
                errorMessage = null,
                phoneCheckStatus = PhoneCheckStatus.IDLE,
                existingStudentId = null
            )
        }

        val digits = v.filter { it.isDigit() }
        if (digits.length < MIN_PHONE_DIGITS) return

        phoneCheckJob = viewModelScope.launch {
            delay(PHONE_DEBOUNCE_MS)
            update { it.copy(phoneCheckStatus = PhoneCheckStatus.CHECKING) }

            repository.fetchStudentByPhone(digits)
                .onSuccess { existing ->
                    if (existing != null) {
                        // Autofill state with existing student details
                        update {
                            it.copy(
                                phoneCheckStatus = PhoneCheckStatus.EXISTING,
                                existingStudentId = existing.id,
                                name = existing.name,
                                address = existing.address ?: "",
                                education = existing.education ?: "",
                                occupation = existing.occupation ?: "Student",
                                category = existing.category,
                                interestLevel = existing.interestLevel ?: InterestLevel.MEDIUM
                            )
                        }
                    } else {
                        // Mark as NEW without overriding what the user typed in other fields
                        update {
                            it.copy(
                                phoneCheckStatus = PhoneCheckStatus.NEW,
                                existingStudentId = null
                            )
                        }
                    }
                }
                .onFailure { error ->
                    update {
                        it.copy(
                            phoneCheckStatus = PhoneCheckStatus.IDLE,
                            errorMessage = "Failed to verify phone: ${error.localizedMessage}"
                        )
                    }
                }
        }
    }

    fun onRegisterClick() {
        val state = _uiState.value
        if (!state.isValid) {
            update { it.copy(errorMessage = "Name, phone, and address are required.") }
            return
        }
        update { it.copy(dialogState = RegDialogState.SENDING_INVITE, errorMessage = null) }
    }

    fun onCancelSendingDialog() {
        update {
            it.copy(
                dialogState = RegDialogState.NONE,
                errorMessage = "Please double-check the details, then tap Register again."
            )
        }
    }

    /** Called when the admin confirms WhatsApp message was sent & verified */
    fun onVerifyAndSubmit() {
        submitStudent(isWhatsappVerified = true)
    }

    /** Called when proceeding without WhatsApp verification (valid phone only) */
    fun onNotVerifyAndSubmit() {
        submitStudent(isWhatsappVerified = false)
    }

    /** Private core submit handler to keep logic in one place */
    private fun submitStudent(isWhatsappVerified: Boolean) {
        viewModelScope.launch {
            // Set loading state and get the latest state snapshot safely
            val currentState = _uiState.updateAndGet { it.copy(isSubmitting = true) }

            if (currentState.isExistingStudent) {
                submitExistingStudentUpdate(
                    state = currentState,
                    isWhatsappVerified = isWhatsappVerified
                )
            } else {
                val digits = currentState.phone.filter { it.isDigit() }
                val newStudent = Student(
                    name = currentState.name.trim().toCamelCase(),
                    phone = digits,
                    address = currentState.address.trim().toCamelCase(),
                    education = currentState.education.trim().ifEmpty { null }?.toCamelCase(),
                    occupation = currentState.occupation.trim().ifEmpty { null }?.toCamelCase(),
                    category = currentState.category,
                    interestLevel = currentState.interestLevel,
                    status = StudentStatus.ACTIVE,
                    joinedOn = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    isPhoneWhatsappVerified = isWhatsappVerified
                )
                submitNewStudentRegistration(newStudent)
            }
        }
    }

    private suspend fun submitExistingStudentUpdate(state: RegistrationUiState, isWhatsappVerified : Boolean = false ) {
        val studentId = state.existingStudentId ?: return

        // Clean map representation for document update
        val updates = mapOf<String, Any?>(
            "name" to state.name.trim(),
            "address" to state.address.trim(),
            "education" to state.education.trim().ifEmpty { null },
            "occupation" to state.occupation.trim().ifEmpty { null },
            "category" to state.category.name,
            "interestLevel" to state.interestLevel.name,
            "status" to StudentStatus.ACTIVE.name,
            "inactiveReason" to null,
            "updatedAt" to Timestamp.now(),
            "rejoinedOn" to Timestamp.now(),
            "isPhoneWhatsappVerified" to isWhatsappVerified
        )

        repository.updateStudent(studentId, updates)
            .onSuccess {
                update {
                    it.copy(
                        dialogState = RegDialogState.SUCCESS,
                        registeredUserID = studentId,
                        isSubmitting = false
                    )
                }
            }
            .onFailure { error ->
                update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Update failed: ${error.localizedMessage}"
                    )
                }
            }
    }

    private suspend fun submitNewStudentRegistration(newStudent: Student) {
        repository.registerStudent(newStudent)
            .onSuccess { generatedId ->
                update {
                    it.copy(
                        dialogState = RegDialogState.SUCCESS,
                        registeredUserID = generatedId,
                        isSubmitting = false
                    )
                }
            }
            .onFailure { error ->
                update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Registration failed: ${error.localizedMessage}"
                    )
                }
            }
    }

    fun onRegisterAnother() {
        phoneCheckJob?.cancel()
        _uiState.update { RegistrationUiState() }
    }

    private inline fun update(block: (RegistrationUiState) -> RegistrationUiState) = _uiState.update(block)
}