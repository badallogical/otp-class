package com.example.otp_class_app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
}