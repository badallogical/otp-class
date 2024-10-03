package com.example.otp_class_app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.otp_class_app.data.models.RegistrationStatus
import com.example.otp_class_app.data.models.StudentDTO
import com.example.otp_class_app.data.models.StudentPOJO


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
    suspend fun getStudentByPhone(phone: String): StudentPOJO?

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
    suspend fun getAllStudents(): List<StudentPOJO>?

    // It will get the list of registrations done by date.
    @Query("SELECT date, count(*) as counts,0 as synced from students group by date order by date DESC")
    suspend fun getRegistrationList(): List<RegistrationStatus>?

    // It will load the initial registration data that will later make the calling report.
    @Query("SELECT _name as name, _phone as phone from students where date = :date order by date desc")
    suspend fun getRegistrations(date: String) : List<Registration>?

    @Query("SELECT * from students where date = :date and _by =:userName")
    suspend fun getFullRegistrationsByDate(date : String, userName : String ) : List<StudentDTO>?



}


data class Registration(
    val name: String,
    val phone: String,
)

