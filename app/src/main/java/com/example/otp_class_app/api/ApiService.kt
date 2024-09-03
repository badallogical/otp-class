package com.example.otp_class_app.api

import android.util.Log
import com.example.otp_class_app.models.AttendanceDTO
import com.example.otp_class_app.models.StudentDTO
import com.example.otp_class_app.models.StudentPOJO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

object ApiService {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbwim9RwFh1eS8NHTMHS79i8YT8EygMbdIBXjLFXs5KWX3FpeczEGRYmWFASfDdhVDFQ/exec"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .build()

    suspend fun registerStudent(student: StudentDTO, updated : Boolean = false ): Boolean {
        return withContext(Dispatchers.IO) {
            val jsonObject = JSONObject().apply {
                put("type", "registerStudent")
                put("updated", updated)// Add the request type
                put("student", JSONObject().apply {
                    put("name", student.name)
                    put("phone", student.phone)
                    put("facilitator", student.facilitator)
                    put("batch", student.batch)
                    put("profession", student.profession)
                    put("address", student.address)
                    put("date", student.date)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = jsonObject.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            return@withContext response.isSuccessful
        }
    }


    suspend fun getStudents(): List<StudentPOJO>? {
        return withContext(Dispatchers.IO) {
            val url = "$BASE_URL?phone=null"
            try {
                val request = Request.Builder()
                    .url(BASE_URL)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonString = responseBody.string()
                        Log.d("ApiService Josn","Json String :" + jsonString);
                        val studentListType = object : TypeToken<List<StudentPOJO>>() {}.type
                        val studentList: List<StudentPOJO> = Gson().fromJson(jsonString, studentListType)
                        Log.d("ApiService", studentList.toString())
                        return@withContext studentList
                    }
                } else {
                    Log.e("ApiService", "GET request failed with code: ${response.code}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("ApiService", "GET request failed: ${e.message}")
                return@withContext null
            }
        }
    }

    suspend fun findStudentByPhone(phoneNumber: String): StudentDTO? {
        return withContext(Dispatchers.IO) {
            try {
                // Add the phone number as a query parameter to the URL
                val url = "$BASE_URL?phone=$phoneNumber"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonString = responseBody.string()
                        Log.d("ApiService Josn", "Json String :" + jsonString)
                        val studentType = object : TypeToken<StudentDTO>() {}.type
                        val student: StudentDTO = Gson().fromJson(jsonString, studentType)
                        Log.d("ApiService", student.toString())
                        return@withContext student
                    }
                } else {
                    Log.e("ApiService", "GET request failed with code: ${response.code}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("ApiService", "GET request failed: ${e.message}")
                return@withContext null
            }
        }
    }



    suspend fun postAttendance(attendance : AttendanceDTO ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonObject = JSONObject().apply {
                    put("type", "MarkAttendance")
                    put("attendance", JSONObject().apply {
                        put("studentID", attendance.studentId)
                        put("date", attendance.date)
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = jsonObject.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    return@withContext true
                } else {
                    Log.e("ApiService", "POST request failed with code: ${response.code}")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "POST request failed: ${e.message}")
                return@withContext false
            }
        }
    }


    suspend fun syncAttendance(attendanceMap: Map<String, List<AttendanceDTO>>): Boolean {
        return withContext(Dispatchers.IO) {
            var success = true

            // Iterate over each date in the attendanceMap
            for ((date, attendanceList) in attendanceMap) {
                for (attendance in attendanceList) {
                    // Post each attendance entry
                    val result = postAttendance(attendance)
                    if (!result) {
                        success = false
                        // Optionally, you could log or handle failed attendance post requests here
                        Log.e("SyncService", "Failed to post attendance for studentID: ${attendance.studentId} on date: $date")
                        return@withContext false
                    }
                }
            }

            true
        }
    }


}
