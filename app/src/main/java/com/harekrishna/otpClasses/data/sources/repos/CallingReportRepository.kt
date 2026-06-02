package com.harekrishna.otpClasses.data.sources.repos

import android.util.Log
import com.harekrishna.otpClasses.data.sources.db.dao.CallingReportDao
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.data.sources.db.dao.StudentDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CallingReportRepository @Inject constructor(
    private val callingReportDao: CallingReportDao,
    private val studentDao: StudentDao
) {

    suspend fun insertCallingReport(callingReport: CallingReportPOJO) {
        callingReportDao.insert(callingReport)
    }

    suspend fun deleteCallingReportByPhone(phone: String) {
        callingReportDao.delete(phone)
    }

    fun getCallingReportsByDate(date: String): Flow<List<CallingReportPOJO>> = flow {
        // 1. Get current calling reports from DB
        var reports = callingReportDao.getCallingReportByDate(date).first()

        // 2. If no reports, try to sync from students table
        if (reports.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                val studentsForDate = studentDao.getStudentsByDate(date)
                studentsForDate.forEach { student ->
                    // Check if already in calling reports to avoid duplicates
                    if (callingReportDao.getCallingReportByPhone(student.phone) == null) {
                        val newReport = CallingReportPOJO(
                            phone = student.phone,
                            name = student.name,
                            status = "Pending",
                            attendanceCount = 0,
                            date = date,
                            isInvited = false,
                            isActive = true,
                            feedback = "",
                            tag = "",
                            remark = "",
                            photoUri = student.photoUri
                        )
                        callingReportDao.insert(newReport)
                    }
                }
            }
            // Fetch updated list
            reports = callingReportDao.getCallingReportByDate(date).first()
        }

        emit(reports ?: emptyList())
    }


    suspend fun updateCallingReportDate(phone: String, date: String) {
        callingReportDao.updateCallingReportDate(phone, date)
    }

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

    suspend fun updateCallingReportRemark(phone : String, remark : String ){
        callingReportDao.updateCallingReportRemark(phone, remark)
    }
}
