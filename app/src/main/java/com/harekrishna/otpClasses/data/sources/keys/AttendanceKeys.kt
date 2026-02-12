package com.harekrishna.otpClasses.data.sources.keys

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object AttendanceKeys {
    val ATTENDANCE_KEY = stringPreferencesKey("attendance_map")
    val DATE_KEY = stringSetPreferencesKey("date_list")

}