package com.example.otp_class_app.data.local.repos

import com.example.otp_class_app.data.local.db.dao.CallingReportDao
import com.example.otp_class_app.data.models.CallingReportPOJO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallingReportRepository(private val callingReportDao: CallingReportDao) {

    // Insert a calling report
    suspend fun insertCallingReport(callingReport: CallingReportPOJO) {
        callingReportDao.insert(callingReport)
    }

    // Delete a calling report by phone
    suspend fun deleteCallingReportByPhone(phone: String) {
        callingReportDao.delete(phone)
    }

    // Get calling reports by a specific date
    // Get calling reports by a specific date
    fun getCallingReportsByDate(date: String): Flow<List<CallingReportPOJO>> {
        return callingReportDao.getCallingReportByDate(date).map { reports ->
            reports ?: emptyList() // Emit an empty list if reports are null
        }
    }


    // Update the date of a calling report by phone
    suspend fun updateCallingReportDate(phone: String, date: String) {
        callingReportDao.updateCallingReportDate(phone, date)
    }

    // Update the status of a calling report by phone
    suspend fun updateCallingReportStatus(phone: String, status: String) {
        callingReportDao.updateCallingReportStatus(phone, status)
    }

}