package com.harekrishna.otpClasses.data.models


data class RegistrationStatus(
    val date: String,
    val counts : Int,
    val synced : Boolean = false
)

