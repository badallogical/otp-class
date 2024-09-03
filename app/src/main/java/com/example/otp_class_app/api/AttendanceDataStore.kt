package com.example.otp_class_app.api

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.models.AttendanceDTO
import com.example.otp_class_app.models.AttendancePOJO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AttendanceDataStore {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "attendance")
    private val context: Context by lazy { MyApplication.applicationContext() }

    // Define a key for your attendance map (using JSON String representation)
    private val ATTENDANCE_KEY = stringPreferencesKey("attendance_map")


    // Function to save attendance data to DataStore
    suspend fun saveNewAttendance(attendance: AttendancePOJO) {

        val date = attendance.date;
        context.dataStore.edit { preferences ->
            val currentMapJson = preferences[ATTENDANCE_KEY]
            val currentMap = if (currentMapJson != null) {
                // Deserialize JSON string to a Map
                val type =
                    object : TypeToken<MutableMap<String, MutableList<AttendancePOJO>>>() {}.type
                Gson().fromJson(currentMapJson, type)
            } else {
                mutableMapOf<String, MutableList<AttendancePOJO>>()
            }

            // Update the map with new attendance
            val attendanceList = currentMap.getOrDefault(date, mutableListOf())

            // Check if the attendance for the student already exists on the specified date
            val isDuplicate = attendanceList.any { it.studentId == attendance.studentId }

            if (!isDuplicate) {
                // Add the attendance only if it's not a duplicate
                attendanceList.add(attendance)
                currentMap[date] = attendanceList

                // Serialize the updated map back to JSON and save it in DataStore
                preferences[ATTENDANCE_KEY] = Gson().toJson(currentMap)
            }
        }
    }

    // Function to save attendance data to DataStore
    suspend fun updateAttendance(attendanceList: List<AttendancePOJO>) {

        val date = attendanceList[0].date;
        context.dataStore.edit { preferences ->
            val currentMapJson = preferences[ATTENDANCE_KEY]
            val currentMap = if (currentMapJson != null) {
                // Deserialize JSON string to a Map
                val type =
                    object : TypeToken<MutableMap<String, MutableList<AttendancePOJO>>>() {}.type
                Gson().fromJson(currentMapJson, type)
            } else {
                mutableMapOf<String, MutableList<AttendancePOJO>>()
            }


            currentMap[date] = attendanceList.toMutableList()

            // Serialize the updated map back to JSON and save it in DataStore
            preferences[ATTENDANCE_KEY] = Gson().toJson(currentMap)
        }
    }

    // Function to retrieve the attendance map from DataStore
    fun getAttendanceMap(): Flow<Map<String, List<AttendancePOJO>>> {
        return context.dataStore.data.map { preferences ->
            val currentMapJson = preferences[ATTENDANCE_KEY]
            if (currentMapJson != null) {
                Gson().fromJson(
                    currentMapJson,
                    object : TypeToken<Map<String, List<AttendancePOJO>>>() {}.type
                )
            } else {
                emptyMap()
            }
        }
    }

    suspend fun clearAttendanceData() {
        context.dataStore.edit { preferences ->
            // Set the key's value to an empty JSON object (i.e., empty map)
            preferences[ATTENDANCE_KEY] = Gson().toJson(emptyMap<String, List<AttendancePOJO>>())
        }
    }

}