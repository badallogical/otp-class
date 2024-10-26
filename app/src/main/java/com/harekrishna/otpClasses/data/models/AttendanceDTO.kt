package com.harekrishna.otpClasses.data.models

data class AttendanceDTO(
    val studentId: String,
    val date: String
)

data class UserAttendance(
    val phone: String,
    val attendanceDates: List<String>
)