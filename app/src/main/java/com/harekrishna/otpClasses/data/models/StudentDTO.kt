package com.harekrishna.otpClasses.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "students")
data class StudentDTO(

    @PrimaryKey
    @SerializedName("Phone")
    val phone: String,

    @SerializedName("Name")
    val name: String,

    @SerializedName("Facilitator")
    val facilitator: String,

    @SerializedName("Batch")
    val batch: String,

    @SerializedName("Profession")
    val profession: String,

    @SerializedName("Address")
    val address: String,

    @SerializedName("Date")
    val date: String,

    @SerializedName("By")
    val byDev: String,

    val sync: Boolean = false,
    val photoUri: String? = null
)



//@Entity(tableName = "students")
//data class StudentDTO(
//
//    @SerializedName("Name")
//     val _name: String,
//
//    @PrimaryKey
//    @SerializedName("Phone")
//     val _phone: String,
//
//    @SerializedName("Facilitator")  val _facilitator: String,
//    @SerializedName("Batch")  val _batch: String,
//    @SerializedName("Profession")  val _profession: String,
//    @SerializedName("Address")  val _address: String,
//    @SerializedName("Date") val date: String,
//    @SerializedName("By")  val _by : String? = null,
//
//    var sync: Boolean = false, // Now persisted in the database
//    val photoUri: String? = null
//) {
//
//    val name: String
//        get() = _name.toCamelCase()
//
//    val phone: String
//        get() = _phone.toString()
//
//    val facilitator: String
//        get() = _facilitator
//
//    val batch: String
//        get() = _batch
//
//    val profession: String
//        get() = _profession.toCamelCase()
//
//    val address: String
//        get() = _address.toCamelCase()
//
//    val by: String
//        get() = _by ?: "Unknown"
//
//    private fun String.toCamelCase(): String {
//        return this.lowercase()
//            .split(" ")
//            .joinToString(" ") { word ->
//                word.replaceFirstChar { it.uppercase() }
//            }
//    }
//}









