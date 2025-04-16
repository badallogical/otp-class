package com.harekrishna.otpClasses.ui.attendance

import com.harekrishna.otpClasses.data.models.AttendanceHistory
import com.harekrishna.otpClasses.data.models.StudentAttendee
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
    val showAttendanceNotAllowed : Boolean  = false
)

data class AttendanceHistoryUiState(
    val isLoadingRemoteAttendance: Boolean = false,
    val historyList : List<AttendanceHistory> = emptyList()
)

data class AttendanceDetailsUiState(
    val attendanceList: List<StudentAttendee> = emptyList(),
    val selectedFilter: String = "All",
    val totalAttendees: Int = 0,
    val totalNew: Int = 0,
    val totalRepeated: Int = 0,
    val assignedCount: Int = 0,
    val totalLeft : Int = 0,
    val totalPresent: Int = totalAttendees - totalLeft,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showLeftDialog: Boolean = false,
    val showReturnDialog: Boolean = false,
    val selectedAttendee : StudentAttendee? = null,
    val isSearchMode : Boolean = false,
    val searchText : String = ""
)

fun AttendanceDetailsUiState.recalculateStats(): AttendanceDetailsUiState {
    val totalAttendees = attendanceList.size
    val totalNew = attendanceList.count { it.repeatedTimes == 1 }
    val totalRepeated = attendanceList.count { it.repeatedTimes > 1 }
    val assignedCount = attendanceList.count {
        !it.facilitator.isNullOrBlank() && it.facilitator != "NA"
    }
    val totalLeft = attendanceList.count { it.hasLeft }
    val totalPresent = totalAttendees - totalLeft

    return this.copy(
        totalAttendees = totalAttendees,
        totalNew = totalNew,
        totalRepeated = totalRepeated,
        assignedCount = assignedCount,
        totalLeft = totalLeft,
        totalPresent = totalPresent
    )
}

