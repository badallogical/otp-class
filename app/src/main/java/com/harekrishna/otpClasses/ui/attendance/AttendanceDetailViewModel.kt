package com.harekrishna.otpClasses.ui.attendance

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.local.repos.AttendanceRepository
import com.harekrishna.otpClasses.data.local.repos.StudentRepository
import com.harekrishna.otpClasses.data.models.StudentAttendee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AttendanceDetailViewModel(
    private val studentRepository: StudentRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    // Attendance Details Ui State
    private val _attendanceDetailUiState = MutableStateFlow(AttendanceDetailsUiState())
    val attendanceDetailsUiState: StateFlow<AttendanceDetailsUiState> =
        _attendanceDetailUiState.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication
                val repository1 =
                    application.container.studentRepository // Assuming container contains the repository
                val repository2 = application.container.attendanceResponseRepository
                AttendanceDetailViewModel(
                    repository1,
                    repository2
                )  // Pass the repository to the ViewModel constructor
            }
        }
    }

    // Load the detail attendance in UI state
    fun loadAttendanceDetailData(date: String) {
        viewModelScope.launch {
            // Set loading true on main thread
            _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(isLoading = true)

            try {
                val list = withContext(Dispatchers.IO) {
                    attendanceRepository.getDetailAttendanceDataByDate(date)
                }

                // Update UI state with the fetched data
                _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
                    attendanceList = list,
                    isLoading = false
                ).recalculateStats()
            } catch (e: Exception) {
                // Handle errors gracefully and stop loading
                _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
                    isLoading = false
                )
                Log.e("ViewModel", "Failed to load attendance detail: ${e.message}")
            }
        }
    }



    fun onMarkLeftDialog(attendee: StudentAttendee) {
        _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
            showLeftDialog = true,
            selectedAttendee = attendee
        )
    }

    fun onDeleteAttendanceDialog(attendee: StudentAttendee) {
        _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
            showDeleteDialog = true,
            selectedAttendee = attendee
        )
    }

    fun onReturnBackDialog(attendee: StudentAttendee) {
        _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
            showReturnDialog = true,
            selectedAttendee = attendee
        )
    }

    fun onDismissDeleteDialog() {
        _attendanceDetailUiState.value =
            _attendanceDetailUiState.value.copy(showDeleteDialog = false)
    }

    fun onDismissMarkLeftDialog() {
        _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(showLeftDialog = false)
    }

    fun onDismissJoinedDialog() {
        _attendanceDetailUiState.value =
            _attendanceDetailUiState.value.copy(showReturnDialog = false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onMarkLeft() {
        viewModelScope.launch {
            val attendee: StudentAttendee

            if (attendanceDetailsUiState.value.selectedAttendee != null) {
                attendee = attendanceDetailsUiState.value.selectedAttendee!!
            } else {
                Log.d("AttendanceViewModel", "selected attendee is null")
                return@launch
            }

            // db operation
            val currentTime = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("hh:mm a")
            )

            withContext(Dispatchers.IO) {
                attendanceRepository.updateAttendanceDateMarkLeft(
                    attendee.phone,
                    attendee.date,
                    true,
                    currentTime
                )


                // Update state
                _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(

                    attendanceList = _attendanceDetailUiState.value.attendanceList.map { _attendee ->
                        if (_attendee.id == attendee.id) {
                            _attendee.copy(hasLeft = true, leftTime = currentTime)
                        } else {
                            _attendee
                        }
                    },
                    showLeftDialog = false
                ).recalculateStats()
            }

        }
    }

    fun onDeleteAttendee(){
        viewModelScope.launch {
            val attendee: StudentAttendee

            if (attendanceDetailsUiState.value.selectedAttendee != null) {
                attendee = attendanceDetailsUiState.value.selectedAttendee!!
            } else {
                Log.d("AttendanceViewModel", "selected attendee is null")
                return@launch
            }

            withContext(Dispatchers.IO) {
                attendanceRepository.updateAttendanceDateDeleted(
                    attendee.phone,
                    attendee.date,
                    true
                )
            }

            // Update state
            _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
                attendanceList = _attendanceDetailUiState.value.attendanceList.filter { it.id != attendee.id },
                showDeleteDialog = false
            ).recalculateStats()
        }
    }

    fun onReturnBack(){
        viewModelScope.launch {
            val attendee: StudentAttendee

            if (attendanceDetailsUiState.value.selectedAttendee != null) {
                attendee = attendanceDetailsUiState.value.selectedAttendee!!
            } else {
                Log.d("AttendanceViewModel", "selected attendee is null")
                return@launch
            }


            withContext(Dispatchers.IO) {
                attendanceRepository.updateAttendanceDateMarkLeft(
                    attendee.phone,
                    attendee.date,
                    false,
                    ""
                )


                // Update state
                _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(

                    attendanceList = _attendanceDetailUiState.value.attendanceList.map { _attendee ->
                        if (_attendee.id == attendee.id) {
                            _attendee.copy(hasLeft = false, leftTime = "")
                        } else {
                            _attendee
                        }
                    },
                    showReturnDialog = false
                ).recalculateStats()
            }

        }
    }

    fun onFilterMode() {
        _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
            isSearchMode = false
        )
    }

    fun onFilter(filter: String = attendanceDetailsUiState.value.selectedFilter) {
        _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
            selectedFilter = filter
        )
    }

    fun onSearchMode( mode : Boolean = true) {
        _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
            isSearchMode = mode
        )
    }

    fun onSearchQuery(searchText: String) {
        _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
            searchText = searchText
        )
    }


}

