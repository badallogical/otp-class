package com.harekrishna.otpClasses.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity(tableName = "attendance_response")
data class AttendanceResponse(
    @PrimaryKey val phone: String
)

@Entity(
    tableName = "attendance_dates",
    primaryKeys = ["date", "attendancePhone"],
    foreignKeys = [
        ForeignKey(
            entity = AttendanceResponse::class, // Replace with actual class
            parentColumns = ["phone"],          // Parent column in the referenced table
            childColumns = ["attendancePhone"], // Column in this table that references the parent
            onDelete = ForeignKey.CASCADE       // Handle deletion rules (optional)
        )
    ]
)
data class AttendanceDate(
    val date: String,               // Attendance date
    val attendancePhone: String     // Foreign key to AttendanceResponse
)

data class AttendanceWithDates(
    @Embedded val attendanceResponse: AttendanceResponse,
    @Relation(
        parentColumn = "phone",
        entityColumn = "attendancePhone"
    )
    val attendanceDates: List<AttendanceDate>
)

data class AttendeeItem(
    val name: String,
    val phone: String,
    val callingStatus: String,
    val attendances : List<String>,   // Last 4 attendance.
    val totalCount : Int,
    val isActive : Boolean = false,
    val isInvited : Boolean = false,
    val feedback: String = "",
    val registrationDate: String = ""
)

