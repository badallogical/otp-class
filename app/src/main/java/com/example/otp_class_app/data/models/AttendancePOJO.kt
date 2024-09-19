package com.example.otp_class_app.data.models

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