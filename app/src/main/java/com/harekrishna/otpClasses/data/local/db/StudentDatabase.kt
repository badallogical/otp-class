package com.harekrishna.otpClasses.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.harekrishna.otpClasses.data.local.db.dao.AttendanceDao
import com.harekrishna.otpClasses.data.local.db.dao.CallingReportDao
import com.harekrishna.otpClasses.data.local.db.dao.StudentDao
import com.harekrishna.otpClasses.data.models.AttendanceDate
import com.harekrishna.otpClasses.data.models.AttendanceResponse
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.data.models.StudentDTO

@Database(entities = [StudentDTO::class, CallingReportPOJO::class, AttendanceResponse::class, AttendanceDate::class], version = 1)
abstract class StudentDatabase : RoomDatabase(){
    // to get dao
    abstract fun getStudentDao(): StudentDao

    abstract fun getCallingReportDao(): CallingReportDao

    abstract fun attendanceResponseDao(): AttendanceDao

    companion object{
        @Volatile
        private var Instance : StudentDatabase? = null

        fun getStudentDatabase(context: Context): StudentDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context = context,
                    klass = StudentDatabase::class.java,
                    name = "devotees"
                )
                    .build()
                    .also { Instance = it }
            }
        }

    }

}
