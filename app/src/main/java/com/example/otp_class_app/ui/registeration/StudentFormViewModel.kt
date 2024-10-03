package com.example.otp_class_app.ui.registeration

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
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.data.api.ApiService
import com.example.otp_class_app.data.api.AttendanceDataStore
import com.example.otp_class_app.data.local.repos.StudentRepository
import com.example.otp_class_app.data.models.StudentDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class StudentFormUiState(
    var name: String = "",
    var phone: String = "",
    var facilitator: String = "Select Facilitator",
    var batch: String = "Select Batch",
    var profession: String = "",
    var address: String = "",
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
)


class StudentFormViewModel(private val studentRepository: StudentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentFormUiState())
    val uiState: StateFlow<StudentFormUiState> = _uiState.asStateFlow()

    lateinit var userName: String
    lateinit var userPhone: String

    init {
        viewModelScope.launch {
            AttendanceDataStore.getUserData().collect { userData ->
                userName = userData.first ?: "Rajiva Prabhu Ji"
                userPhone = userData.second ?: "+919807726801"
            }
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

    fun registerStudent(student: StudentDTO, updated: Boolean = false) {
        viewModelScope.launch {
            // Set isSubmitting to true before starting the registration process
            _uiState.update { current ->
                current.copy(isSubmitting = true)
            }

            try {
                // Switch to the I/O dispatcher for I/O-bound operations
                withContext(Dispatchers.IO) {
                    studentRepository.insertStudent(student, updated)
                }

                // Switch back to the main thread for UI updates
                _uiState.update { current ->
                    current.copy(isSubmitting = false, isSuccessfull = true)
                }

            } catch (e: Exception) {
                // Handle the error if something goes wrong
                _uiState.update { current ->
                    current.copy(isSubmitting = false)
                }
                // Optionally, log or display error feedback
                e.printStackTrace()
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
            userName
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                studentRepository.insertStudent(student, updated = uiState.value.updated)
                AttendanceDataStore.addDate(student.date)
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
    }

    fun onSuccessDialog(){
        _uiState.update { current ->
            current.copy( showSuccessDialog = true)
        }
    }


    fun sendWhatsAppMessage(
        context: Context,
        phoneNumber: String,
        name: String,
        contact: String
    ) {
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
        intent.data = Uri.parse("https://wa.me/$phoneNumber?text=$message")
        context.startActivity(intent)
    }

    fun String.toCamelCase(): String {
        return this.lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }


}