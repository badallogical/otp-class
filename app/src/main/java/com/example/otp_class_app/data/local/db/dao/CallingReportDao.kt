package com.example.otp_class_app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.otp_class_app.data.models.CallingReportPOJO

@Dao
interface CallingReportDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callingReport : CallingReportPOJO)

    @Query("select * from calling_report where date =:date")
    suspend fun getCallingReportByDate(date : String ) : List<CallingReportPOJO>?
}