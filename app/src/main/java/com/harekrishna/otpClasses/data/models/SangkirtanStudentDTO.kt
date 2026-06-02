package com.harekrishna.otpClasses.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "sangkirtan_students")
data class SangkirtanStudentDTO(

    @ColumnInfo("_name")
    @SerializedName("Name")
    val name: String,

    @PrimaryKey
    @ColumnInfo("_phone")
    @SerializedName("Phone")
    val phone: String,

    @ColumnInfo("_category")
    @SerializedName("Category")
    val category: String,

    @ColumnInfo("_location")
    @SerializedName("Location")
    val location: String,

    @ColumnInfo("_date")
    @SerializedName("Date")
    val date: String,

    @ColumnInfo("_by")
    @SerializedName("By")
    val byDev: String? = null,

    val sync: Boolean = false,
    val photoUri: String? = null
)