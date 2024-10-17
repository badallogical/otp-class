package com.harekrishna.otpClasses.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.harekrishna.otpClasses.data.models.RegistrationStatus
import com.harekrishna.otpClasses.data.models.StudentDTO
import com.harekrishna.otpClasses.data.models.StudentPOJO
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentDTO)

    @Query("""
        SELECT 
            _name AS name, 
            _phone AS phone, 
            _facilitator AS facilitator, 
            _batch AS batch 
        FROM students 
        WHERE phone = :phone
    """)
    fun getStudentByPhone(phone: String): StudentPOJO?


    @Update
    suspend fun update(student: StudentDTO)

    @Query("DELETE FROM students WHERE _phone = :phone")
    suspend fun deleteByPhone(phone: String)

    @Query("""
        SELECT
            _name AS name, 
            _phone AS phone, 
            _facilitator AS facilitator, 
            _batch AS batch 
        FROM students
    """)
    fun getAllStudents(): Flow<List<StudentPOJO>>

    // It will get the list of registrations done by date.
    @Query("""
    SELECT date, 
           COUNT(*) AS counts, 
           MIN(sync) AS synced 
    FROM students 
    WHERE _by = :by 
    GROUP BY date 
    ORDER BY date DESC
""")
    fun getRegistrationList(by: String): Flow<List<RegistrationStatus>>

    @Query("""
    SELECT *
    FROM students 
    WHERE _by = :by
    ORDER BY date DESC
""")
    fun getFullRegistrationByBy(by : String) : Flow<List<StudentDTO>>

    // It will load the initial registration data that will later make the calling report.
    @Query("SELECT _name AS name, _phone AS phone FROM students WHERE date = :date ORDER BY date DESC")
    fun getRegistrations(date: String): Flow<List<Registration>>

    @Query("SELECT * FROM students WHERE date = :date AND _by = :by")
    fun getFullRegistrationsByDate(date: String, by: String): Flow<List<StudentDTO>>

    @Query("SELECT * FROM students WHERE date = :date AND _by = :by AND sync = 0")
    fun getFullRegistrationsByDateNotSynced(date: String, by: String): Flow<List<StudentDTO>>

    @Query("UPDATE students SET sync = 1 WHERE _phone = :phone")
    suspend fun updateToSync(phone: String)
}


data class Registration(
    val name: String,
    val phone: String,
)

