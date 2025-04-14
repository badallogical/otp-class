package com.harekrishna.otpClasses.data.models


// For local use we use POJO that include name.
data class AttendancePOJO(
    val studentId: String,
    val date: String,
    val name: String,
    val regDate: String?
)

// Enhanced Data Class with left status and time
data class StudentAttendee(
    val name: String,
    val phone: String,
    val facilitator: String?,
    val date: String,
    val repeatedTimes: Int,
    val isNew: Boolean,
    val regDate: String,
    val hasLeft: Boolean = false,
    val leftTime: String? = null,
    val id: String = phone // Using phone as unique identifier for simplicity
)

data class AttendanceHistory(
    val date: String,  // date of attendance
    val count: Int     // count of total attendance
)

// To Sync online we use DTO
fun AttendancePOJO.toDTO(): AttendanceDTO {
    return AttendanceDTO(
        studentId = this.studentId,
        date = this.date,
        regDate = this.regDate?: ""
    )
}

