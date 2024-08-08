package com.example.otp_class_app

import com.example.otp_class_app.api.ApiService
import com.example.otp_class_app.models.StudentDTO
import com.example.otp_class_app.models.StudentPOJO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class ApiServiceTest {

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        ApiService.BASE_URL = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testRegisterStudent() = runBlockingTest {
        val student = StudentDTO(
            name = "John Doe",
            phone = "1234567890",
            facilitator = "Jane Smith",
            batch = "Batch 1",
            profession = "Engineer",
            address = "123 Main St",
            date = "2023-08-07"
        )

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("Success")
        mockWebServer.enqueue(mockResponse)

        ApiService.registerStudent(student)

        val request = mockWebServer.takeRequest()
        assert(request.method == "POST")
        assert(request.path == "/")
    }

    @Test
    fun testGetStudents() = runBlockingTest {
        val students = listOf(
            StudentPOJO("John Doe", "1234567890", "Jane Smith", "Batch 1"),
            StudentPOJO("Alice Doe", "0987654321", "John Smith", "Batch 2")
        )
        val studentListType = object : TypeToken<List<StudentPOJO>>() {}.type
        val jsonResponse = Gson().toJson(students, studentListType)

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(jsonResponse)
        mockWebServer.enqueue(mockResponse)

        ApiService.getStudent { fetchedStudents ->
            assert(fetchedStudents == students)
        }

        val request = mockWebServer.takeRequest()
        assert(request.method == "GET")
        assert(request.path == "/")
    }
}
