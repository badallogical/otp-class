package com.harekrishna.otpClasses.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.harekrishna.otpClasses.data.models.AttendanceDate
import com.harekrishna.otpClasses.data.models.AttendanceHistory
import com.harekrishna.otpClasses.data.models.AttendanceResponse
import com.harekrishna.otpClasses.data.models.AttendanceWithDates
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.data.models.MonthCount
import com.harekrishna.otpClasses.data.models.StudentAttendee
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceResponse(attendanceResponse: AttendanceResponse)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceDate(attendanceDate: AttendanceDate)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttendanceDateFromRemote(attendanceDate: AttendanceDate)

    @Query("SELECT * FROM attendance_response WHERE phone = :phone")
    suspend fun getAttendanceResponse(phone: String): AttendanceResponse?

    @Update
    suspend fun updateAttendanceResponse(attendanceResponse: AttendanceResponse)

    @Update
    suspend fun updateAttendanceDateTable(attendanceDate: AttendanceDate)

    @Query("""
    UPDATE attendance_dates 
    SET leftEarly = :left, 
        leftEarlyTime = :leftTime 
    WHERE attendancePhone = :phone 
      AND date = :date """)
    suspend fun updateAttendanceDateLeftField(
        phone: String,
        date: String,
        left: Boolean,
        leftTime: String
    )

    @Query("""
    UPDATE attendance_dates 
    SET deleted = :deleted
    WHERE attendancePhone = :phone 
      AND date = :date """)
    suspend fun updateAttendanceDateDeleteField(
        phone: String,
        date: String,
        deleted: Boolean,
    )



    // ✅ Count only non-deleted dates
    @Query("""
        SELECT strftime('%m', date) * 1 AS month, COUNT(*) AS count 
        FROM attendance_dates 
        WHERE attendancePhone = :phone AND deleted = 0
        GROUP BY month
    """)
    suspend fun getMonthlyAttendanceCounts(phone: String): List<MonthCount>

    @Transaction
    suspend fun markAttendance(phone: String, date: String) {

        // check existing entry
        val existing = getAttendanceResponse(phone)
        if (existing == null) {
            insertAttendanceResponse(AttendanceResponse(phone))
        }

        // mark attendance
        val attendanceDate = AttendanceDate(date = date, attendancePhone = phone)
        insertAttendanceDate(attendanceDate)


        val monthlyCounts = getMonthlyAttendanceCounts(phone)

        val jan = monthlyCounts.firstOrNull { it.month == 1 }?.count ?: 0
        val feb = monthlyCounts.firstOrNull { it.month == 2 }?.count ?: 0
        val mar = monthlyCounts.firstOrNull { it.month == 3 }?.count ?: 0
        val apr = monthlyCounts.firstOrNull { it.month == 4 }?.count ?: 0
        val may = monthlyCounts.firstOrNull { it.month == 5 }?.count ?: 0
        val jun = monthlyCounts.firstOrNull { it.month == 6 }?.count ?: 0
        val jul = monthlyCounts.firstOrNull { it.month == 7 }?.count ?: 0
        val aug = monthlyCounts.firstOrNull { it.month == 8 }?.count ?: 0
        val sep = monthlyCounts.firstOrNull { it.month == 9 }?.count ?: 0
        val oct = monthlyCounts.firstOrNull { it.month == 10 }?.count ?: 0
        val nov = monthlyCounts.firstOrNull { it.month == 11 }?.count ?: 0
        val dec = monthlyCounts.firstOrNull { it.month == 12 }?.count ?: 0

        val total = jan + feb + mar + apr + may + jun + jul + aug + sep + oct + nov + dec

        val response = AttendanceResponse(
            phone = phone,
            janCount = jan,
            febCount = feb,
            marCount = mar,
            aprCount = apr,
            mayCount = may,
            junCount = jun,
            julCount = jul,
            augCount = aug,
            sepCount = sep,
            octCount = oct,
            novCount = nov,
            decCount = dec,
            totalCount = total
        )

        updateAttendanceResponse(response)
    }

    // load single student attendance
    @Transaction
    suspend fun loadAttendance(phone: String, dates : List<String>) {

        // check existing entry
        val existing = getAttendanceResponse(phone)
        if (existing == null) {
            insertAttendanceResponse(AttendanceResponse(phone))
        }

        // Save all attendance of one student
        for (date in dates) {
            insertAttendanceDateFromRemote(
                AttendanceDate(
                    date = date,
                    attendancePhone = phone
                )
            )
        }

        // get the month count of attendance
        val monthlyCounts = getMonthlyAttendanceCounts(phone)
        val jan = monthlyCounts.firstOrNull { it.month == 1 }?.count ?: 0
        val feb = monthlyCounts.firstOrNull { it.month == 2 }?.count ?: 0
        val mar = monthlyCounts.firstOrNull { it.month == 3 }?.count ?: 0
        val apr = monthlyCounts.firstOrNull { it.month == 4 }?.count ?: 0
        val may = monthlyCounts.firstOrNull { it.month == 5 }?.count ?: 0
        val jun = monthlyCounts.firstOrNull { it.month == 6 }?.count ?: 0
        val jul = monthlyCounts.firstOrNull { it.month == 7 }?.count ?: 0
        val aug = monthlyCounts.firstOrNull { it.month == 8 }?.count ?: 0
        val sep = monthlyCounts.firstOrNull { it.month == 9 }?.count ?: 0
        val oct = monthlyCounts.firstOrNull { it.month == 10 }?.count ?: 0
        val nov = monthlyCounts.firstOrNull { it.month == 11 }?.count ?: 0
        val dec = monthlyCounts.firstOrNull { it.month == 12 }?.count ?: 0

        val total = jan + feb + mar + apr + may + jun + jul + aug + sep + oct + nov + dec

        // prepare and update the student response
        val response = AttendanceResponse(
            phone = phone,
            janCount = jan,
            febCount = feb,
            marCount = mar,
            aprCount = apr,
            mayCount = may,
            junCount = jun,
            julCount = jul,
            augCount = aug,
            sepCount = sep,
            octCount = oct,
            novCount = nov,
            decCount = dec,
            totalCount = total
        )

        updateAttendanceResponse(response)
    }


    @Transaction
    @Query("SELECT * FROM attendance_response WHERE phone = :phone")
    suspend fun getAttendanceWithDates(phone: String): AttendanceWithDates?

    @Query("SELECT * FROM attendance_dates WHERE date = :date AND deleted = 0")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceDate>>

    @Query("SELECT DISTINCT(date) FROM attendance_dates WHERE deleted = 0")
    fun getAttendanceDates(): Flow<List<String>>



    @Delete
    suspend fun deleteAttendanceResponse(attendanceResponse: AttendanceResponse)

    @Query("DELETE FROM attendance_dates WHERE date = :date AND attendancePhone = :phone")
    suspend fun deleteAttendanceDateByDateAndPhone(date: String, phone: String)

    @Query("SELECT * FROM attendance_dates WHERE date = :date AND attendancePhone = :phone AND deleted = 0")
    suspend fun findAttendanceDate(date: String, phone: String): List<AttendanceDate>

    // Fetch the Data for Attendance History
    @Query("""
        SELECT date, COUNT(*) as count 
        FROM attendance_dates 
        WHERE deleted = 0 
        GROUP BY date 
        ORDER BY date DESC
    """)
    suspend fun getAllAttendanceHistoryData(): List<AttendanceHistory>

    @Query("""
    SELECT
        r.phone AS phone,
        r.totalCount AS repeatedTimes,
        d.date AS date,
        d.leftEarly AS hasLeft,
        d.leftEarlyTime AS leftTime,
        s._name AS name,
        s._facilitator AS facilitator,
        s.date AS regDate,
        CASE WHEN r.totalCount = 1 THEN 1 ELSE 0 END AS isNew,
        r.phone as id
    FROM attendance_dates d
    INNER JOIN attendance_response r ON r.phone = d.attendancePhone
    LEFT JOIN students s ON s._phone = d.attendancePhone
    WHERE d.deleted = 0 AND d.date = :targetDate
    ORDER BY d.date DESC
""")
    suspend fun getStudentsDetailsAttendanceByDate(targetDate: String): List<StudentAttendee>


    @Transaction
    @Query("SELECT * FROM attendance_response")
    fun getAllAttendancesWithDates(): List<AttendanceWithDates>

    // ✅ Filter out deleted entries
    @Transaction
    @Query("""
        SELECT ad.date FROM attendance_dates ad
        INNER JOIN attendance_response ar ON ad.attendancePhone = ar.phone
        WHERE ar.phone = :phone AND ad.deleted = 0
        ORDER BY ad.date DESC
        LIMIT 4
    """)
    suspend fun getLastFourAttendanceDates(phone: String): List<String>

    // ✅ Removed ORDER BY since not needed in count, and added isDeleted filter
    @Query("SELECT count(*) FROM attendance_dates WHERE attendancePhone = :phone AND deleted = 0")
    suspend fun getAttendanceCount(phone: String): Int

    // ✅ Filter deleted entries
    @Query("SELECT DISTINCT attendancePhone FROM attendance_dates WHERE date = :date AND deleted = 0")
    suspend fun getAttendeePresentOn(date: String): List<String>

    @Query("SELECT count(*) FROM attendance_dates WHERE deleted = 0")
    fun isAttendanceEmpty(): Int

    @Query("""
        SELECT attendancePhone 
        FROM attendance_dates 
        WHERE date BETWEEN :startDate AND :endDate AND deleted = 0
    """)
    fun getAttendeeFromLastFourWeeks(startDate: String, endDate: String): List<String>

    // ✅ Optional: Get all unsynced entries (for syncing to Sheets)
    @Query("SELECT * FROM attendance_dates WHERE synced = 0 AND deleted = 0")
    suspend fun getUnsyncedAttendances(): List<AttendanceDate>

    @Update
    suspend fun updateAttendanceDates(attendances: List<AttendanceDate>)
}
