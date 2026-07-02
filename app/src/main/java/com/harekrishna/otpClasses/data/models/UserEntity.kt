package com.harekrishna.otpClasses.data.models

import androidx.room.Entity
import com.harekrishna.otpClasses.domain.model.Role

@Entity(tableName = "user", primaryKeys = ["id"])
data class UserEntity(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val photoURL : String? = null,
    val email: String = "",
    val isGuest: Boolean = false,
    val role: String = Role.DEVOTEE.name,
)
