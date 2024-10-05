package com.example.otp_class_app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.otp_class_app.data.models.CallingReportPOJO
import kotlinx.coroutines.flow.Flow

@Dao
interface CallingReportDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callingReport : CallingReportPOJO)

    @Query("DELETE FROM calling_report WHERE phone = :phone")
    suspend fun delete(phone: String)

    @Query("select * from calling_report where date =:date")
    fun getCallingReportByDate(date : String ) : Flow<List<CallingReportPOJO>?>

    @Query("UPDATE calling_report SET date = :date WHERE phone = :phone")
    suspend fun updateCallingReportDate(phone : String, date : String )

    @Query("UPDATE calling_report SET status = :status WHERE phone = :phone")
    suspend fun updateCallingReportStatus(phone : String, status: String )
}