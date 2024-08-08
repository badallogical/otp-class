package com.example.otp_class_app.api

import android.util.Log
import com.example.otp_class_app.models.StudentDTO
import com.example.otp_class_app.models.StudentPOJO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.withContext

object ApiService {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbxob1SA8266rWuhkLpMjDReCQnh7Ag3DE7GEtT4-PxJjtreM42gMzwUjwmrEz70QwWi/exec"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .build()

    suspend fun registerStudent(student: StudentDTO): Boolean {
        return withContext(Dispatchers.IO) {
            val jsonObject = JSONObject().apply {
                put("type", "registerStudent") // Add the request type
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


    fun getStudents(onResponse: (List<StudentPOJO>?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val request = Request.Builder()
                .url(BASE_URL)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.let { responseBody ->
                    val jsonString = responseBody.string()
                    val studentListType = object : TypeToken<List<StudentPOJO>>() {}.type
                    val studentList: List<StudentPOJO> = Gson().fromJson(jsonString, studentListType)
                    onResponse(studentList)
                    Log.d("ApiService", studentList.toString())
                } ?: run {
                    onResponse(null)
                }
            } else {
                Log.e("ApiService", "GET request failed")
                onResponse(null)
            }
        }
    }

    fun postAttendance(studentId: String, date: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = JSONObject().apply {
                put("type", "MarkAttendance")
                put("attendance", JSONObject().apply {
                    put("studentID", studentId)
                    put("date", date)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = jsonObject.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onError(Exception("POST request failed: ${response.code}"))
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
