package com.harekrishna.otpClasses.ui.registeration

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter


data class CallingListUiState(
    val registrations: List<CallingReportPOJO> = emptyList(),
    val updatingStatus: Boolean = false
)

class CallingListViewModel(private val callingReportRepository: CallingReportRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(CallingListUiState())
    val uiState: StateFlow<CallingListUiState> = _uiState.asStateFlow()

    lateinit var userName : String
    lateinit var userPhone : String

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
        }
    }

    fun updateStudentStatus(phone: String, status: String, invited : Boolean) {

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
                callingReportRepository.updateCallingReportStatus(phone, status)
                callingReportRepository.updateCallingReportInvited(phone,invited)
            }
        }
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
                    var reportMsg = "\uD83D\uDCDD *$formattedDate Welcome Calling Report* \n\n"
                    val reports = callingReportRepository.getCallingReportsByDate(date).first()

                    for (report in reports) {
                        // Append the default information for each report
                        reportMsg += "👤 *${report.name}* \n📞 ${report.phone}"

                        // Check if the report is invited and append the message sent icons if true
                        if (report.isInvited) {
                            reportMsg += " ✉️ ✅"
                        }

                        // Add status and a newline for the next report
                        reportMsg += "\n📊 Status: *${report.status}*\n\n"
                    }

                    // Add the closing message with emojis
                    reportMsg += "🙏 Hare Krishna Prabhu Ji \n🙇‍♂️ Dandwat Pranam"

                    reportMsg
                }

                // Send the message via WhatsApp
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=&text=$msg")
                }
                context.startActivity(intent)

            } catch (e: Exception) {
                e.printStackTrace()
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