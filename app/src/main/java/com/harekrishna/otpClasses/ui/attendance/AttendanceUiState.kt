package com.harekrishna.otpClasses.ui.attendance

import com.harekrishna.otpClasses.data.models.StudentPOJO

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
    val showAttendanceNotAllowed : Boolean  = false,
)