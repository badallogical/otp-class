package com.example.otp_class_app.ui.attendance

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.otp_class_app.data.models.StudentPOJO

data class AttendanceUiState (
    val students: List<StudentPOJO>? = null,
    val filteredStudents: List<StudentPOJO> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showDialog: Boolean = false,
    val selectedStudent: StudentPOJO? = null,
    val showRegistrationDialog: Boolean = false,
    val isRegistering: Boolean = false,
    val isPostingAttendance : Boolean = false,
    val showCongratsAfterPosting : Boolean = false
)