package com.harekrishna.otpClasses.data.models


// For local use we use POJO that include name.
data class AttendancePOJO(
    val studentId: String,
    val date: String,
    val name: String,
    val regDate: String?
)

// To Sync online we use DTO
fun AttendancePOJO.toDTO(): AttendanceDTO {
    return AttendanceDTO(
        studentId = this.studentId,
        date = this.date,
        regDate = this.regDate?: ""
    )
}

