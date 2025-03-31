package com.harekrishna.otpClasses.ui.attendance

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.local.repos.StudentRepository
import com.harekrishna.otpClasses.data.models.AttendancePOJO
import com.harekrishna.otpClasses.data.models.StudentDTO
import com.harekrishna.otpClasses.data.models.StudentPOJO
import com.harekrishna.otpClasses.ui.followup.FollowUpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
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

    lateinit var userPhone:String
    init {
        viewModelScope.launch {
            val userData = withContext(Dispatchers.IO) {
                AttendanceDataStore.getUserData().first() // Fetch user data
            }
            userPhone = userData.second ?: "+919807726801"


            fetchStudents(false) // Load from Room database first
            onRefresh()
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
    fun fetchStudents(filter: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                studentRepository.getAllStudents()
                    .flowOn(Dispatchers.IO) // Ensure DB operations are in IO thread
                    .map { fetchedStudents ->
                        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                        val sortedStudents = fetchedStudents.sortedByDescending {
                            runCatching { LocalDate.parse(it.date, dateFormatter) }
                                .getOrDefault(LocalDate.MIN) // Fallback to MIN date if parsing fails
                        }

                        // Apply filter before updating UI state
                        if (filter) {
                            sortedStudents.filter { it.phone.contains(_uiState.value.searchQuery, ignoreCase = true) }
                        } else {
                            sortedStudents
                        }
                    }
                    .stateIn(viewModelScope) // Keeps the latest state, avoiding multiple launches
                    .collect { finalStudents ->
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                students = finalStudents,
                                filteredStudents = finalStudents,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("fetchStudents", "Error fetching students", e)
                _uiState.value = _uiState.value.copy(isLoading = false) // Ensure UI state is updated
            }
        }
    }



    // Function to fetch students from API and filter them
    @RequiresApi(Build.VERSION_CODES.O)
    fun onRefresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                withContext(Dispatchers.IO) {
                    studentRepository.syncStudentData() // Fetch new data
                }
                fetchStudents(filter = true) // Refresh UI with new data
            } catch (e: Exception) {
                Log.e("onRefresh", "Error syncing students", e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun onStudentItemClicked(student: StudentPOJO){


        if( isTodayWeekend() ){
            _uiState.update { currentState ->
                currentState.copy(
                    selectedStudent = student,
                    showDialog = true
                )
            }
        }else{
            _uiState.update { currentState ->
                currentState.copy(
                    selectedStudent = student,
                   showAttendanceNotAllowed = true
                )
            }
        }



    }

    fun onDismissShowNoAttendance(){
        _uiState.update { state ->
            state.copy( showAttendanceNotAllowed = false )
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
    fun onRegisterStudent(name: String, phone: String, takenStatus : Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegistering = true)

            val authority = if (takenStatus) userPhone else "NA"
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val student = StudentDTO(name, phone, "NA", "OTP", "NA", "NA", currentDate, authority)

            try {

                    // Perform the network call on IO thread
                    studentRepository.insertStudent(student)

                    // Fetch the updated student list after insertion
                    val updatedStudentList = studentRepository.getAllStudents().first()

                    // Update the UI after successful registration
                    _uiState.value = _uiState.value.copy(
                        isRegistering = false,
                        showRegistrationDialog = false, // Close registration dialog
                        students = updatedStudentList
                    )

                // Sync and update sync status in background
                launch(Dispatchers.IO) {
                    try {
                        // Simulate the remote request (for example, syncing to a remote server)
                        studentRepository.syncStudent(student)
                        studentRepository.updateStudentToSynced(student.phone)
                    } catch (e: Exception) {
                        // Log or handle any errors related to remote sync
                        Log.e("RemoteSync", "Failed to sync student to remote", e)
                    }
                }
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPreviousSunday(): String {
        // Get the current date
        val currentDate = LocalDate.now()

        // Calculate days to subtract to get to the previous Sunday
        val daysToSubtract = if (currentDate.dayOfWeek == DayOfWeek.SUNDAY) {
            0 // Today is Sunday
        } else {
            currentDate.dayOfWeek.value // Get numerical value of the current day (1 = Monday, ..., 7 = Sunday)
        }

        // Find the previous Sunday
        val previousSunday = currentDate.minusDays(daysToSubtract.toLong())

        // Format the date as "yyyy-MM-dd"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return previousSunday.format(formatter)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun isTodayWeekend(): Boolean {
        // Get today's date
        val today = LocalDate.now()

        // Get the day of the week
        val dayOfWeek = today.dayOfWeek

        // Check if today is Saturday or Sunday
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun postAttendance(student: StudentPOJO) {
        viewModelScope.launch {

            // Update UI state to show that submission is in progress
            _uiState.value = _uiState.value.copy(isPostingAttendance = true)


            val currentDate = if (isTodayWeekend()) getCurrentOrNextSunday() else getPreviousSunday()
            val attendance = AttendancePOJO(student.phone, currentDate, student.name, regDate = student.date)

            try {
                // Save attendance locally using the data store
                withContext(Dispatchers.IO) {
                    // save to local room database.
                    studentRepository.markAttendance(student.phone, currentDate)

                    // save to data store for syncing.
                    AttendanceDataStore.saveNewAttendance(attendance)

                }

                // Update UI state after submission completes
                _uiState.value = _uiState.value.copy(
                    isPostingAttendance = false,
                    showCongratsAfterPosting = true,
                    showAttendanceNotAllowed = false,// If successful, show congratulations
                )
            } catch (e: Exception) {
                // Handle submission failure
                _uiState.value = _uiState.value.copy(isPostingAttendance = false)
                Log.e("AttendanceViewModel", "Error posting attendance: ${e.message}")
            }
        }
    }

}