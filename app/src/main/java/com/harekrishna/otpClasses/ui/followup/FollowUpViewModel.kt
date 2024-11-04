package com.harekrishna.otpClasses.ui.followup

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
import com.harekrishna.otpClasses.data.local.repos.AttendanceResponseRepository
import com.harekrishna.otpClasses.data.models.AttendeeItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    var selectedSort: String = "None",

    // Add new selection-related states
    val selectedAttendees: Set<String> = emptySet(), // Store selected phone numbers
    val isInSelectionMode: Boolean = false,

    val initialLoading: Boolean = true,  // Add initial loading state
)


@RequiresApi(Build.VERSION_CODES.O)
class FollowUpViewModel(private val attendanceResponseRepository: AttendanceResponseRepository) :
    ViewModel() {

    // UI state
    private var _uiState = MutableStateFlow(FollowUpUiState())
    val uiState: StateFlow<FollowUpUiState> = _uiState.asStateFlow()

    private lateinit var userName : String
    private lateinit var userPhone: String

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Set initial loading state
                _uiState.value = _uiState.value.copy(initialLoading = true)

                getAllLastFourWeekAttendeesAndRegistration()

                // Fetch user data
                val userData = AttendanceDataStore.getUserData().first()
                userName = userData.first ?: "Rajiva Prabhu Ji"
                userPhone = userData.second ?: "+919807726801"

                fetchAndUpdateAttendance()

            } finally {
                // Disable initial loading once done
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(initialLoading = false)
                }
            }
        }
    }

    // Get all last four week attendees using Dispatchers.IO for database calls
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAllLastFourWeekAttendeesAndRegistration() {
        viewModelScope.launch(Dispatchers.IO) {
            val attendeeList = attendanceResponseRepository.getAllLastFourWeekAttendeeAndRegistration()

            // Switch back to Main dispatcher to update UI
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    attendees = attendeeList,
                    filteredAttendee = attendeeList,
                    isLoading = false  // Stop loading indicator after data fetch
                )
            }
        }
    }

    fun filterByLastFourWeekPresentAttendees(){
        viewModelScope.launch(Dispatchers.IO) {
            val attendeeList = attendanceResponseRepository.getAllLastFourWeekAttendee()

            // Switch back to Main dispatcher to update UI
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(
                    filteredAttendee = attendeeList
                )
            }
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

        return lastSunday.format(formatter)
    }


    fun filterByLastPresent() {
        val date = getLastSundayDate()
        viewModelScope.launch(Dispatchers.IO) {
            val presents = attendanceResponseRepository.getAttendeePresentOn(date)
            val result = _uiState.value.attendees.filter { presents.contains(it.phone) }

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(filteredAttendee = result)
            }
        }
    }

    fun filterByLastAbsent() {
        val date = getLastSundayDate()
        viewModelScope.launch(Dispatchers.IO) {
            val presents = attendanceResponseRepository.getAttendeePresentOn(date)
            val result = _uiState.value.attendees.filterNot { presents.contains(it.phone) }

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(filteredAttendee = result)
            }
        }
    }


    fun filterByAll() {
        _uiState.value = _uiState.value.copy( filteredAttendee = uiState.value.attendees)
    }

    fun onSwitchChange( student : AttendeeItem ){
        _uiState.value = _uiState.value.copy(
            attendees = uiState.value.attendees.map {
                if (it.phone == student.phone) it.copy(isActive = !student.isActive) else it
            },
            filteredAttendee = uiState.value.attendees.map {
                if (it.phone == student.phone) it.copy(isActive = !student.isActive) else it
            }
        )
    }

    fun fetchAndUpdateAttendance() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                Log.d("followup", "fetch and Update called")
                val userId = userPhone

                val attendances = ApiService.getAttendanceResponses(userId)
                attendances.forEach { attendance ->
                    attendanceResponseRepository.insertMultipleAttendance(
                        attendance.phone, attendance.attendanceDates
                    )
                }

                // Update Log on IO thread
                Log.d("followup fetching", attendances.toString())
            } catch (e: Exception) {
                Log.e("fetchAndUpdateAttendance", "Error fetching attendance: ${e.message}", e)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun filterByLastFourWeekPresent() {

        val sundays = getLastFourSundays()

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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun filterByLastFourWeekAbsent() {

        val sundays = getLastFourSundays()

        val result = mutableListOf<AttendeeItem>()

        // Iterate through each calling report
        for (attendee in _uiState.value.attendees) {

            // Fetch the last four attendance dates for the current report's phone number
            val lastFourDatesOfAttendee =
                attendanceResponseRepository.getLastFourAttendanceDate(attendee.phone)

            // Check if none of the last four attendance dates match the Sundays
            val absentOnAllSundays = lastFourDatesOfAttendee.none { date -> sundays.contains(date) }

            // If the student was absent on all of the last four Sundays, add to result
            if (absentOnAllSundays) {
                result.add(attendee)
            }
        }

        // Update the UI state with the filtered absent attendees
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
                "All" -> filterByAll()
                "Last Present" -> filterByLastPresent()
                "Last Absent" -> filterByLastAbsent()
                "Last 4 Weeks Present" -> filterByLastFourWeekPresentAttendees()
                "Last 4 Weeks Absent" -> filterByLastFourWeekAbsent()
                else -> filterByLastFourWeekPresentAttendees()
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

    fun updateStudentStatus(
        phone: String,
        status: String,
        invited: Boolean,
        isActive: Boolean,
        feedback: String
    ) {
        _uiState.value = _uiState.value.copy(
            attendees = _uiState.value.attendees.map { student ->
                if (student.phone == phone) student.copy(
                    callingStatus = status,
                    isInvited = invited,
                    isActive = isActive,
                    feedback = feedback
                ) else student
            },
            filteredAttendee = _uiState.value.filteredAttendee.map { student ->
                if (student.phone == phone) student.copy(
                    callingStatus = status,
                    isInvited = invited,
                    isActive = isActive,
                    feedback = feedback
                ) else student
            }
        )

        viewModelScope.launch(Dispatchers.IO) {
            attendanceResponseRepository.updateCallingReportStatus(phone, status)
            attendanceResponseRepository.updateCallingReportInvited(phone, invited)
            attendanceResponseRepository.updateCallingReportFeedback(phone, feedback)
            attendanceResponseRepository.updateCallingReportActivation(phone, isActive)
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

    fun countOfLastFourSundaysPresent(lastFourAttendance : List<String>) : Int{
        // Fetch the last four attendance dates for the current report's phone number

        val sundays = getLastFourSundays()
        var count : Int = 0;

        for( date in lastFourAttendance ){
            if( sundays.contains(date) )
                count += 1;
        }

        return count
    }

    // Update the sendFollowUpReport function to handle selections
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendFollowUpReport(context: Context) {
        viewModelScope.launch {
            try {
                val msg = withContext(Dispatchers.IO) {
                    // Use selected attendees if any, otherwise use filtered list
                    val reports = if (uiState.value.selectedAttendees.isNotEmpty()) {
                        uiState.value.filteredAttendee.filter {
                            uiState.value.selectedAttendees.contains(it.phone)
                        }
                    } else {
                        uiState.value.filteredAttendee
                    }

                    var reportMsg = "\uD83D\uDCDD *Last 4 Weeks Follow Up Calling Report* \n"
                    reportMsg += "Total Strength : ${reports.size}\n\n"

                    for (report in reports) {
                        // Append the default information for each report
                        reportMsg += "👤 *${report.name.trim()}* \n📞 ${report.phone.trim()}"

                        // Check if the report is invited and append the message sent icons if true
                        if (report.isInvited) {
                            reportMsg += " ✉️ ✅"
                        }

                        // Add status and a newline for the next report
                        reportMsg += "\n📊 Status: *${report.callingStatus.trim()}*\n"

                        // Add attendance count
                        reportMsg += "\n📅 Attendance Count: *${countOfLastFourSundaysPresent(report.attendances)}*\n"

                        // Show feedback only if it's not empty
                        if (report.feedback.isNotEmpty()) {
                            reportMsg += "💬 Feedback: *${report.feedback.trim()}*\n"
                        }

                        // Show feedback
                        reportMsg += "\n\n"
                    }

                    // Add the closing message with emojis
                    reportMsg += "Your Servant \uD83D\uDE4F \n${userName.toCamelCase()}"

                    reportMsg
                }

                // Send the message via WhatsApp
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=&text=$msg")
                }
                context.startActivity(intent)

                // Clear selections after sharing
                clearSelections()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Toggle selection mode
    fun toggleSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isInSelectionMode = !_uiState.value.isInSelectionMode,
            selectedAttendees = emptySet() // Clear selections when toggling off
        )
    }

    // Toggle selection for a specific attendee
    fun toggleAttendeeSelection(phone: String) {
        val currentSelections = _uiState.value.selectedAttendees.toMutableSet()
        if (currentSelections.contains(phone)) {
            currentSelections.remove(phone)
        } else {
            currentSelections.add(phone)
        }

        _uiState.value = _uiState.value.copy(
            selectedAttendees = currentSelections,
            isInSelectionMode = currentSelections.isNotEmpty()
        )
    }

    // Clear all selections
    fun clearSelections() {
        _uiState.value = _uiState.value.copy(
            selectedAttendees = emptySet(),
            isInSelectionMode = false
        )
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

        // It don't count the current sunday but give me last sundays.
        @RequiresApi(Build.VERSION_CODES.O)
        fun getLastFourSundays(): List<String> {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // Calculate the previous Sunday, even if today is Sunday
            val daysToPreviousSunday = if (today.dayOfWeek == DayOfWeek.SUNDAY) 7 else today.dayOfWeek.value
            var current = today.minusDays(daysToPreviousSunday.toLong())

            // Collect the last 4 Sundays
            val sundays = mutableListOf<String>()
            for (i in 0 until 4) {
                sundays.add(current.format(formatter))
                current = current.minusWeeks(1) // Go back one week
            }



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