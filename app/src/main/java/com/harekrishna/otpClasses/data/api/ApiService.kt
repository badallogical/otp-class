package com.harekrishna.otpClasses.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.harekrishna.otpClasses.data.models.AttendanceDTO
import com.harekrishna.otpClasses.data.models.ReportDTO
import com.harekrishna.otpClasses.data.models.StudentDTO
import com.harekrishna.otpClasses.data.models.StudentPOJO
import com.harekrishna.otpClasses.data.models.UserAttendance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object ApiService {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbxcLISgKYmgXoMyPcWlNWOCJegKrIPKgj2JMMZurWlJ5JIwKln43ddIxtUlKRXn6Djf/exec"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        })
        .connectTimeout(30, TimeUnit.SECONDS)  // Time to establish a connection
        .readTimeout(45, TimeUnit.SECONDS)     // Time to read the data
        .writeTimeout(30, TimeUnit.SECONDS)    // Time to write data to the connection .connectTimeout(30, TimeUnit.SECONDS)  // Time to establish a connection
        .build()

    suspend fun registerStudent(student: StudentDTO, updated : Boolean = false ): Response {
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
                    put("by",student.by)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = jsonObject.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            return@withContext response
        }
    }


    suspend fun getStudents(): List<StudentDTO> {
        return withContext(Dispatchers.IO) {

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
                        val studentListType = object : TypeToken<List<StudentDTO>>() {}.type
                        val studentList: List<StudentDTO> = Gson().fromJson(jsonString, studentListType) ?: emptyList()
                        Log.d("ApiService", studentList.toString())
                        studentList
                    }?: emptyList<StudentDTO>()
                } else {
                    Log.e("ApiService", "GET request failed with code: ${response.code}")
                    emptyList<StudentDTO>()
                }
            } catch (e: Exception) {
                Log.e("ApiService", "GET request failed: ${e.message}")
                emptyList<StudentDTO>()
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

    suspend fun fetchStudentsByBy(by: String): List<StudentDTO> {
        return withContext(Dispatchers.IO) {
            try {
                // Build the request URL with the facilitator as a query parameter
                val url = "$BASE_URL?by=$by"
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
                        val studentDataType = object : TypeToken<List<StudentDTO>>() {}.type
                        val studentsList: List<StudentDTO> = Gson().fromJson(jsonString, studentDataType)

                        Log.d("ApiService Fetch By By", "Parsed Reports: $studentsList")
                        return@withContext studentsList
                    } ?: run {
                        Log.e("ApiService Fetch By By", "Response body is null")
                        return@withContext emptyList<StudentDTO>() // Return an empty list if response body is null
                    }
                } else {
                    Log.e("ApiService", "GET request failed with code: ${response.code}")
                    return@withContext emptyList<StudentDTO>() // Return an empty list on failure
                }
            } catch (e: Exception) {
                Log.e("ApiService", "GET request failed: ${e.message}")
                return@withContext emptyList<StudentDTO>() // Return an empty list in case of an exception
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

    suspend fun postBulkAttendance(attendanceList: List<AttendanceDTO>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val jsonArray = JSONArray()
                attendanceList.forEach { attendance ->

                    jsonArray.put(JSONObject().apply {
                        put("studentID", attendance.studentId)
                        put("date", attendance.date)
                        put("regDate", attendance.regDate)
                    })
                }

                val jsonObject = JSONObject().apply {
                    put("type", "BulkMarkAttendance")
                    put("attendanceList", jsonArray)
                }

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = jsonObject.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() // Read response body safely

                if (response.isSuccessful) {
                    Log.d("ApiService", "Response: $responseBody") // Log actual response
                    return@withContext true
                } else {
                    Log.d("ApiService", "POST request failed with code: ${response.code}, response: $responseBody")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e("ApiService", "POST request failed: ${e.message}")
                return@withContext false
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
                        put("regDate", attendance.regDate)
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

    // getAttendances
    suspend fun getAttendanceResponses(userID: String): List<UserAttendance> {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_URL?userID=$userID"
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        val jsonString = responseBody.string()
                        Log.d("ApiService JSON", "JSON String: $jsonString")

                        val attendanceListType = object : TypeToken<List<UserAttendance>>() {}.type
                        val attendanceList: List<UserAttendance> = Gson().fromJson(jsonString, attendanceListType) ?: emptyList()
                        Log.d("ApiService", attendanceList.toString())
                        Log.d("followup", "Online : ${attendanceList.toString()}")
                        attendanceList
                    } ?: emptyList()
                } else {
                    Log.e("ApiService", "GET request failed with code: ${response.code}")
                    emptyList()
                }
            } catch (e: SocketTimeoutException) {
                Log.e("ApiService", "GET request timed out: ${e.message}")
                emptyList()  // Handle timeout separately if needed
            } catch (e: Exception) {
                Log.e("ApiService", "GET request failed: ${e.message}")
                emptyList()
            }
        }
    }


    private const val BATCH_SIZE = 10  // Adjust as needed

    suspend fun syncAttendance(
        attendanceMap: Map<String, List<AttendanceDTO>>,
        onSave: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        var success = true

        attendanceMap.forEach { (date, attendanceList) ->
            val totalForDay = attendanceList.size
            val day = attendanceMap.keys.indexOf(date) + 1

            // Split into smaller batches
            attendanceList.chunked(BATCH_SIZE).forEach { batch ->
                Log.d("attendance", batch.toString())

                val result = postBulkAttendance(batch)
                if (result) {
                    Log.d("ApiService", "Attendance Posted batch count  ${batch.size}")
                    onSave(batch.size)
                } else {
                    Log.d("ApiService", "Attendance Posting Failed ")
                    success = false
                    onSave(-1)
                }

            }
        }
        return@withContext success
    }


    //            val results = attendanceList.map { attendance ->
//                async {
//                    val result = postAttendance(attendance)
//                    synchronized(progressLock) {
//                        if (result) {
//                            savedForDay++
//                            postedCount++
//                            val progress = (postedCount * 100) / totalAttendanceCount
//                            onProgressUpdate(progress, day, totalForDay, savedForDay)
//                        } else {
//                            success = false
//                            Log.e(
//                                "SyncService",
//                                "Failed to post attendance for studentID: ${attendance.studentId} on date: $date"
//                            )
//                        }
//                    }
//                    result
//                }
// }
// Await all results for the current day
//            results.awaitAll()

//            if (!success) return@withContext false
//        }


}
