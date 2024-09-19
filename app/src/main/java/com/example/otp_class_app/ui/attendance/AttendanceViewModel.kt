package com.example.otp_class_app.ui.attendance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otp_class_app.data.api.ApiService
import com.example.otp_class_app.data.api.AttendanceDataStore
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendanceViewModel : ViewModel(){
    private var _uiState = MutableStateFlow(AttendanceUiState())
    val uiState : StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    // Function to filter students based on search query
    fun filterStudents() {
        val currentState = _uiState.value
        val query = currentState.searchQuery

        // Filter the students based on the query
        val filtered = currentState.students?.filter {
            it.phone.contains(query, ignoreCase = true)
        } ?: emptyList()

        // Update the UI state with the new filtered students and query
        _uiState.value = currentState.copy(
            filteredStudents = filtered
        )
    }

    // Function to fetch students and update UI state
    fun fetchStudents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val fetchedStudents = ApiService.getStudents()

            _uiState.value = _uiState.value.copy(
                students = fetchedStudents,
                filteredStudents = fetchedStudents ?: emptyList(),
                isLoading = false
            )
        }
    }

    // Function to fetch students from API and filter them
    fun onRefresh() {
        fetchStudents()
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


    fun onRegisterStudent(name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegistering = true)

            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val student = StudentDTO(name, phone, "NA", "NA", "NA", "NA", currentDate)

            try {
                // Perform the network call on IO thread

                ApiService.registerStudent(student)

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


    fun postAttendance(student: StudentPOJO) {
        viewModelScope.launch {
            // Update UI state to show that submission is in progress
            _uiState.value = _uiState.value.copy(isPostingAttendance = true)

            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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