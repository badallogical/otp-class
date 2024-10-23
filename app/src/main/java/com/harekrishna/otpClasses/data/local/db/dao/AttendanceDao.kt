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

@Dao
interface AttendanceDao {

    // Insert the parent entity (AttendanceResponse)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceResponse(attendanceResponse: AttendanceResponse)

    // Insert the child entity (AttendanceDate)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceDate(attendanceDate: AttendanceDate)

    // Get attendance response with its related dates
    @Transaction
    @Query("SELECT * FROM attendance_response WHERE phone = :phone")
    suspend fun getAttendanceWithDates(phone: String): AttendanceWithDates?

    // Delete attendance response and cascade delete attendance dates
    @Delete
    suspend fun deleteAttendanceResponse(attendanceResponse: AttendanceResponse)

    // Get all attendances with their dates
    @Transaction
    @Query("SELECT * FROM attendance_response")
    fun getAllAttendancesWithDates(): List<AttendanceWithDates>
}