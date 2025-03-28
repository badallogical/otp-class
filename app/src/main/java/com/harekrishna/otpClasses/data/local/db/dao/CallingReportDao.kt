package com.harekrishna.otpClasses.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.data.models.StudentDTO
import kotlinx.coroutines.flow.Flow

@Dao
interface CallingReportDao  {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callingReport : CallingReportPOJO)

    @Query("DELETE FROM calling_report WHERE phone = :phone")
    suspend fun delete(phone: String)

    @Update
    suspend fun update(reportPOJO: CallingReportPOJO)

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

    @Query("UPDATE calling_report SET feedback = :feedback WHERE phone = :phone")
    suspend fun updateCallingReportFeedback(phone : String, feedback : String )

    @Query("UPDATE calling_report SET isActive = :isActive WHERE phone = :phone ")
    suspend fun updateCallingReportActivation( phone: String, isActive : Boolean )

    @Query("UPDATE calling_report SET remark = :remark WHERE phone = :phone ")
    suspend fun updateCallingReportRemark(phone: String, remark : String )

    @Query("Select * from calling_report where phone = :phone ")
    suspend fun checkIfReportExist(phone : String ): CallingReportPOJO?

    @Query("SELECT * FROM calling_report WHERE date BETWEEN :startDate AND :endDate")
    fun getReportsFromLastFourWeeks(startDate: String, endDate: String): List<CallingReportPOJO>
}