package com.harekrishna.otpClasses.ui.registeration

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.data.models.StudentDTO
import com.harekrishna.otpClasses.data.sources.repos.AttendancePreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.MessagePreferencesRepository
import com.harekrishna.otpClasses.data.sources.repos.MessageType
import com.harekrishna.otpClasses.data.sources.repos.StudentRepository
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
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StudentFormUiState(
    val photoUri: Uri? = null,
    var name: String = "",
    var phone: String = "",
    var facilitator: String = "NA",
    var batch: String = "OTP",
    var profession: String = "student",
    var address: String = "na",
    var city : String = "Lucknow",
    var showDropdownFacilitator: Boolean = false,
    var showDropdownBatch: Boolean = false,
    var showDropdownCity : Boolean = false,
    var showSuccessDialog: Boolean = false,
    var isSubmitting: Boolean = false,
    var showPhoneDialog: Boolean = false,
    var showStudentNotFoundDialog: Boolean = false,
    var showDataFetchedToast: Boolean = false,
    var updated: Boolean = false,
    var response: okhttp3.Response? = null,
    var isSuccessfull : Boolean = false,
    var isInvited : Boolean = false,
    var regData: String = "",
    var regBy: String = ""
)

@HiltViewModel
class StudentFormViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val messageRepository: MessagePreferencesRepository,
    private val attendancePreferencesRepository: AttendancePreferencesRepository,
    private val prepareWhatsappMessageUseCase: PrepareWhatsappMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentFormUiState())
    val uiState: StateFlow<StudentFormUiState> = _uiState.asStateFlow()

    lateinit var userName: String
    lateinit var userPhone: String


    var tempPhotoUri: Uri? = null

    init {
        viewModelScope.launch {
            val userData = userPreferencesRepository.getUserData().first() // Get the first emitted value
            userName = userData.first ?: "Rajiva Prabhu Ji"
            userPhone = userData.second ?: "+919807726801"
        }
    }

    fun onPhotoSelected(uri: Uri) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun createImageUri(context: Context): Uri? {
        val imageFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "profile_${System.currentTimeMillis()}.jpg"
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
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

    fun onFetchStudentByPhone(phoneInput : String){
        _uiState.update { current ->
            current.copy( showPhoneDialog = false)
        }

        viewModelScope.launch {
            val student = withContext(Dispatchers.IO) {
                studentRepository.getStudentDTOByPhone(phoneInput)
            }
            if (student != null) {
                Log.d("Student form", student.toString())
                val parts = student.address.split(",")
                val addressOnly = parts.firstOrNull()?.trim() ?: ""
                val cityOnly = parts.getOrNull(1)?.trim() ?: ""

                _uiState.update { current ->
                    current.copy(
                        name = student.name ,
                        phone = student.phone,
                        facilitator = student.facilitator,
                        batch = student.batch,
                        profession = student.profession,
                        address = addressOnly,
                        photoUri = student.photoUri?.toUri(),
                        showDataFetchedToast = true,
                        regData = student.date,
                        regBy = student.byDev?:"na",
                        city = cityOnly
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
            current.copy( updated = true )
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

    fun onDismissCity() {
        _uiState.update { current ->
            current.copy(showDropdownCity = false)
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

    fun onCityChange(city: String) {
        _uiState.update { current ->
            current.copy( city = city, showDropdownCity = false)
        }
    }

    fun onDropDownCity() {
        _uiState.update { current ->
            current.copy(showDropdownCity = !current.showDropdownCity)
        }
    }


    fun onInvited(){
        _uiState.update { current ->
            current.copy(isInvited = true)
        }
    }

    private fun resetFormState() {
        _uiState.update {
            StudentFormUiState()  // Resets all fields to their default values
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onUpdate(){
        _uiState.update { current -> current.copy(isSubmitting = true, updated = true) }


//        val currentDate = "2024-12-31"

        val student = StudentDTO(
            uiState.value.name,
            uiState.value.phone,
            uiState.value.facilitator,
            uiState.value.batch,
            uiState.value.profession,
            address = "${uiState.value.address},${uiState.value.city}",
            uiState.value.regData,
            uiState.value.regBy,
            photoUri = uiState.value.photoUri.toString()
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                studentRepository.updateStudent(student)
                Log.d("registration","Updated")
            }

            _uiState.update { current ->
                current.copy(isSubmitting = false, showSuccessDialog = true, isSuccessfull = true)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSubmit() {
        _uiState.update { current -> current.copy(isSubmitting = true) }

        val currentDate =
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

//        val currentDate = "2024-12-31"

        val student = StudentDTO(
            uiState.value.name,
            uiState.value.phone,
            uiState.value.facilitator,
            uiState.value.batch,
            uiState.value.profession,
            uiState.value.address + "," + uiState.value.city ,
            currentDate,
            userPhone,
            photoUri = uiState.value.photoUri.toString()
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                registerStudent(student, updated = uiState.value.updated)

                attendancePreferencesRepository.addDate(student.date)
                Log.d("registration","Added date to data store")
                Log.d("registration","dates ${attendancePreferencesRepository.dates.first()}")
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


    suspend fun getWhatsAppMessage(
        phoneNumber: String,
        type : MessageType
    ) : String {

        return prepareWhatsappMessageUseCase(phoneNumber, MessageType.WELCOME)

    }


}