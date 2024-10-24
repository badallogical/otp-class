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
    foreignKeys = [ForeignKey(
        entity = AttendanceResponse::class,
        parentColumns = ["phone"],
        childColumns = ["attendancePhone"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["attendancePhone"])] // Add index to improve query performance
)
data class AttendanceDate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,  // Store attendance date as String or Date (with TypeConverter)
    val attendancePhone: String  // This is the foreign key that references AttendanceResponse
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
    val isActive : Boolean = false,
    val isInvited : Boolean = false,
    val feedback: String = "",
    val registrationDate: String = ""
)

