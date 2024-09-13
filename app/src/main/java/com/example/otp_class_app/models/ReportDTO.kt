package com.example.otp_class_app.models

data class ReportDTO(
    var name: String,
    var contact: String,
    var facilitator: String,
    var classlevel: String,
    var chanting: String = "",
    var wrh: String = "",
    var book: String = "",
    var whh: String = "",
    var active: String = "",
    var fourRegPrinciples: String = "",
    var percentageRegularity: String = "",
    var meetsFacilitator: String = "",
    var sewa: String = "",
    var seriousness: String = "",
    var attendsDailyEveningClass: String = "",
    var reasonForNotAttending: String? = "",
    var noOfNightStay: String = "",
    var remarks: String? = "",
    var lastMeetingDate: String? = ""
)

