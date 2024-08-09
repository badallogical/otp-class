package com.example.otp_class_app.models

import com.google.gson.annotations.SerializedName

data class StudentPOJO (
    @SerializedName("Name") val name: String,
    @SerializedName("Phone") val phone: String,
    @SerializedName("Facilitator") val facilitator: String,
    @SerializedName("batch") val batch: String
)