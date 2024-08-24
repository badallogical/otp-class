package com.example.otp_class_app.models

import com.google.gson.annotations.SerializedName

data class StudentDTO(
    @SerializedName("Name") val name: String,
    @SerializedName("Phone") val phone: String,
    @SerializedName("Facilitator") val facilitator: String,
    @SerializedName("Batch") val batch: String,
    @SerializedName("Profession") val profession: String,
    @SerializedName("Address") val address: String,
    @SerializedName("Date") val date: String
)


