package com.harekrishna.otpClasses.data.models
import java.util.Date
// For local use we use POJO that include name.
data class AttendancePOJO(
    val studentId: String,
    val date: String,
    val name: String
)

// To Sync online we use DTO
fun AttendancePOJO.toDTO(): AttendanceDTO {
    return AttendanceDTO(
        studentId = this.studentId,
        date = this.date
    )
}


data class AttendanceResponse(
    val phone: String,
    val attendanceDates: List<Date> // List of Date objects instead of String
)