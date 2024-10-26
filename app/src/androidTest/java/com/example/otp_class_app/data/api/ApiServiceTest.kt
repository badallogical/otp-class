package com.example.otp_class_app.data.api

import com.harekrishna.otpClasses.data.api.ApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ApiServiceTest {

    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        // Use the actual base URL for the ApiService
        apiService = com.harekrishna.otpClasses.data.api.ApiService
    }

    @Test
    fun getAttendanceResponsesTest() = runBlocking {
        // Call the real APIg
        val result = apiService.getAttendanceResponses("9532945033")

        // Print or log the result for inspection
        println("API Response: $result")

        // Make assertions based on expected API behavior
        // Replace the expected data with what the API should return for "123"
        assert(result.isNotEmpty()) // Basic check to ensure data is returned
    }
}
