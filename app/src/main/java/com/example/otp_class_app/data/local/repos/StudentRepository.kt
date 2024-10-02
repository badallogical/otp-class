package com.example.otp_class_app.data.local.repos

import android.util.Log
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.data.api.ApiService
import com.example.otp_class_app.data.local.db.dao.StudentDao
import com.example.otp_class_app.data.models.StudentDTO
import com.example.otp_class_app.data.models.StudentPOJO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Its the repository that will work for registrations.
class StudentRepository(private val studentDao: StudentDao) {

    // Insert a student into the database
    suspend fun insertStudent(student: StudentDTO) {
        // save to local
        studentDao.insert(student)

        // If internet then post it to Google sheet in a background thread.
        CoroutineScope(Dispatchers.IO).launch {
            if (MyApplication.checkInternetConnection()) {
                // Sync with server in the background
                ApiService.registerStudent(student)
            }
        }
    }

    // Get a student by phone
    suspend fun getStudentByPhone(phone: String): StudentPOJO? {
        return studentDao.getStudentByPhone(phone)
    }

    // Update a student record
    suspend fun updateStudent(student: StudentDTO) {
        studentDao.update(student)

        // Sync the update to the remote server in the background if internet is available
        CoroutineScope(Dispatchers.IO).launch {
            if (MyApplication.checkInternetConnection()) {
                try {
                    // Sync with the server
                    ApiService.registerStudent(student,true)
                } catch (exception: Exception) {
                    // Handle network or API errors here
                    exception.printStackTrace()
                }
            }
        }
    }

    // Delete a student by phone number
    suspend fun deleteStudentByPhone(phone: String) {
        studentDao.deleteByPhone(phone)
    }

    suspend fun syncStudentData(){
        val remoteStudents = ApiService.getStudents()
        Log.d("StudentRepo", "Data Fetched From the remote ${remoteStudents.size}")

        // Store the data in the local database for future requests
        withContext(Dispatchers.IO){
            Log.d("student Repo", "wring to db")
            remoteStudents.forEach { student ->
                Log.d("StudentRepo", "Inserting student: $student")
                studentDao.insert(student)
                Log.d("StudentRepo", "Student inserted: ${student.name}")
            }
        }
    }

    // Fetch all students as a list of StudentPOJO
    suspend fun getAllStudents(): List<StudentPOJO>? {
        // First, attempt to get data from the local database (Room)
        var students = studentDao.getAllStudents()

        // If local data is empty or not available, fetch from remote
        if (students.isNullOrEmpty()) {
            if (MyApplication.checkInternetConnection()) {
                // Update data from remote source
                try {

                    // synced to get the remote data.
                    syncStudentData()

                    // get the updated date from database
                    students = studentDao.getAllStudents()
                } catch (exception: Exception) {
                    // Handle any network or API errors here
                    exception.printStackTrace()
                }
            } else {
                // If no network, return null or throw an error
                throw Exception("No internet connection")
            }
        }

        return students;
    }
}