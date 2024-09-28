package com.example.otp_class_app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("calling_report")
data class CallingReportPOJO(
    @PrimaryKey val phone: String,
    val name: String,
    val status : String,
    val attendanceCount : Int,
    val date : String
)