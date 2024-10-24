package com.harekrishna.otpClasses.data.local.repos

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.harekrishna.otpClasses.MyApplication
import com.harekrishna.otpClasses.data.api.ApiService
import com.harekrishna.otpClasses.data.api.AttendanceDataStore
import com.harekrishna.otpClasses.data.local.db.dao.AttendanceDao
import com.harekrishna.otpClasses.data.local.db.dao.CallingReportDao
import com.harekrishna.otpClasses.data.local.db.dao.Registration
import com.harekrishna.otpClasses.data.local.db.dao.StudentDao
import com.harekrishna.otpClasses.data.models.AttendanceDate
import com.harekrishna.otpClasses.data.models.AttendanceResponse
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.data.models.RegistrationStatus
import com.harekrishna.otpClasses.data.models.StudentDTO
import com.harekrishna.otpClasses.data.models.StudentPOJO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class StudentRepository(
    private val studentDao: StudentDao,
    private val callingDao: CallingReportDao,
    private val attendanceDao: AttendanceDao,
) {


    // Insert a student into the database and update the callings
    suspend fun insertStudent(student: StudentDTO, isInvited: Boolean = false) {
        // Save to local
        studentDao.insert(student)

        // Update calling report, if already existed.
        var callingReport = callingDao.checkIfReportExist(student.phone)
        if (callingReport == null) {
            callingReport = CallingReportPOJO(
                student.phone,
                student.name,
                "status",
                0,
                student.date,
                isInvited,
                true,
                "",
                "",
                ""
            )
        } else {
            callingReport = CallingReportPOJO(
                student.phone,
                student.name,
                callingReport.status,
                callingReport.attendanceCount,
                student.date,
                callingReport.isInvited,
                callingReport.isActive,
                callingReport.feedback,
                callingReport.tag,
                callingReport.remark
            )
        }

        callingDao.insert(callingReport)
    }

    suspend fun syncStudent(student: StudentDTO, updated: Boolean = false) {
        ApiService.registerStudent(student, updated)
    }

    // Get a student by phone as a Flow
    fun getStudentByPhone(phone: String): StudentPOJO? {
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

    suspend fun markAttendance(phoneNo : String, _date: String){

        attendanceDao.insertAttendanceResponse(AttendanceResponse(phoneNo))
        attendanceDao.insertAttendanceDate(AttendanceDate(date = _date, attendancePhone = phoneNo))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncStudentData() {
        val userData = AttendanceDataStore.getUserData().first();
        val phone = userData.second;

        val remoteStudents = ApiService.getStudents().map { student ->
            val formatteddate = formatDateString(student.date)
            student.copy(date = formatteddate)
        }

        Log.d("StudentRepo", "Data Fetched From the remote ${remoteStudents.size}")

        // Store the data in the local database for future requests
        withContext(Dispatchers.IO) {
            Log.d("student Repo", "writing to db")
            remoteStudents.forEach { student ->
                Log.d("StudentRepo", "Inserting student: $student")
                studentDao.insert(student.copy(sync = true))

                Log.d("StudentRepo", "Student inserted: ${student.name}")
            }
        }
    }

    // It will load the registration made from remote and insert to database, and update calling.
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun syncMyStudentData() {
        val userData = withContext(Dispatchers.IO) { AttendanceDataStore.getUserData().first() }
        Log.d("registration", "USER DATA ${userData.second}")

        val remoteStudents = withContext(Dispatchers.IO) {
            userData.second?.let {
                ApiService.fetchStudentsByBy(it).map { student ->
                    val formatteddate = formatDateString(student.date)
                    student.copy(date = formatteddate)
                }
            }
        }

        Log.d("registration", "Data Fetched From the remote ${remoteStudents?.size}")

        // Store the data in the local database for future requests
        withContext(Dispatchers.IO) {
            Log.d("student Repo", "writing to db")
            remoteStudents?.forEach { student ->
                Log.d("StudentRepo", "Inserting student: $student")

                // not only insert student but also update calling report
                insertStudent(student.copy(sync = true))

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
    fun getAllStudents(): Flow<List<StudentPOJO>> = flow {
        // Attempt to get data from the local database (Room)
        val localStudents = studentDao.getAllStudents().first() // Collect the first value from Flow

        // Emit local data if available
        if (localStudents.isNotEmpty()) {
            emit(localStudents)
        }

        // If local data is empty, fetch from remote
        if (localStudents.isEmpty()) {
            if (MyApplication.checkInternetConnection()) {
                try {
                    // Sync to get the remote data
                    syncStudentData()

                    // Get the updated data from the local database
                    val remoteStudents = studentDao.getAllStudents().first()
                    emit(remoteStudents) // Emit the remote data
                } catch (exception: Exception) {
                    // Handle any network or API errors here
                    exception.printStackTrace()
                    // Optionally emit an empty list or rethrow the exception
                    emit(emptyList())
                }
            } else {
                // If no network, throw an error or emit an empty list
                throw Exception("No internet connection")
            }
        }
    }


    // Fetch list of registrations by date with counts
    @RequiresApi(Build.VERSION_CODES.O)
    fun getRegistrationList(): Flow<List<RegistrationStatus>> = flow {
        val userData = AttendanceDataStore.getUserData().first()

        // Attempt to get the data from the local database (Room)
        val localRegistrationCounts = userData.second?.let {
            studentDao.getRegistrationList(it).first()
        } // Collect the first value
        Log.d("registration local", localRegistrationCounts.toString())


        // Emit local data if available
        if (localRegistrationCounts != null && localRegistrationCounts.isNotEmpty()) {
            emit(localRegistrationCounts)
        }

        // If local data is empty, check for internet and fetch from remote
        if (localRegistrationCounts.isNullOrEmpty()) {


            if (MyApplication.checkInternetConnection()) {
                try {
                    // Fetch remote data if available
                    syncMyStudentData()

                    // Get the updated data from the local database
                    val remoteRegistrationCounts =
                        userData.second?.let { studentDao.getRegistrationList(it).first() }
                    Log.d("registration after sync", remoteRegistrationCounts.toString())
                    emit(
                        remoteRegistrationCounts ?: emptyList()
                    ) // Emit remote data or an empty list
                } catch (exception: Exception) {
                    // Handle network or API errors
                    exception.printStackTrace()
                    // Optionally emit an empty list
                    emit(emptyList())
                }
            } else {
                // If no internet, throw an error or emit an empty list
                throw Exception("No internet connection")
            }
        }
    }


    // Fetch registrations for a specific date
    @RequiresApi(Build.VERSION_CODES.O)
    fun getRegistrationsByDate(date: String): Flow<List<Registration>> = flow {
        // Attempt to get registrations for the given date from local database (Room)
        val localRegistrations =
            studentDao.getRegistrations(date).first() // Collect the first value

        // Emit local data if available
        if (localRegistrations.isNotEmpty()) {
            emit(localRegistrations)
        }

        // If local data is empty, check for internet and fetch from remote
        if (localRegistrations.isEmpty()) {
            if (MyApplication.checkInternetConnection()) {
                try {
                    // Sync data from the remote source
                    syncStudentData()

                    // Get the updated registrations for the given date from the local database
                    val remoteRegistrations = studentDao.getRegistrations(date).first()
                    emit(remoteRegistrations) // Emit the remote data
                } catch (exception: Exception) {
                    // Handle any network or API errors here
                    exception.printStackTrace()
                    // Optionally emit an empty list or throw an error
                    emit(emptyList())
                }
            } else {
                // If no internet, throw an error or emit an empty list
                throw Exception("No internet connection")
            }
        }
    }


    suspend fun syncLocalRegistrations(date: String) {
        withContext(Dispatchers.IO) {
            // Get user data from DataStore
            val userData = AttendanceDataStore.getUserData().first() // Get the first emitted value

            // Check userData and fetch registrations using Flow
            userData.second?.let { userId ->
                // Collect registrations from the database using Flow but only take the first emission
                studentDao.getFullRegistrationsByDateNotSynced(date, userId)
                    .take(1) // Take only the first emission
                    .collect { registrations ->
                        // Safely iterate over the fetched registrations and sync with the server
                        registrations.forEach { registration ->
                            try {
                                // Perform the network operation (API call) within the IO context
                                Log.d("registration", "syncing : ${registration.toString()}")
                                val response = ApiService.registerStudent(registration)
                                if (response.isSuccessful) {
                                    // Update the local database to mark as synced
                                    studentDao.updateToSync(registration.phone)
                                    Log.d("registration", "Response : ${response.isSuccessful}")
                                } else {
                                    Log.e(
                                        "registration",
                                        "Failed to sync registration for ${registration.phone}"
                                    )
                                }
                            } catch (e: Exception) {
                                // Handle any network-related errors here
                                e.printStackTrace()
                            }
                        }
                    }
            } ?: run {
                Log.e("registration", "User data is null or empty")
            }

            Log.d("registration", "sync over")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getFullRegistrationsByDateNotSynced(
        date: String,
        userPhone: String
    ): Flow<List<StudentDTO>> {
        // Fetch unsynced registrations for the given date and userPhone from the local database (Room)
        return studentDao.getFullRegistrationsByDateNotSynced(date, userPhone)
    }


    suspend fun updateStudentToSynced(phone: String) {
        try {
            // Update the student's sync status in the local database
            studentDao.updateToSync(phone)
        } catch (exception: Exception) {
            // Handle any errors during the update
            exception.printStackTrace()
        }
    }
}
