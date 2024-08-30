package com.example.otp_class_app.models

import com.google.gson.annotations.SerializedName

data class StudentDTO(
    @SerializedName("Name") private val _name: String,
    @SerializedName("Phone") private val _phone: String,
    @SerializedName("Facilitator") private val _facilitator: String,
    @SerializedName("Batch") private val _batch: String,
    @SerializedName("Profession") private val _profession: String,
    @SerializedName("Address") private val _address: String,
    @SerializedName("Date") val date: String
) {
    val name: String
        get() = _name.toCamelCase()

    val phone: String
        get() = _phone.toCamelCase()

    val facilitator: String
        get() = _facilitator

    val batch: String
        get() = _batch

    val profession: String
        get() = _profession.toCamelCase()

    val address: String
        get() = _address.toCamelCase()

    private fun String.toCamelCase(): String {
        return this.lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }

}



