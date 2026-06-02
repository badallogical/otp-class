package com.harekrishna.otpClasses.data.models

data class SangkirtanRegistrationStatus(
    val date: String,
    val counts : Int,
    val synced : Boolean = false
)
