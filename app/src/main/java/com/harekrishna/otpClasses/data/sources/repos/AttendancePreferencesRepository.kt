package com.harekrishna.otpClasses.data.sources.repos

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.google.gson.Gson
import com.harekrishna.otpClasses.data.models.AttendancePOJO
import com.harekrishna.otpClasses.data.sources.dataStore
import com.harekrishna.otpClasses.data.sources.keys.AttendanceKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AttendancePreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context : Context
) {

    private val gson = Gson()

    suspend fun clearAttendanceData() {
        context.dataStore.edit {
            it[AttendanceKeys.ATTENDANCE_KEY] =
                gson.toJson(emptyMap<String, List<AttendancePOJO>>())
        }
    }

    val dates: Flow<Set<String>> = context.dataStore.data.map {
        it[AttendanceKeys.DATE_KEY] ?: emptySet()
    }

    // Add a new date
    suspend fun addDate(date: String) {
        context.dataStore.edit {
            val currentDates = it[AttendanceKeys.DATE_KEY] ?: emptySet()
            it[AttendanceKeys.DATE_KEY] = currentDates + date
        }
    }

    // Remove a date
    suspend fun removeDate(date: String) {
        context.dataStore.edit { preferences ->
            val currentDates = preferences[AttendanceKeys.DATE_KEY] ?: emptySet()
            preferences[AttendanceKeys.DATE_KEY] = currentDates - date // Remove date from the set
        }
    }

    // Check if a date exists
//    suspend fun dateExists(date: String): Boolean {
//        val currentDates = AppDataStore.context.dataStore.data
//            .map { preferences -> preferences[DATE_KEY] ?: emptySet() }
//            .first() // Get the first emitted value
//        return date in currentDates // Check if the date exists
//    }


}