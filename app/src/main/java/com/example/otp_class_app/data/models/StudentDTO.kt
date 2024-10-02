package com.example.otp_class_app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "students")
data class StudentDTO(

    @SerializedName("Name")
    private val _name: String,

    @PrimaryKey
    @SerializedName("Phone")
    private val _phone: String,

    @SerializedName("Facilitator") private val _facilitator: String,
    @SerializedName("Batch") private val _batch: String,
    @SerializedName("Profession") private val _profession: String,
    @SerializedName("Address") private val _address: String,
    @SerializedName("Date") val date: String,
    @SerializedName("By") private val _by : String? = null
) {
    val name: String
        get() = _name.toCamelCase()

    val phone: String
        get() = _phone.toString()

    val facilitator: String
        get() = _facilitator

    val batch: String
        get() = _batch

    val profession: String
        get() = _profession.toCamelCase()

    val address: String
        get() = _address.toCamelCase()

    val by: String
        get() = _by ?: "Unknown"

    private fun String.toCamelCase(): String {
        return this.lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }


}



