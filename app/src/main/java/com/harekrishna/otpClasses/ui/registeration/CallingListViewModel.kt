package com.harekrishna.otpClasses.ui.registeration

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import com.harekrishna.otpClasses.MyApplication.Companion.toCamelCase
import com.harekrishna.otpClasses.data.local.repos.CallingReportRepository
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.models.AttendeeItem
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter


data class CallingListUiState(
    val registrations: List<CallingReportPOJO> = emptyList(),
    val updatingStatus: Boolean = false,
    val date: String = "",

    // Add new selection-related states
    val selectedAttendees: Set<String> = emptySet(), // Store selected phone numbers
    val isInSelectionMode: Boolean = false,
    val showDeleteDialog : Boolean =  false,
    val isDeleting : Boolean = false
)

class CallingListViewModel(private val callingReportRepository: CallingReportRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(CallingListUiState())
    val uiState: StateFlow<CallingListUiState> = _uiState.asStateFlow()

    lateinit var userName : String
    lateinit var userPhone : String

    lateinit var welcomeMsg : String

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository =
                    application.container.callingReportRepository // Assuming container contains the repository
                CallingListViewModel(repository)  // Pass the repository to the ViewModel constructor
            }
        }
    }

    init{
        viewModelScope.launch {
            val userData = AttendanceDataStore.getUserData().first()
            userName = userData.first ?: "Rajiva Prabhu Ji"
            userPhone = userData.second ?: "+919807726801"

            welcomeMsg = AttendanceDataStore.getWelcomeMessage()


        }
    }


    fun getCallingRegistrations(date: String = "") {
        viewModelScope.launch {
            callingReportRepository.getCallingReportsByDate(date).collect { callingReports ->
                _uiState.update { current ->
                    current.copy(
                        registrations = callingReports ?: emptyList(),
                    )
                }
            }

            _uiState.value = _uiState.value.copy(date = date )
        }
    }

    fun updateStudentStatus(phone: String, status: String, invited : Boolean, feedback : String) {

        val viewStatus = status.split(",").firstOrNull()?.trim() ?: status

        // Update the UI, registrations list with the new status
        _uiState.value = _uiState.value.copy(
            registrations = _uiState.value.registrations.map { student ->
                if (student.phone == phone) student.copy(status = viewStatus, isInvited = invited) else student
            }
        )

        // update the database
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                callingReportRepository.updateCallingReportStatus(phone, status.trim())
                callingReportRepository.updateCallingReportInvited(phone,invited)
                callingReportRepository.updateCallingReportFeedback(phone,feedback)
            }
        }
    }

    fun deleteSelectedStudents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            try {
                coroutineScope {
                    val successfulDeletions = uiState.value.selectedAttendees.map { phone ->
                        async(Dispatchers.IO) {
                            try {
                                // Delete from the database
                                callingReportRepository.deleteCallingReportByPhone(phone)
                                phone // Return phone if successful
                            } catch (e: Exception) {
                                Log.e("DeleteError", "Error deleting $phone: ${e.message}")
                                null // Return null if failed
                            }
                        }
                    }.awaitAll().filterNotNull() // Filter out failed deletions

                    // Update local state by removing deleted reports
                    _uiState.value = _uiState.value.copy(
                        registrations = _uiState.value.registrations.filterNot { it.phone in successfulDeletions },
                        selectedAttendees = emptySet() // Clear selections after deletion,
                    )

                    Log.d("DeleteStatus", "Successfully deleted: $successfulDeletions")
                }
            } catch (e: Exception) {
                Log.e("DeleteError", "Error in deleteSelectedStudents: ${e.message}")
            } finally {
                _uiState.value = _uiState.value.copy(isDeleting = false, showDeleteDialog = false, isInSelectionMode = false)
            }
        }
    }


    fun onClickDelete(){
        _uiState.value = _uiState.value.copy( showDeleteDialog = true );
    }

    fun onDismissDelete(){
        _uiState.value = _uiState.value.copy( showDeleteDialog = false )
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun sendCallingReportMsg(context: Context, date: String) {
        viewModelScope.launch {
            try {
                // Fetch the message content in a background thread
                val msg = withContext(Dispatchers.IO) {
                    // Parse the date string (assuming it's in 'yyyy-MM-dd' format)
                    val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                    // Format the date into "Mon, 8 Oct, 2024"
                    val formattedDate = parsedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM, yyyy"))

                    // Start building the message 🗓️
                    var reportMsg = "\uD83D\uDCDD *$formattedDate Welcome Calling Report* \n"

                    // Use selected attendees if any, otherwise use filtered list
                    val reports = if (uiState.value.selectedAttendees.isNotEmpty()) {
                        uiState.value.registrations.filter {
                            uiState.value.selectedAttendees.contains(it.phone)
                        }
                    } else {
                        uiState.value.registrations
                    }

                    reportMsg += "Total Strength : ${reports.size}\n\n"

                    for (report in reports) {
                        // Append the default information for each report
                        reportMsg += "👤 *${report.name.trim()}* \n📞 ${report.phone.trim()}"

                        // Check if the report is invited and append the message sent icons if true
                        if (report.isInvited) {
                            reportMsg += " ✉️ ✅"
                        }

                        // Add status and a newline for the next report
                        reportMsg += "\n📊 Status: *${report.status.trim()}*\n"

                        // Show feedback only if it's not empty
                        if (report.feedback.isNotEmpty()) {
                            reportMsg += "💬 Feedback: *${report.feedback.trim()}*\n"
                        }


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

    fun sendWhatsAppMessage(
        context: Context,
        phoneNumber: String,
        name: String
    ) {

        val phone = formatPhoneNumber(phoneNumber)

        val greeting = "Hare Krishna *${name.toCamelCase().trim()} Prabhu Ji* \uD83D\uDE4F";
        val footer = "Your Servent\n${userName}\n\uD83D\uDCDE *Contact*: ${userPhone}\n(Please save this number)"
        val message = greeting + "\n\n" + welcomeMsg + "\n\n" + footer;


        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/${phone}?text=${message.trimIndent()}")
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


}