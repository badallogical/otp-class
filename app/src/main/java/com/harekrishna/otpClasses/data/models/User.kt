package com.harekrishna.otpClasses.data.models

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val photoURL : String,
    val email: String,
    val isGuest: Boolean
)
