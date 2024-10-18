package com.harekrishna.otpClasses.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import kotlinx.coroutines.flow.Flow

@Dao
interface CallingReportDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callingReport : CallingReportPOJO)

    @Query("DELETE FROM calling_report WHERE phone = :phone")
    suspend fun delete(phone: String)

    @Query("select * from calling_report where date =:date")
    fun getCallingReportByDate(date : String ) : Flow<List<CallingReportPOJO>?>

    @Query("select * from calling_report where phone =:phone")
    suspend fun getCallingReportByPhone(phone: String) : CallingReportPOJO?

    @Query("UPDATE calling_report SET date = :date WHERE phone = :phone")
    suspend fun updateCallingReportDate(phone : String, date : String )

    @Query("UPDATE calling_report SET status = :status WHERE phone = :phone")
    suspend fun updateCallingReportStatus(phone : String, status: String )

    @Query("UPDATE calling_report SET isInvited = :invited WHERE phone = :phone")
    suspend fun updateCallingReportInvite(phone : String, invited: Boolean)

    @Query("Select * from calling_report where phone = :phone ")
    suspend fun checkIfReportExist(phone : String ): CallingReportPOJO?
}