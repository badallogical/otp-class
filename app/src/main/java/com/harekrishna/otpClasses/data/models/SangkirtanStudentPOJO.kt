package com.harekrishna.otpClasses.data.models

import com.google.gson.annotations.SerializedName

data class SangkirtanStudentPOJO (
    @SerializedName("Name") val name: String,
    @SerializedName("Phone") val phone: String,
    @SerializedName("Category") val category: String,
    @SerializedName("Location") val location: String,
    @SerializedName("Date") val date: String,
    @SerializedName("By") val regBy: String
)