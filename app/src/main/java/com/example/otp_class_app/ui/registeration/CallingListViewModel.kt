package com.example.otp_class_app.ui.registeration

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.data.local.repos.CallingReportRepository
import com.example.otp_class_app.data.models.CallingReportPOJO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.Color
import java.io.File
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

    fun updateStudentStatus(phone: String, status: String) {

        val viewStatus = status.split(",").firstOrNull()?.trim() ?: status


        // Update the UI, registrations list with the new status
        _uiState.value = _uiState.value.copy(
            registrations = _uiState.value.registrations.map { student ->
                if (student.phone == phone) student.copy(status = viewStatus) else student
            }
        )

        // update the database
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                callingReportRepository.updateCallingReportStatus(phone, status)
            }
        }
    }

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
                    var reportMsg = "\uD83D\uDCDD *$formattedDate Calling Report* \n\n"
                    val reports = callingReportRepository.getCallingReportsByDate(date).first()

                    // Loop through the reports and add them to the message
                    for (report in reports) {
                        reportMsg += "👤 *${report.name}* \n📞 ${report.phone} \n📊 Status: *${report.status}*\n\n"
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








}