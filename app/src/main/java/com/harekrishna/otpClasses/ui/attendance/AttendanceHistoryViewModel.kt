package com.harekrishna.otpClasses.ui.attendance

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.local.repos.AttendanceRepository
import com.harekrishna.otpClasses.data.local.repos.StudentRepository
import com.harekrishna.otpClasses.data.models.AttendanceHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AttendanceHistoryViewModel(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    // Attendance History Screen Ui State
    private val _attendanceHistoryUiState = MutableStateFlow(AttendanceHistoryUiState())
    val attendanceHistoryUiState: StateFlow<AttendanceHistoryUiState> =
        _attendanceHistoryUiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository1 =
                    application.container.studentRepository // Assuming container contains the repository
                val repository2 = application.container.attendanceResponseRepository
                AttendanceHistoryViewModel(
                    repository1,
                    repository2
                )  // Pass the repository to the ViewModel constructor
            }
        }
    }


    fun getAttendanceHistory(){
        viewModelScope.launch {


            val historyList: List<AttendanceHistory> = withContext(Dispatchers.IO) {

                attendanceRepository.getLocalAllAttendanceHistoryData()

            }

            // Update UI state with fetched data
            _attendanceHistoryUiState.value = _attendanceHistoryUiState.value.copy(
                historyList = historyList
            )
        }
    }


    fun onRefreshAttendanceHistory(lastMonth: Int = 0) {
        viewModelScope.launch {
            // Update UI state: loading started
            _attendanceHistoryUiState.value = _attendanceHistoryUiState.value.copy(
                isLoadingRemoteAttendance = true
            )

            val historyList: List<AttendanceHistory> = withContext(Dispatchers.IO) {
                if (lastMonth == 0) {
                    // Load all attendances
                    attendanceRepository.getAllAttendanceHistoryData()
                } else {
                    // TODO: Load only from lastMonth to current month
                    emptyList() // Replace this with actual filtered fetch
                }
            }

            // Update UI state with fetched data
            _attendanceHistoryUiState.value = _attendanceHistoryUiState.value.copy(
                historyList = historyList,
                isLoadingRemoteAttendance = false
            )
        }
    }

    // Share the attendance form the Database
    fun shareAttendanceFromDB(context: Context, date: String) {
        viewModelScope.launch {

            val msg = withContext(Dispatchers.IO) {
                Uri.encode(
                    studentRepository.getStringAttendanceByDate(date)
                )
            }

            if (msg.isNullOrBlank()) {
                Toast.makeText(context, "No attendance data to share", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Send the message via WhatsApp
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=$msg")
                setPackage("com.whatsapp")
            }

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

}