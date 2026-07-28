package com.harekrishna.otpClasses.domain.model

enum class Role {
    ADMIN,
    DEVOTEE,
    FACILITATOR,
    EVENT_MANAGER
}

data class User(
    val id: String,
    val name: String,
    val phone: String? = null,
    val photoURL: String,
    val email: String,
    val isGuest: Boolean,
    val role: String = Role.DEVOTEE.name,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
