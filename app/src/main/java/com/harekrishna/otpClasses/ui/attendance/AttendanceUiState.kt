package com.harekrishna.otpClasses.ui.attendance

import com.harekrishna.otpClasses.data.models.AttendancePOJO
import com.harekrishna.otpClasses.data.models.AttendanceWithDates
import com.harekrishna.otpClasses.data.models.StudentPOJO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AttendanceUiState (
    val students: List<StudentPOJO> = emptyList(),
    val filteredStudents: List<StudentPOJO> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,
    val selectedStudent: StudentPOJO? = null,
    val showRegistrationDialog: Boolean = false,
    val isRegistering: Boolean = false,
    val isPostingAttendance : Boolean = false,
    val showCongratsAfterPosting : Boolean = false,
    val showAttendanceNotAllowed : Boolean  = false
)

data class AttendanceDetailsUiState(
    val selectedFilter: String = "All",
    val filteredAttendees: List<StudentAttendee> = emptyList(),
    val totalAttendees: Int = 0,
    val totalNew: Int = 0,
    val totalRepeated: Int = 0,
    val assignedCount: Int = 0
)
