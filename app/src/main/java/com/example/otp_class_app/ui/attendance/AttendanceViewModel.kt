package com.example.otp_class_app.ui.attendance

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
import com.example.otp_class_app.data.models.AttendancePOJO
import com.example.otp_class_app.data.models.StudentDTO
import com.example.otp_class_app.data.models.StudentPOJO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendanceViewModel(private val studentRepository: StudentRepository) : ViewModel(){
    private var _uiState = MutableStateFlow(AttendanceUiState())
    val uiState : StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository = application.container.studentRepository // Assuming container contains the repository
                AttendanceViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }
    }

    // Function to filter students based on search query
    fun filterStudents() {
        val currentState = _uiState.value
        val query = currentState.searchQuery

        // Filter the students based on the query
        val filtered = currentState.students.filter {
            it.phone.contains(query, ignoreCase = true)
        }

        // Update the UI state with the new filtered students and query
        _uiState.value = currentState.copy(
            filteredStudents = filtered
        )
    }

    // Function to fetch students and update UI state
    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchStudents(filter : Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val fetchedStudents = studentRepository.getAllStudents()

            _uiState.value = fetchedStudents?.let {
                _uiState.value.copy(
                    students = it,
                    filteredStudents = if( !filter ) {
                        fetchedStudents
                    }else { fetchedStudents.filter {
                        it.phone.contains(_uiState.value.searchQuery, ignoreCase = true)
                    } },
                    isLoading = false
                )
            }!!
        }
    }



    // Function to fetch students from API and filter them
    @RequiresApi(Build.VERSION_CODES.O)
    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            studentRepository.syncStudentData()
            _uiState.value = _uiState.value.copy(isLoading = false)
            fetchStudents(true)
        }
    }

    // Function to update the search query and trigger filtering
    fun onSearchQueryChanged(newQuery: String) {
        _uiState.update { currentState ->
            currentState.copy(searchQuery = newQuery)
        }

        // Re-filter students based on the updated query
        filterStudents()
    }

    fun onClickQuickRegisteration(){
        _uiState.update { currentState ->
            currentState.copy(showRegistrationDialog = true)
        }
    }

    fun onStudentItemClicked(student: StudentPOJO){
        _uiState.update { currentState ->
            currentState.copy(
                selectedStudent = student,
                showDialog = true
            )
        }
    }

    fun onDismissAttendanceDialog(){
        _uiState.update { state ->
            state.copy(
                showDialog = false,
                showCongratsAfterPosting = false
            )
        }
    }

    fun onDismissRegisterationDialog(){
        _uiState.update { state ->
            state.copy(
                showRegistrationDialog = false
            )
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onRegisterStudent(name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegistering = true)


            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val student = StudentDTO(name, phone, "NA", "NA", "NA", "NA", currentDate,"NA")

            try {
                // Perform the network call on IO thread
                studentRepository.insertStudent(student)

                // Update the UI after successful registration
                _uiState.value = _uiState.value.copy(
                    isRegistering = false,
                    showRegistrationDialog = false // Close registration dialog
                )
            } catch (e: Exception) {
                // Handle registration failure, e.g., log the error or update the UI
                _uiState.value = _uiState.value.copy(
                    isRegistering = false
                )
                Log.e("AttendanceViewModel", "Error registering student: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentOrNextSunday(): String {
        // Get the current date
        var currentDate = LocalDate.now()

        // Check if the current day is Saturday
        if (currentDate.dayOfWeek == DayOfWeek.SATURDAY) {
            // Update to the next day (Sunday)
            currentDate = currentDate.plusDays(1)
        }else if( currentDate.dayOfWeek != DayOfWeek.SUNDAY){
            return ""
        }

        // Format the date in the required format "YYYY-MM-DD"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return currentDate.format(formatter)
    }

    fun postAttendance(student: StudentPOJO) {
        viewModelScope.launch {
            // Update UI state to show that submission is in progress
            _uiState.value = _uiState.value.copy(isPostingAttendance = true)

            val currentDate = "2024-01-07"
//   val currentDate = getCurrentOrNextSunday()
            //val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val attendance = AttendancePOJO(student.phone, currentDate, student.name)

            try {
                // Save attendance locally using the data store
                withContext(Dispatchers.IO) {
                    AttendanceDataStore.saveNewAttendance(attendance)
                }

                delay(500)

                // Update UI state after submission completes
                _uiState.value = _uiState.value.copy(
                    isPostingAttendance = false,
                    showCongratsAfterPosting = true // If successful, show congratulations
                )
            } catch (e: Exception) {
                // Handle submission failure
                _uiState.value = _uiState.value.copy(isPostingAttendance = false)
                Log.e("AttendanceViewModel", "Error posting attendance: ${e.message}")
            }
        }
    }

}