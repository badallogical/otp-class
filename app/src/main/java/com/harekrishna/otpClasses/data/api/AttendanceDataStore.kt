package com.harekrishna.otpClasses.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.models.AttendancePOJO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

object AttendanceDataStore {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "attendance")
    private val context: Context by lazy { MyApplication.applicationContext() }

    // Define keys for storing user data
    private val NAME_KEY = stringPreferencesKey("user_name")
    private val PHONE_KEY = stringPreferencesKey("user_phone")

    // Define a key for your attendance map (using JSON String representation)
    private val ATTENDANCE_KEY = stringPreferencesKey("attendance_map")

    private val DATE_KEY = stringSetPreferencesKey("date_list")

    // Define keys for the  settings
    private val WELCOME_MESSAGE_KEY = stringPreferencesKey("welcome_message")
    private val THANKS_MESSAGE_KEY = stringPreferencesKey("thanks_message")
    private val WELCOME_IMAGE_URI_KEY = stringPreferencesKey("welcome_image_uri")
    private val THANKS_IMAGE_URI_KEY = stringPreferencesKey("thanks_image_uri")


    // Function to save attendance data to DataStore
    suspend fun saveNewAttendance(attendance: AttendancePOJO) {

        val date = attendance.date
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



    suspend fun clearAttendanceData() {
        context.dataStore.edit { preferences ->
            // Set the key's value to an empty JSON object (i.e., empty map)
            preferences[ATTENDANCE_KEY] = Gson().toJson(emptyMap<String, List<AttendancePOJO>>())
        }
    }

    // Function to save name and phone to DataStore
    suspend fun saveUserData(name: String, phone: String) {
        context.dataStore.edit { preferences ->
            preferences[NAME_KEY] = name
            preferences[PHONE_KEY] = phone
        }
    }

    // Function to retrieve name and phone from DataStore
    fun getUserData(): Flow<Pair<String?, String?>> {
        return context.dataStore.data.map { preferences ->
            val name = preferences[NAME_KEY]
            val phone = preferences[PHONE_KEY]
            Pair(name, phone)
        }
    }

    // Function to clear user data
    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(NAME_KEY)
            preferences.remove(PHONE_KEY)
        }
    }

    // Add a new date
    suspend fun addDate(date: String) {
        context.dataStore.edit { preferences ->
            val currentDates = preferences[DATE_KEY] ?: emptySet()
            preferences[DATE_KEY] = currentDates + date // Add date to the set
        }
    }

    // Remove a date
    suspend fun removeDate(date: String) {
        context.dataStore.edit { preferences ->
            val currentDates = preferences[DATE_KEY] ?: emptySet()
            preferences[DATE_KEY] = currentDates - date // Remove date from the set
        }
    }

    // Retrieve the list of dates
    val getDates: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[DATE_KEY] ?: emptySet() // Get the set of dates
        }

    // Check if a date exists
    suspend fun dateExists(date: String): Boolean {
        val currentDates = context.dataStore.data
            .map { preferences -> preferences[DATE_KEY] ?: emptySet() }
            .first() // Get the first emitted value
        return date in currentDates // Check if the date exists
    }


    // Function to save a setting in DataStore
    suspend fun saveWelcomeMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[WELCOME_MESSAGE_KEY] = message
        }
    }

    suspend fun saveThanksMessage(message: String) {
        context.dataStore.edit { preferences ->
            preferences[THANKS_MESSAGE_KEY] = message
        }
    }

    suspend fun saveWelcomeImageUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[WELCOME_IMAGE_URI_KEY] = uri
        }
    }

    suspend fun saveThanksImageUri(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[THANKS_IMAGE_URI_KEY] = uri
        }
    }

    // Retrieve settings from DataStore
    suspend fun getWelcomeMessage(): String {
        val preferences = context.dataStore.data.first() // This suspends until data is available
        return preferences[WELCOME_MESSAGE_KEY] ?: ""
    }

    suspend fun getThanksMessage(): String {
        val preferences = context.dataStore.data.first() // This suspends until data is available
        return preferences[THANKS_MESSAGE_KEY] ?: ""
    }

    suspend fun getWelcomeImageUri(): String? {
        val preferences = context.dataStore.data.first() // This suspends until data is available
        return preferences[WELCOME_IMAGE_URI_KEY]
    }

    suspend fun getThanksImageUri(): String? {
        val preferences = context.dataStore.data.first() // This suspends until data is available
        return preferences[THANKS_IMAGE_URI_KEY]
    }




}