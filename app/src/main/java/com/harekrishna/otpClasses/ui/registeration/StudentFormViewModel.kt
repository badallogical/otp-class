package com.harekrishna.otpClasses.ui.registeration

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.api.ApiService
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.local.repos.StudentRepository
import com.harekrishna.otpClasses.data.models.StudentDTO
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

data class StudentFormUiState(
    var name: String = "",
    var phone: String = "",
    var facilitator: String = "NA",
    var batch: String = "DYS",
    var profession: String = "student",
    var address: String = "na",
    var showDropdownFacilitator: Boolean = false,
    var showDropdownBatch: Boolean = false,
    var showSuccessDialog: Boolean = false,
    var isSubmitting: Boolean = false,
    var showPhoneDialog: Boolean = false,
    var showStudentNotFoundDialog: Boolean = false,
    var showDataFetchedToast: Boolean = false,
    var updated: Boolean = false,
    var response: okhttp3.Response? = null,
    var isSuccessfull : Boolean = false,
    var isInvited : Boolean = false,
)


class StudentFormViewModel(private val studentRepository: StudentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentFormUiState())
    val uiState: StateFlow<StudentFormUiState> = _uiState.asStateFlow()

    lateinit var userName: String
    lateinit var userPhone: String

    init {
        viewModelScope.launch {
            val userData = AttendanceDataStore.getUserData().first() // Get the first emitted value
            userName = userData.first ?: "Rajiva Prabhu Ji"
            userPhone = userData.second ?: "+919807726801"
        }

    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository =
                    application.container.studentRepository // Assuming container contains the repository
                StudentFormViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }
    }


    private fun registerStudent(student: StudentDTO, updated: Boolean = false) {
        viewModelScope.launch {
            // Set isSubmitting to true before starting the registration process
            _uiState.update { current ->
                current.copy(isSubmitting = true)
            }

            try {
                // Switch to the I/O dispatcher for I/O-bound operations (e.g., inserting into local database)
                withContext(Dispatchers.IO) {
                    studentRepository.insertStudent(student, uiState.value.isInvited)
                }

                // Update UI state to reflect that the submission is successful
                _uiState.update { current ->
                    current.copy(isSubmitting = false, isSuccessfull = true)
                }

                // Now, perform the remote sync operation in a separate thread without blocking the UI
                // We don't wait for the result of the remote sync before proceeding
                launch(Dispatchers.IO) {
                    try {
                        // Simulate the remote request (for example, syncing to a remote server)
                        studentRepository.syncStudent(student,updated)
                        studentRepository.updateStudentToSynced(student.phone)
                    } catch (e: Exception) {
                        // Log or handle any errors related to remote sync
                        Log.e("RemoteSync", "Failed to sync student to remote", e)
                    }
                }

            } catch (e: Exception) {
                // If there's an error during the local registration process, update UI accordingly
                _uiState.update { current ->
                    current.copy(isSubmitting = false, isSuccessfull = false)
                }
                Log.e("RegisterStudent", "Failed to register student", e)
            }
        }
    }


    fun onDismissPhoneDialog(){
        _uiState.update { current ->
            current.copy( showPhoneDialog = false )
        }
    }

    fun onStudentNotFound(){
        _uiState.update { current ->
            current.copy( showStudentNotFoundDialog = false )
        }
    }

    fun onDataFetched(){
        _uiState.update{ current ->
            current.copy( showDataFetchedToast = false )
        }
    }

    fun onFetchStudentByPhone(phoneInput : String ){
        _uiState.update { current ->
            current.copy( showPhoneDialog = false)
        }

        viewModelScope.launch {
            val student = withContext(Dispatchers.IO) {
                ApiService.findStudentByPhone(phoneInput)
            }
            if (student != null) {
                Log.d("Student form", student.toString())
                _uiState.update { current ->
                    current.copy(
                        name = student.name ,
                        phone = student.phone,
                        facilitator = student.facilitator,
                        batch = student.batch,
                        profession = student.profession,
                        address = student.address,
                        showDataFetchedToast = true
                    )
                }
            } else {
                _uiState.update{ current ->
                    current.copy( showStudentNotFoundDialog = true )
                }
            }
        }
    }

    fun onEditRegistration() {
        _uiState.update { current ->
            current.copy(showPhoneDialog = true, updated = true)
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { current ->
            current.copy(name = newName)
        }
    }

    fun onPhoneChange(newPhone: String) {
        _uiState.update { current ->
            current.copy(phone = newPhone)
        }
    }

    fun onProfessionChange(newProfession: String) {
        _uiState.update { current ->
            current.copy(profession = newProfession)
        }
    }

    fun onAddressChange(newAddress: String) {
        _uiState.update { current ->
            current.copy(address = newAddress)
        }
    }


    fun onFacilitatorChange(newFacilitator: String) {
        _uiState.update { current ->
            current.copy(facilitator = newFacilitator, showDropdownFacilitator = false)
        }
    }

    fun onDropDownFacilitator() {
        _uiState.update { current ->
            current.copy(showDropdownFacilitator = !current.showDropdownFacilitator)
        }
    }

    fun onDismissFacilitator() {
        _uiState.update { current ->
            current.copy(showDropdownFacilitator = false)
        }
    }

    fun onBatchChange(newbatch: String) {
        _uiState.update { current ->
            current.copy(batch = newbatch, showDropdownBatch = false)
        }
    }

    fun onDropDownBatch() {
        _uiState.update { current ->
            current.copy(showDropdownBatch = !current.showDropdownBatch)
        }
    }

    fun onDismissBatch() {
        _uiState.update { current ->
            current.copy(showDropdownBatch = false)
        }
        resetFormState()
    }

    private fun resetFormState() {
        _uiState.update {
            StudentFormUiState()  // Resets all fields to their default values
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSubmit() {
        _uiState.update { current -> current.copy(isSubmitting = true) }

        val currentDate =
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        val student = StudentDTO(
            uiState.value.name,
            uiState.value.phone,
            uiState.value.facilitator,
            uiState.value.batch,
            uiState.value.profession,
            uiState.value.address,
            currentDate,
            userPhone
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                registerStudent(student, updated = uiState.value.updated)

                AttendanceDataStore.addDate(student.date)
                Log.d("registration","Added date to data store")
                Log.d("registration","dates ${AttendanceDataStore.getDates.first()}")
            }

            _uiState.update { current ->
                current.copy(isSubmitting = false, showSuccessDialog = true, isSuccessfull = true)
            }
        }

    }

    fun onDismissSuccessDialog(){
        _uiState.update { current ->
            current.copy( showSuccessDialog = false)
        }

        resetFormState()
    }

    fun onSuccessDialog(){
        _uiState.update { current ->
            current.copy( showSuccessDialog = true)
        }
    }


    fun sendWhatsAppMessage(
        context: Context,
        phoneNumber: String,
        name: String
    ) {

       val phone = formatPhoneNumber(phoneNumber)

        val message = """
    Hare Krishna *${name.toCamelCase()} Prabhu Ji* 🙏
    
    Thanks for your registration for ISKCON Youth Forum (IYF) classes, it's a life-changing step to discover yourself and unleash your true potential. 💯
    
    📢 *We invite you to the Sunday Program*:
    🕒 *Timing*: 4:30 PM, this Sunday
    🎉  *Event*: Seminar 🧑‍💻🗣️, Kirtan 🎤, Music 🎸 and Delicious Prasadam 🍛🍰
    
    🏛️ *Venue*: ISKCON Temple, Lucknow
    
    Hare Krishna Prabhu Ji 🙏
    ${userName} Prabhu
    📞 *Contact*: ${userPhone} 
    (Please save this number)
""".trimIndent()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/${phone}?text=$message")
        context.startActivity(intent)

        _uiState.update{ current ->
            current.copy(isInvited = true)
        }

    }

    fun formatPhoneNumber(phone: String): String {
        // Check if the phone number starts with the country code +91
        return if (phone.startsWith("+91")) {
            phone  // Already has the correct country code
        } else {
            "+91${phone.filter { it.isDigit() }}" // Prepend +91 and remove any non-digit characters
        }
    }


    fun String.toCamelCase(): String {
        return this.lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }


}