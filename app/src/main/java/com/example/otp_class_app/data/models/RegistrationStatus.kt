package com.example.otp_class_app.data.models


data class RegistrationStatus(
    val date: String,
    val counts : Int,
    val synced : Boolean = false
)

