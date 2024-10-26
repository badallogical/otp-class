package com.harekrishna.otpClasses.ui.followup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.api.ApiService
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.local.repos.AttendanceResponseRepository
import com.harekrishna.otpClasses.data.models.AttendeeItem
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.ui.attendance.AttendanceUiState
import com.harekrishna.otpClasses.ui.registeration.CallingListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FollowUpUiState(
    val attendees: List<AttendeeItem> = emptyList(),
    var filteredAttendee: List<AttendeeItem> = emptyList(), // also include sorting.
    var selectedTab: Int = 0,
    val isLoading: Boolean = false,

    // States for expanded dropdowns
    val isFilterDropdownExpanded: Boolean = false,
    val isSortDropdownExpanded: Boolean = false,

    // States for selected options
    var selectedFilter: String = "All",
    var selectedSort: String = "None"
)


@RequiresApi(Build.VERSION_CODES.O)
class FollowUpViewModel(private val attendanceResponseRepository: AttendanceResponseRepository) :
    ViewModel() {

    // UI state
    private var _uiState = MutableStateFlow(FollowUpUiState())
    val uiState: StateFlow<FollowUpUiState> = _uiState.asStateFlow()


    init {
        getAllLastFourWeekAttendees()
        viewModelScope.launch {
            fetchAndUpdateAttendance()
        }
    }

    // get all last 4 attendees.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAllLastFourWeekAttendees() {

        viewModelScope.launch {
            val attendeeList = withContext(Dispatchers.IO) {
                attendanceResponseRepository.getAllLastFourWeekAttendees()
            }
            Log.d("FollowUp", attendeeList.toString())
            _uiState.value =
                _uiState.value.copy(attendees = attendeeList, filteredAttendee = attendeeList)
        }
    }

    fun getLastSundayDate(): String {
        // Get the current date
        val today = LocalDate.now()

        // Calculate days to subtract to get to the last Sunday
        val daysSinceSunday = (today.dayOfWeek.value % 7)
        val lastSunday = today.minusDays(daysSinceSunday.toLong())

        // Format the date as "YYYY-MM-DD"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        Log.d("followup", lastSunday.format(formatter))
        return lastSunday.format(formatter)
    }


    suspend fun filterByLastPresent() {
        val date = getLastSundayDate()
        val result = mutableListOf<AttendeeItem>()

        val presents = attendanceResponseRepository.getAttendeePresentOn(date)
        for (attendee in _uiState.value.attendees) {
            if (presents.contains(attendee.phone)) {
                result.add(attendee)
            }
        }

        _uiState.value = _uiState.value.copy(filteredAttendee = result)
    }

    fun filterByLastFourWeeks() {
        _uiState.value = _uiState.value.copy( filteredAttendee = uiState.value.attendees)
    }

    fun onSwitchChange( student : AttendeeItem ){
        _uiState.value = _uiState.value.copy(
            attendees = uiState.value.attendees.map {
                if (it.phone == student.phone) it.copy(isActive = !student.isActive) else it
            }
        )
    }

    fun fetchAndUpdateAttendance() {
        viewModelScope.launch {
            try {
                // Collecting the first emitted user data
                val userData = AttendanceDataStore.getUserData().first()
                val userId = userData.second

                // Ensure userId is not null before making the API call
                if (userId != null) {
                    val attendances = ApiService.getAttendanceResponses(userId)
                    attendances.forEach { attendance ->
                        attendanceResponseRepository.insertMultipleAttendance(attendance.phone, attendance.attendanceDates)
                    }
                } else {
                    Log.e("fetchAndUpdateAttendance", "User ID is null")
                }
            } catch (e: Exception) {
                Log.e("fetchAndUpdateAttendance", "Error fetching attendance: ${e.message}", e)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun filterByLastFourWeekPresent(sundays: List<String>) {

        val result = mutableListOf<AttendeeItem>()

        // Iterate through each calling report
        for (attendee in _uiState.value.attendees) {

            // Fetch the last four attendance dates for the current report's phone number
            val lastFourDatesOfAttendee =
                attendanceResponseRepository.getLastFourAttendanceDate(attendee.phone)

            // Check if any of the last four attendance dates match the Sundays
            val attendedOnSunday = lastFourDatesOfAttendee.any { date -> sundays.contains(date) }

            // If the student attended on any of the last four Sundays, add to result
            if (attendedOnSunday) {
                result.add(attendee)
            }
        }

        _uiState.value = _uiState.value.copy(filteredAttendee = result)
    }

    fun onTabSelected(_selectedTab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = _selectedTab)
    }

    // Filter logic function
    fun filterStudents(filter: String) {
        _uiState.value = _uiState.value.copy(isFilterDropdownExpanded = false, selectedFilter = filter)

        viewModelScope.launch {
            when (filter) {
                "All" -> filterByLastFourWeeks()
                "Last Present" -> filterByLastPresent()
                "Last 4 Weeks Present" -> filterByLastFourWeekPresent(getLastFourSundays())
            }

            sortStudents(uiState.value.selectedSort)
        }


    }

    // Sort logic function
    fun sortStudents(sort: String) {
        _uiState.value = _uiState.value.copy(isSortDropdownExpanded = false, selectedSort = sort)

        val result = uiState.value.filteredAttendee

        viewModelScope.launch {
            val sortedResult = when (sort) {
                "Name" -> result.sortedBy { it.name }
                "Date" -> result.sortedByDescending { it.registrationDate } // Assuming registrationDate is a String in "yyyy-MM-dd" format
                "Attendance" -> result.sortedByDescending { it.attendances.size } // Sort by number of attendances (descending)
                else -> result // No sorting if unknown criteria
            }

            _uiState.value = _uiState.value.copy(filteredAttendee = sortedResult)
        }
    }


    fun onFitlerDropDownSelected() {
        _uiState.value = _uiState.value.copy(isFilterDropdownExpanded = true)

    }

    fun onDismissFilterDropDown() {
        _uiState.value = _uiState.value.copy(isFilterDropdownExpanded = false)

    }

    fun onSortDropDownSelected() {
        _uiState.value = _uiState.value.copy(isSortDropdownExpanded = true)
    }

    fun onDismissSortDropDown() {
        _uiState.value = _uiState.value.copy(isSortDropdownExpanded = false)

    }

    fun updateStudentStatus(phone: String, status: String, invited : Boolean, isActive : Boolean, feedback : String ) {

        val viewStatus = status.split(",").firstOrNull()?.trim() ?: status

        // Update the UI, registrations list with the new status
        _uiState.value = _uiState.value.copy(
            attendees = _uiState.value.attendees.map { student ->
                if (student.phone == phone) student.copy(callingStatus = viewStatus, isInvited = invited) else student
            },
        )

        // update the database
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                attendanceResponseRepository.updateCallingReportStatus(phone, status)
                attendanceResponseRepository.updateCallingReportInvited(phone,invited)
                attendanceResponseRepository.updateCallingReportFeedback(phone,feedback)
                attendanceResponseRepository.updateCallingReportActivation(phone, isActive)
            }
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
    
    Thank you for attending our ISKCON Youth Forum (IYF) session! 🌟 We're glad you joined, and we hope it was a fruitful experience for your spiritual journey. 🌱
    
    📢 *We warmly invite you to our next Sunday Program*:
    🕒 *Timing*: 4:30 PM, this Sunday
    🎉 *Highlights*: Engaging Seminar 🧑‍💻🗣️, Soul-stirring Kirtan 🎤, Live Music 🎸, and Delicious Prasadam 🍛🍰.
    
    🏛️ *Venue*: ISKCON Temple, Lucknow
    
    Hare Krishna Prabhu Ji 🙏
    We look forward to seeing you again!
    
    📞 *Contact*: (Your Name Prabhu), Phone (Please save this number)
""".trimIndent()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/${phone}?text=$message")
        context.startActivity(intent)
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository =
                    application.container.attendanceResponseRepository // Assuming container contains the repository
                FollowUpViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun getLastFourSundays(): List<String> {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // If today is Sunday, start with today; otherwise, go to the previous Sunday
            var current = if (today.dayOfWeek == DayOfWeek.SUNDAY) {
                today
            } else {
                today.minusDays((today.dayOfWeek.value % 7).toLong())
            }

            // Collect the last 4 Sundays
            val sundays = mutableListOf<String>()
            for (i in 0..3) {
                sundays.add(current.format(formatter))
                current = current.minusWeeks(1) // Go back one week
            }

            Log.d("followup",sundays.toString())

            return sundays
        }


        // Function to get today's date and the date 4 weeks ago
        fun getLastFourWeeksRange(): Pair<String, String> {
            val today = LocalDate.now()
            val fourWeeksAgo = today.minusWeeks(4)
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            return Pair(fourWeeksAgo.format(dateFormatter), today.format(dateFormatter))
        }


    }

}