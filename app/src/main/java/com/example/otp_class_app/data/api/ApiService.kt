package com.example.otp_class_app.data.api

import android.util.Log
import com.example.otp_class_app.data.models.AttendanceDTO
import com.example.otp_class_app.data.models.ReportDTO
import com.example.otp_class_app.data.models.StudentDTO
import com.example.otp_class_app.data.models.StudentPOJO
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
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbzUgtZDJr3hPVJXvdLUs6AiyT6TWxV3rFEqGLjE7EqGylbVpyaN5kAh_w9920VYCowo/exec"

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

    suspend fun fetchStudentReportByFacilitator(facilitator: String): List<ReportDTO> {
        return withContext(Dispatchers.IO) {
            try {
                // Build the request URL with the facilitator as a query parameter
                val url = "$BASE_URL?facilitator=$facilitator"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                // Execute the HTTP request
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonString = responseBody.string()
                        Log.d("ApiService Json", "Json String: $jsonString")

                        // Parse the JSON response to a list of ReportDTO objects
                        val studentReportType = object : TypeToken<List<ReportDTO>>() {}.type
                        val studentReports: List<ReportDTO> = Gson().fromJson(jsonString, studentReportType)

                        Log.d("ApiService", "Parsed Reports: $studentReports")
                        return@withContext studentReports
                    } ?: run {
                        Log.e("ApiService", "Response body is null")
                        return@withContext emptyList<ReportDTO>() // Return an empty list if response body is null
                    }
                } else {
                    Log.e("ApiService", "GET request failed with code: ${response.code}")
                    return@withContext emptyList<ReportDTO>() // Return an empty list on failure
                }
            } catch (e: Exception) {
                Log.e("ApiService", "GET request failed: ${e.message}")
                return@withContext emptyList<ReportDTO>() // Return an empty list in case of an exception
            }
        }
    }

    suspend fun fetchStudentsByFacilitator(facilitator: String): List<StudentPOJO> {
        return withContext(Dispatchers.IO) {
            try {
                // Build the request URL with the facilitator as a query parameter
                val url = "$BASE_URL?studentFacilitator=$facilitator"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                // Execute the HTTP request
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonString = responseBody.string()
                        Log.d("ApiService Json", "Json String: $jsonString")

                        // Parse the JSON response to a list of ReportDTO objects
                        val studentDataType = object : TypeToken<List<StudentPOJO>>() {}.type
                        val studentsList: List<StudentPOJO> = Gson().fromJson(jsonString, studentDataType)

                        Log.d("ApiService", "Parsed Reports: $studentsList")
                        return@withContext studentsList
                    } ?: run {
                        Log.e("ApiService", "Response body is null")
                        return@withContext emptyList<StudentPOJO>() // Return an empty list if response body is null
                    }
                } else {
                    Log.e("ApiService", "GET request failed with code: ${response.code}")
                    return@withContext emptyList<StudentPOJO>() // Return an empty list on failure
                }
            } catch (e: Exception) {
                Log.e("ApiService", "GET request failed: ${e.message}")
                return@withContext emptyList<StudentPOJO>() // Return an empty list in case of an exception
            }
        }
    }

    suspend fun postStudentReport(report: ReportDTO): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Construct the JSON object
                val jsonObject = JSONObject().apply {
                    put("type", "Reporting")
                    put("report", JSONObject().apply {
                        put("name", report.name)
                        put("contact", report.contact)
                        put("facilitator", report.facilitator)
                        put("classlevel", report.classlevel)
                        put("chanting", report.chanting)
                        put("wrh", report.wrh)
                        put("book", report.book)
                        put("whh", report.whh)
                        put("active", report.active)
                        put("fourRegPrinciples", report.fourRegPrinciples)
                        put("percentageRegularity", report.percentageRegularity)
                        put("meetsFacilitator", report.meetsFacilitator)
                        put("sewa", report.sewa)
                        put("seriousness", report.seriousness)
                        put("attendsDailyEveningClass", report.attendsDailyEveningClass)
                        put("reasonForNotAttending", report.reasonForNotAttending ?: JSONObject.NULL)
                        put("noOfNightStay", report.noOfNightStay)
                        put("remarks", report.remarks ?: JSONObject.NULL)
                        put("lastMeetingDate", report.lastMeetingDate ?: JSONObject.NULL)
                    })
                }

                // Convert JSON object to request body
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = jsonObject.toString().toRequestBody(mediaType)

                // Build and execute the request
                val request = Request.Builder()
                    .url(BASE_URL) // Replace with your actual base URL
                    .post(body)
                    .build()

                val client = OkHttpClient()
                val response = client.newCall(request).execute()

                // Handle response
                if (response.isSuccessful) {
                    true
                } else {
                    Log.e("ApiService", "POST request failed with code: ${response.code}")
                    false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "POST request failed: ${e.message}")
                false
            }
        }
    }



    suspend fun postAttendance(attendance : AttendanceDTO): Boolean {
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
