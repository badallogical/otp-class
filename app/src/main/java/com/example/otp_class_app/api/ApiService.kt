package com.example.otp_class_app.api

import android.util.Log
import com.example.otp_class_app.models.StudentDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

object ApiService {
    private const val BASE_URL = "https://script.google.com/macros/s/AKfycbwizVXqu45nCOfHfqsE7EF8PkfjhPUAPluMlt7TFgyVXTpsr3lrxdc9fXmOBRLBeS6P/exec"

    private val client: OkHttpClient

    init {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    fun registerStudent(student: StudentDTO) {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = JSONObject().apply {
                put("name", student.name)
                put("phone", student.phone)
                put("facilitator", student.facilitator)
                put("batch", student.batch)
                put("profession", student.profession)
                put("address", student.address)
                put("date", student.date)
            }

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val body = jsonObject.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(BASE_URL)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Handle successful response
                print("Succesfull");
            } else {
                // Handle unsuccessful response
                print("not Succesfull");
            }
        }
    }
}
