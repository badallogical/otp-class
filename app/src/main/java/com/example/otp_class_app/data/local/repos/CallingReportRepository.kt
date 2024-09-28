package com.example.otp_class_app.data.local.repos

import com.example.otp_class_app.data.local.db.dao.CallingReportDao
import com.example.otp_class_app.data.models.CallingReportPOJO

class CallingReportRepository(private val callingReportDao: CallingReportDao) {

    suspend fun insert(callingReport : CallingReportPOJO){
        callingReportDao.insert(callingReport)
    }

    suspend fun getCallingReportByDate(date: String ) : List<CallingReportPOJO>?{
        return callingReportDao.getCallingReportByDate(date)
    }
}