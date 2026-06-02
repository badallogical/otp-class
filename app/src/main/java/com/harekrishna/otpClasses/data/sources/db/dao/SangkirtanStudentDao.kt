package com.harekrishna.otpClasses.data.sources.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harekrishna.otpClasses.data.models.SangkirtanStudentDTO
import com.harekrishna.otpClasses.data.models.SangkirtanStudentPOJO
import com.harekrishna.otpClasses.data.models.SangkirtanRegistrationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SangkirtanStudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: SangkirtanStudentDTO)

    @Query("""
        SELECT
            _name AS name, 
            _phone AS phone, 
            _category AS category,
             _location AS location,
            _date AS date ,
            _by AS regBy
        FROM sangkirtan_students
    """)
    fun getAllSangkirtanStudents(): Flow<List<SangkirtanStudentPOJO>>

    @Query("""
        SELECT
            _name AS name, 
            _phone AS phone, 
            _category AS category,
             _location AS location,
            _date AS date ,
            _by AS regBy
        FROM sangkirtan_students
        WHERE _category = :category 
    """)
    fun getAllSangkirtanStudentsByCategory( category : String ): Flow<List<SangkirtanStudentPOJO>>

    @Query("""
        SELECT
            _name AS name, 
            _phone AS phone, 
            _category AS category,
             _location AS location,
            _date AS date ,
            _by AS regBy
        FROM sangkirtan_students
        WHERE _date = :date
    """)
    fun getStudentsByDate(date: String): Flow<List<SangkirtanStudentPOJO>>

    @Query("UPDATE sangkirtan_students SET sync = 1 WHERE _phone = :phone")
    suspend fun updateToSync(phone: String)

    @Query("""
        SELECT _date AS date, 
               COUNT(*) AS counts, 
               MIN(sync) AS synced 
        FROM sangkirtan_students 
        WHERE _by = :by 
        GROUP BY _date 
        ORDER BY _date DESC
    """)
    fun getRegistrationList(by: String): Flow<List<SangkirtanRegistrationStatus>>

    @Query("SELECT * FROM sangkirtan_students WHERE _date = :date AND _by = :by")
    fun getFullRegistrationsByDate(date: String, by: String): Flow<List<SangkirtanStudentDTO>>

    @Query("SELECT * FROM sangkirtan_students WHERE _date = :date AND _by = :by AND sync = 0")
    fun getFullRegistrationsByDateNotSynced(date: String, by: String): Flow<List<SangkirtanStudentDTO>>

    @Query("DELETE FROM sangkirtan_students WHERE _phone = :phone")
    suspend fun deleteByPhone(phone: String)
}
