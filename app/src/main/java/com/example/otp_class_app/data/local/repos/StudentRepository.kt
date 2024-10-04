package com.example.otp_class_app.data.local.repos

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.otp_class_app.MyApplication
import com.example.otp_class_app.data.api.ApiService
import com.example.otp_class_app.data.api.AttendanceDataStore
import com.example.otp_class_app.data.local.db.dao.CallingReportDao
import com.example.otp_class_app.data.local.db.dao.Registration
import com.example.otp_class_app.data.local.db.dao.StudentDao
import com.example.otp_class_app.data.models.CallingReportPOJO
import com.example.otp_class_app.data.models.RegistrationStatus
import com.example.otp_class_app.data.models.StudentDTO
import com.example.otp_class_app.data.models.StudentPOJO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Its the repository that will work for registrations.
class StudentRepository(
    private val studentDao: StudentDao,
    private val callingDao: CallingReportDao
) {

    // Insert a student into the database and update the callings
    suspend fun insertStudent(student: StudentDTO, updated: Boolean = false) {
        // save to local
        studentDao.insert(student)

        // update calling report
        val callingReport =
            CallingReportPOJO(student.phone, student.name, "status", 0, student.date)
        callingDao.insert(callingReport)


        // If internet then post it to Google sheet in a background thread.
//        CoroutineScope(Dispatchers.IO).launch {
//            if (MyApplication.checkInternetConnection()) {
//                // Sync with server in the background
//                ApiService.registerStudent(student, updated)
//            }
//        }
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
                    ApiService.registerStudent(student, true)
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
        callingDao.delete(phone)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncStudentData() {
        val remoteStudents = ApiService.getStudents().map { student ->
            val formatteddate = formatDateString(student.date)
            student.copy(date = formatteddate)
        }

        Log.d("StudentRepo", "Data Fetched From the remote ${remoteStudents.size}")

        // Store the data in the local database for future requests
        withContext(Dispatchers.IO) {
            Log.d("student Repo", "wring to db")
            remoteStudents.forEach { student ->
                Log.d("StudentRepo", "Inserting student: $student")
                studentDao.insert(student)
                Log.d("StudentRepo", "Student inserted: ${student.name}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateString(dateString: String): String {
        // Parse the ISO 8601 date string to an Instant (UTC date-time)
        val instant = Instant.parse(dateString)

        // Convert the Instant to LocalDate using system default time zone
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()

        // Format the LocalDate to "yyyy-MM-dd" pattern
        return localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    // Fetch all students as a list of StudentPOJO
    @RequiresApi(Build.VERSION_CODES.O)
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

    // Fetch list of registrations by date with counts
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getRegistrationList(): List<RegistrationStatus>? {


        val userData = AttendanceDataStore.getUserData().first()

        // Attempt to get the data from the local database (Room)
        var registrationCounts = userData.second?.let { studentDao.getRegistrationList(it) }

        // If local data is empty, check for internet and fetch from remote
        if (registrationCounts.isNullOrEmpty()) {
            if (MyApplication.checkInternetConnection()) {
                // Fetch remote data if available
                try {
                    // Sync student data from the remote source
                    syncStudentData()

                    // Get the updated data from the local database
                    registrationCounts = userData.second?.let { studentDao.getRegistrationList(it) }
                } catch (exception: Exception) {
                    // Handle network or API errors
                    exception.printStackTrace()
                }
            } else {
                // If no internet, return null or throw an error
                throw Exception("No internet connection")
            }
        }

        return registrationCounts
    }

    // Fetch registrations for a specific date
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getRegistrationsByDate(date: String): List<Registration>? {
        // Attempt to get registrations for the given date from local database (Room)
        var registrations = studentDao.getRegistrations(date)

        // If no data, fetch from remote if internet is available
        if (registrations.isNullOrEmpty()) {
            if (MyApplication.checkInternetConnection()) {
                // Sync data from the remote source
                try {
                    // Sync registrations from the remote source
                    syncStudentData()

                    // Get the updated registrations for the given date from the local database
                    registrations = studentDao.getRegistrations(date)
                } catch (exception: Exception) {
                    // Handle any network or API errors here
                    exception.printStackTrace()
                }
            } else {
                // If no internet, return null or throw an error
                throw Exception("No internet connection")
            }
        }

        return registrations
    }

    suspend fun syncLocalRegisterations(date: String) {
        withContext(Dispatchers.IO) {
            val userData = AttendanceDataStore.getUserData().first() // Get the first emitted value

            // Use let to safely work with user data
            val registrations: List<StudentDTO>? = userData.second?.let {
                // Fetch registrations from the database in the IO context
                studentDao.getFullRegistrationsByDate(date, it)
            }

            // Safely iterate over the fetched registrations and sync with the server
            registrations?.forEach { registration ->
                try {
                    // Perform the network operation (API call) within the IO context
                    Log.d("registration", "syncing : ${registration.toString()}")
                    val response = ApiService.registerStudent(registration)
                    Log.d("registration", "Response : ${response.isSuccessful}")
                } catch (e: Exception) {
                    // Handle any network-related errors here
                    e.printStackTrace()
                }
            }

            Log.d("registration", "sync over")
        }
    }


}