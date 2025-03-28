package com.harekrishna.otpClasses.ui.registeration

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
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
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class StudentFormUiState(
    val photoUri: Uri? = null,
    var name: String = "",
    var phone: String = "",
    var facilitator: String = "NA",
    var batch: String = "OTP",
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
    var regData: String = "",
    var regBy: String = ""
)


class StudentFormViewModel(private val studentRepository: StudentRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentFormUiState())
    val uiState: StateFlow<StudentFormUiState> = _uiState.asStateFlow()

    lateinit var userName: String
    lateinit var userPhone: String
    lateinit var welcomeMsg: String

    var tempPhotoUri: Uri? = null

    init {
        viewModelScope.launch {
            val userData = AttendanceDataStore.getUserData().first() // Get the first emitted value
            userName = userData.first ?: "Rajiva Prabhu Ji"
            userPhone = userData.second ?: "+919807726801"

            welcomeMsg = AttendanceDataStore.getWelcomeMessage()
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
                _uiState.update { current ->
                    current.copy(
                        name = student.name ,
                        phone = student.phone,
                        facilitator = student.facilitator,
                        batch = student.batch,
                        profession = student.profession,
                        address = student.address,
                        photoUri = student.photoUri?.toUri(),
                        showDataFetchedToast = true,
                        regData = student.date,
                        regBy = student.by
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
    fun onUpdate(){
        _uiState.update { current -> current.copy(isSubmitting = true, updated = true) }


//        val currentDate = "2024-12-31"

        val student = StudentDTO(
            uiState.value.name,
            uiState.value.phone,
            uiState.value.facilitator,
            uiState.value.batch,
            uiState.value.profession,
            uiState.value.address,
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
            uiState.value.address,
            currentDate,
            userPhone,
            photoUri = uiState.value.photoUri.toString()
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

        val greeting = "Hello \uD83D\uDC90\uD83D\uDC90\uD83D\uDC90";
        val footer = "\uD83C\uDFDB *Venue*: ISKCON Youth Forum Seminar Hall, ISKCON Temple, Sushant Golf City, Lko\n\n*Contact*: 9807726801,6307444507 \n(Please save this number)\n\nRegards,\n" + "ISKCON Youth Forum"
        val message = greeting + "\n\n" + welcomeMsg + "\n\n" + footer;


        // TODO: if whatsapp business is their then open whatsapp only.
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.data = Uri.parse("https://wa.me/${phone}?text=${message.trimIndent()}")
//        context.startActivity(intent)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$phone?text=${Uri.encode(message.trimIndent())}")
            setPackage("com.whatsapp") // Ensures only WhatsApp (not Business) opens
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
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



    // Function to create a file in app-specific external storage
    fun createImageFile(context: Context): Uri {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile(
            "IMG_${System.currentTimeMillis()}", // File name
            ".jpg",                              // File extension
            storageDir                           // Directory
        )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Replace with your app's provider authority
            file
        )
    }


}