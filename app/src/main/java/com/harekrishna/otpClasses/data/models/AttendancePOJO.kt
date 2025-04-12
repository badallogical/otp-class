package com.harekrishna.otpClasses.data.models


// For local use we use POJO that include name.
data class AttendancePOJO(
    val studentId: String,
    val date: String,
    val name: String,
    val regDate: String?
)

data class StudentAttendee(
    val name: String = "Unknown",
    val phone: String = "",
    val facilitator : String?,
    val date : String,              // date of attendance
    val repeatedTimes : Int = 0,    // repetition in the whole year
    val regDate: String             // date of registration
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

