package com.harekrishna.otpClasses.data.local.repos

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.harekrishna.otpClasses.data.api.ApiService
import com.harekrishna.otpClasses.data.local.db.dao.AttendanceDao
import com.harekrishna.otpClasses.data.local.db.dao.CallingReportDao
import com.harekrishna.otpClasses.data.models.AttendanceDate
import com.harekrishna.otpClasses.data.models.AttendanceResponse
import com.harekrishna.otpClasses.data.models.AttendanceWithDates
import com.harekrishna.otpClasses.data.models.AttendeeItem
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.ui.followup.FollowUpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AttendanceResponseRepository(
    private val callingReportDao: CallingReportDao,
    private val attendanceDao: AttendanceDao
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getData(userPhone: String): List<AttendeeItem> {
        // Check if attendance data is empty
        if (attendanceDao.isAttendanceEmpty() == 0) {
            // This line suspends the coroutine and waits for fetchAndUpdateAttendance to complete
            withContext(Dispatchers.IO) {
                Log.d("AttendanceRepo", " Loading from remote")
                fetchAndUpdateAttendance(userPhone)
            }
        }

        // After fetchAndUpdateAttendance completes (or if attendance wasn't empty),
        // getAllLastFourWeekAttendeeAndRegistration() is called
        return withContext(Dispatchers.IO) {
            getAllLastFourWeekAttendeeAndRegistration()
        }
    }


    // It will not only get me the last four week attendee but all last four attendees also.
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllLastFourWeekAttendeeAndRegistration(): List<AttendeeItem> = coroutineScope {
        val result = mutableListOf<AttendeeItem>()

        // Get the date range for the last four weeks
        val fourWeekRange = FollowUpViewModel.getLastFourWeeksRange()

        val lastFourWeekAttendeesPhoneDeferred = async(Dispatchers.IO) {
            attendanceDao.getAttendeeFromLastFourWeeks(fourWeekRange.first, fourWeekRange.second).toMutableList()
        }

        // Fetch calling reports and last four weeks attendees in parallel
        val callingReportsDeferred = async(Dispatchers.IO) {
            callingReportDao.getReportsFromLastFourWeeks(fourWeekRange.first, fourWeekRange.second)
        }

        val lastFourWeekAttendeesPhone = lastFourWeekAttendeesPhoneDeferred.await()
        val callingReports = callingReportsDeferred.await()

        // Fetch data for each calling report in parallel
        callingReports.map { report ->
            async(Dispatchers.IO) {
                val lastFourDatesOfAttendee = attendanceDao.getLastFourAttendanceDates(report.phone)
                val attendanceCount = attendanceDao.getAttendanceCount(report.phone)

                if (lastFourWeekAttendeesPhone.contains(report.phone)) {
                    lastFourWeekAttendeesPhone.remove(report.phone)
                }

                AttendeeItem(
                    name = report.name,
                    phone = report.phone,
                    callingStatus = report.status,
                    attendances = lastFourDatesOfAttendee,
                    totalCount = attendanceCount,
                    isActive = report.isActive,
                    isInvited = report.isInvited,
                    feedback = report.feedback.ifEmpty { "No Feedback Yet" },
                    registrationDate = report.date
                )
            }
        }.awaitAll().let { result.addAll(it) }

        // Handle remaining attendees in parallel
        lastFourWeekAttendeesPhone.map { phone ->
            async(Dispatchers.IO) {
                val attendee = callingReportDao.getCallingReportByPhone(phone)
                val lastFourDatesOfAttendee = attendanceDao.getLastFourAttendanceDates(phone)
                val attendanceCount = attendanceDao.getAttendanceCount(phone)

                attendee?.let {
                    AttendeeItem(
                        name = it.name,
                        phone = it.phone,
                        callingStatus = it.status,
                        attendances = lastFourDatesOfAttendee,
                        totalCount = attendanceCount,
                        isActive = it.isActive,
                        isInvited = it.isInvited,
                        feedback = it.feedback.ifEmpty { "No Feedback Yet" },
                        registrationDate = it.date
                    )
                }
            }
        }.awaitAll().filterNotNull().let { result.addAll(it) }

        return@coroutineScope result
    }

    suspend fun fetchAndUpdateAttendance(userPhone : String ) {
        try{
            Log.d("followup", "fetch and Update called")
            val userId = userPhone

            // Fetch from remote
            val attendances = ApiService.getAttendanceResponses(userId)

            // Save to Local
            attendances.forEach { attendance ->
                insertMultipleAttendance(
                    attendance.phone, attendance.attendanceDates
                )
            }

            // Update Log on IO thread
            Log.d("followup fetching", attendances.toString())
        } catch (e: Exception) {
            Log.e("fetchAndUpdateAttendance", "Error fetching attendance: ${e.message}", e)
        }
    }


    suspend fun insertAttendance(_phone: String, _date: String) {
        attendanceDao.insertAttendanceResponse(AttendanceResponse(_phone))
        attendanceDao.insertAttendanceDate(AttendanceDate(date = _date, attendancePhone = _phone))
    }

    suspend fun insertMultipleAttendance(_phone: String, attendances : List<String>){
        attendanceDao.insertAttendanceResponse(AttendanceResponse(_phone))
        Log.d("followup", "Inserting ${attendances.toString()}")
        for(date in attendances){
            attendanceDao.insertAttendanceDate(AttendanceDate(date = date, attendancePhone = _phone))
        }
    }

    suspend fun deleteAttendance(attendanceDate: AttendanceDate) {
        attendanceDao.deleteAttendanceDate(attendanceDate)
    }

    suspend fun deleteAttendanceRecord(attendanceResponse: AttendanceResponse) {
        attendanceDao.deleteAttendanceResponse(attendanceResponse)
    }


    suspend fun getAttendanceWithDates(_phone: String): AttendanceWithDates? {
        val attendanceDates = attendanceDao.getAttendanceWithDates(_phone)

        return attendanceDates
    }


    suspend fun getLastFourAttendanceDate(phone : String ) : List<String>{
        return attendanceDao.getLastFourAttendanceDates(phone)
    }

    suspend fun getAttendeePresentOn(date: String) : List<String>{
        return attendanceDao.getAttendeePresentOn(date)
    }


    // It will not only get me the last four week attendee but all last four attendees also.
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllLastFourWeekAttendee(): List<AttendeeItem> = coroutineScope {
        val result = mutableListOf<AttendeeItem>()

        // Get the date range for the last four weeks
        val fourWeekRange = FollowUpViewModel.getLastFourWeeksRange()


        val lastFourWeekAttendeesPhoneDeferred = async(Dispatchers.IO) {
            attendanceDao.getAttendeeFromLastFourWeeks(fourWeekRange.first, fourWeekRange.second).toMutableList()
        }


        val lastFourWeekAttendeesPhone = lastFourWeekAttendeesPhoneDeferred.await()

        // Handle remaining attendees in parallel
        lastFourWeekAttendeesPhone.map { phone ->
            async(Dispatchers.IO) {
                val attendee = callingReportDao.getCallingReportByPhone(phone)
                val lastFourDatesOfAttendee = attendanceDao.getLastFourAttendanceDates(phone)
                val attendanceCount = attendanceDao.getAttendanceCount(phone)

                attendee?.let {
                    AttendeeItem(
                        name = it.name,
                        phone = it.phone,
                        callingStatus = it.status,
                        attendances = lastFourDatesOfAttendee,
                        totalCount = attendanceCount,
                        isActive = it.isActive,
                        isInvited = it.isInvited,
                        feedback = it.feedback.ifEmpty { "No Feedback Yet" },
                        registrationDate = it.date
                    )
                }
            }
        }.awaitAll().filterNotNull().let { result.addAll(it) }

        return@coroutineScope result
    }



    // Update the status of a calling report by phone
    suspend fun updateCallingReportStatus(phone: String, status: String) {
        callingReportDao.updateCallingReportStatus(phone, status)
    }

    suspend fun updateCallingReportInvited(phone :String, isInvited: Boolean ){
        callingReportDao.updateCallingReportInvite(phone, isInvited)
    }

    suspend fun updateCallingReportFeedback(phone : String, feedback : String ){
        callingReportDao.updateCallingReportFeedback(phone, feedback)
    }

    suspend fun updateCallingReportActivation(phone: String, isActive : Boolean ){
        callingReportDao.updateCallingReportActivation(phone, isActive)
    }


}