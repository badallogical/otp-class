package com.harekrishna.otpClasses.ui.attendance

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.api.ApiService.postBulkAttendance
import com.harekrishna.otpClasses.data.local.repos.AttendanceRepository
import com.harekrishna.otpClasses.data.local.repos.StudentRepository
import com.harekrishna.otpClasses.data.models.AttendanceDTO
import com.harekrishna.otpClasses.data.models.StudentAttendee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
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

    fun loadAttendanceDetailData(date: String) {
        viewModelScope.launch {
            // Set loading true on the main thread
            _attendanceDetailUiState.update { it.copy(isLoading = true) }

            try {
                // Switch to IO dispatcher for data loading
                withContext(Dispatchers.IO) {
                    attendanceRepository.getDetailAttendanceDataByDate(date).collect { list ->
                        // Back to Main thread to update UI
                        withContext(Dispatchers.Main) {
                            _attendanceDetailUiState.update {
                                it.copy(
                                    attendanceList = list,
                                    isLoading = false
                                ).recalculateStats()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exception on main thread
                _attendanceDetailUiState.update {
                    it.copy(isLoading = false)
                }
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

//                    attendanceList = _attendanceDetailUiState.value.attendanceList.map { _attendee ->
//                        if (_attendee.id == attendee.id) {
//                            _attendee.copy(hasLeft = true, leftTime = currentTime)
//                        } else {
//                            _attendee
//                        }
//                    },
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
                    !attendee.deleted
                )
            }

//            // Update state
//            _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
//                attendanceList = _attendanceDetailUiState.value.attendanceList.filter { it.id != attendee.id },
//                showDeleteDialog = false
//            ).recalculateStats()

            // Update state
            _attendanceDetailUiState.value = _attendanceDetailUiState.value.copy(
//
//                attendanceList = _attendanceDetailUiState.value.attendanceList.map { _attendee ->
//                    if (_attendee.id == attendee.id) {
//                        _attendee.copy(deleted = !_attendee.deleted)
//                    }else{
//                        _attendee
//                    }
//                },
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

//                    attendanceList = _attendanceDetailUiState.value.attendanceList.map { _attendee ->
//                        if (_attendee.id == attendee.id) {
//                            _attendee.copy(hasLeft = false, leftTime = "")
//                        } else {
//                            _attendee
//                        }
//                    },
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

    // Filter the present and convert to AttendanceDTO list.
    private fun List<StudentAttendee>.toAttendanceDTOs(): List<AttendanceDTO> {
        return this
            .map {
                AttendanceDTO(
                    studentId = it.id,
                    date = it.date,
                    regDate = it.regDate
                )
            }
    }

    private val BATCH_SIZE = 10  // Adjust as needed

    fun syncAttendance() {
        viewModelScope.launch {
            _attendanceDetailUiState.update {
                it.copy(
                    isSyncing = true,
                    syncStatus = false
                )
            }

            val totalPresentList = attendanceDetailsUiState.value.attendanceList
                .filter { !it.hasLeft && !it.deleted }
                .toAttendanceDTOs()

            val result = withContext(Dispatchers.IO) {
                totalPresentList.chunked(BATCH_SIZE).forEach { batch ->
                    Log.d("attendance", batch.toString())

                    val success = postBulkAttendance(batch)
                    if (success) {
                        batch.forEach { attendee ->
                            attendanceRepository.updateSyncStatus(
                                attendee.studentId,
                                attendee.date,
                                true
                            )
                        }

                        withContext(Dispatchers.Main){
                            _attendanceDetailUiState.update {
                                it.copy(
                                    totalSynced = it.totalSynced + batch.size
                                )
                            }
                        }

                        Log.d("ApiService", "Attendance Posted batch count ${batch.size}")
                    } else {
                        Log.d("ApiService", "Attendance Posting Failed")
                        return@withContext false // Exit early and return failure
                    }
                }
                return@withContext true // If all batches succeed
            }

            _attendanceDetailUiState.update {
                it.copy(
                    isSyncing = false,
                    syncStatus = result,
                    totalSynced = 0
                )
            }
        }
    }

    fun exportAttendanceToExcel(
        context: Context,
        date: String,
        attendeesList: List<StudentAttendee>,
        onExported: (Uri?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val workbook = XSSFWorkbook()
                val sheet = workbook.createSheet("Attendance")

                // Header row
                val header = sheet.createRow(0)
                val headers = listOf("Name", "Phone", "Facilitator", "RepeatedTimes", "IsNew", "RegDate", "HasLeft", "LeftTime")
                headers.forEachIndexed { index, title ->
                    val cell = header.createCell(index)
                    cell.setCellValue(title)
                }

                // Data rows
                attendeesList.filter { !it.deleted }.forEachIndexed { index, attendee ->
                    val row = sheet.createRow(index + 1)
                    row.createCell(0).setCellValue(attendee.name)
                    row.createCell(1).setCellValue(attendee.phone)
                    row.createCell(2).setCellValue(attendee.facilitator ?: "Unassigned")
                    row.createCell(3).setCellValue(attendee.repeatedTimes.toDouble())
                    row.createCell(4).setCellValue(attendee.isNew)
                    row.createCell(5).setCellValue(attendee.regDate)
                    row.createCell(6).setCellValue(attendee.hasLeft)
                    row.createCell(7).setCellValue(attendee.leftTime ?: "")
                }

                // Write file
                val fileName = "Attendance_Report_$date.xlsx"
                val file = File(context.cacheDir, fileName)
                FileOutputStream(file).use { workbook.write(it) }
                workbook.close()

                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

                // Post result on main thread
                withContext(Dispatchers.Main) {
                    onExported(uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onExported(null)
                }
            }
        }
    }


}

