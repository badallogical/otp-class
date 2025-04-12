package com.harekrishna.otpClasses.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.harekrishna.otpClasses.data.models.AttendanceDate
import com.harekrishna.otpClasses.data.models.AttendanceResponse
import com.harekrishna.otpClasses.data.models.AttendanceWithDates
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    // Insert the parent entity (AttendanceResponse)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttendanceResponse(attendanceResponse: AttendanceResponse)

    // Insert the child entity (AttendanceDate)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceDate(attendanceDate: AttendanceDate)

    // Get attendance response with its related dates
    @Transaction
    @Query("SELECT * FROM attendance_response WHERE phone = :phone")
    suspend fun getAttendanceWithDates(phone: String): AttendanceWithDates?


    @Query("SELECT * FROM attendance_dates WHERE date = :date")
     fun getAttendanceByDate(date: String): Flow<List<AttendanceDate>>

    @Query("SELECT DISTINCT(date) FROM attendance_dates")
     fun getAttendanceDates() : Flow<List<String>>


    // Delete attendance response and cascade delete attendance dates
    @Delete
    suspend fun deleteAttendanceResponse(attendanceResponse: AttendanceResponse)


    @Query("DELETE FROM attendance_dates WHERE date = :date AND attendancePhone = :phone")
    suspend fun deleteAttendanceDateByDateAndPhone(date: String, phone: String)

    // Optional for debugging
    @Query("SELECT * FROM attendance_dates WHERE date = :date AND attendancePhone = :phone")
    suspend fun findAttendanceDate(date: String, phone: String): List<AttendanceDate>

    // Get all attendances with their dates
    @Transaction
    @Query("SELECT * FROM attendance_response")
    fun getAllAttendancesWithDates(): List<AttendanceWithDates>

    // Get the last 4 attendances for a specific phone number
    @Transaction
    @Query("""
        SELECT ad.date FROM attendance_dates ad
        INNER JOIN attendance_response ar ON ad.attendancePhone = ar.phone
        WHERE ar.phone = :phone
        ORDER BY ad.date DESC
        LIMIT 4
    """)
    suspend fun getLastFourAttendanceDates(phone: String): List<String>

    @Query("SELECT count(*) FROM attendance_dates WHERE attendancePhone =:phone ORDER BY date DESC")
    suspend fun getAttendanceCount(phone: String): Int

    @Query("SELECT DISTINCT attendancePhone from attendance_dates where date = :date ")
    suspend fun getAttendeePresentOn(date : String) : List<String>

    @Query("SELECT count(*) FROM attendance_dates")
    fun isAttendanceEmpty() : Int

    @Query("SELECT attendancePhone FROM attendance_dates WHERE date BETWEEN :startDate AND :endDate")
    fun getAttendeeFromLastFourWeeks(startDate: String, endDate: String): List<String>

}