package com.harekrishna.otpClasses.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("calling_report")
data class CallingReportPOJO(
    @PrimaryKey val phone: String,
    val name: String,
    val status : String,            // calling status
    val attendanceCount : Int,
    val date : String,              // registration date
    val isInvited: Boolean,
    val isActive: Boolean,
    val feedback: String,
    val tag: String,
    val remark: String, // for personal use. will work like about.
    val photoUri : String? = null
)