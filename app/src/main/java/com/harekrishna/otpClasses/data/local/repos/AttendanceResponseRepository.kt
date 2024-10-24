package com.harekrishna.otpClasses.data.local.repos

import android.os.Build
import androidx.annotation.RequiresApi
import com.harekrishna.otpClasses.data.local.db.dao.AttendanceDao
import com.harekrishna.otpClasses.data.local.db.dao.CallingReportDao
import com.harekrishna.otpClasses.data.models.AttendanceDate
import com.harekrishna.otpClasses.data.models.AttendanceResponse
import com.harekrishna.otpClasses.data.models.AttendanceWithDates
import com.harekrishna.otpClasses.data.models.AttendeeItem
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.ui.followup.FollowUpViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AttendanceResponseRepository(
    private val callingReportDao: CallingReportDao,
    private val attendanceDao: AttendanceDao
) {

    suspend fun insertAttendance(_phone: String, _date: String) {
        attendanceDao.insertAttendanceResponse(AttendanceResponse(_phone))
        attendanceDao.insertAttendanceDate(AttendanceDate(date = _date, attendancePhone = _phone))
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

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getAllLastFourWeekAttendees(): List<AttendeeItem> {
        val result = mutableListOf<AttendeeItem>()

        // Get the date range for the last four weeks
        val fourWeekRange = FollowUpViewModel.getLastFourWeeksRange()

        // Get all calling reports for the last four weeks
        val callingReports: List<CallingReportPOJO> = withContext(Dispatchers.IO) {
            callingReportDao.getReportsFromLastFourWeeks(fourWeekRange.first, fourWeekRange.second)
        }
        // Iterate through each calling report
        for (report in callingReports) {

            // Fetch the last four attendance dates for the current report's phone number
            val lastFourDatesOfAttendee = attendanceDao.getLastFourAttendanceDates(report.phone)

            result.add(
                AttendeeItem(
                    name = report.name,
                    phone = report.phone,
                    callingStatus = report.status,
                    attendances = lastFourDatesOfAttendee,
                    isActive = report.isActive,
                    isInvited = report.isInvited,
                    feedback = report.feedback.ifEmpty { "No Feedback Yet" },
                    registrationDate = report.date
                )
            )
        }

        return result
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