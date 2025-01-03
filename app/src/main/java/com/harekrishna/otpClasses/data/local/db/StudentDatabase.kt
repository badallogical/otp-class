package com.harekrishna.otpClasses.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.harekrishna.otpClasses.data.local.db.dao.AttendanceDao
import com.harekrishna.otpClasses.data.local.db.dao.CallingReportDao
import com.harekrishna.otpClasses.data.local.db.dao.StudentDao
import com.harekrishna.otpClasses.data.models.AttendanceDate
import com.harekrishna.otpClasses.data.models.AttendanceResponse
import com.harekrishna.otpClasses.data.models.CallingReportPOJO
import com.harekrishna.otpClasses.data.models.StudentDTO

@Database(entities = [StudentDTO::class, CallingReportPOJO::class, AttendanceResponse::class, AttendanceDate::class], version = 3, exportSchema = true)
abstract class StudentDatabase : RoomDatabase(){
    // to get dao
    abstract fun getStudentDao(): StudentDao

    abstract fun getCallingReportDao(): CallingReportDao

    abstract fun getAttendanceResponseDao(): AttendanceDao

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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Add your migration here
                    .build()
                    .also { Instance = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new 'photoUri' column to the 'students' table
                database.execSQL("ALTER TABLE students ADD COLUMN photoUri TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new 'photoUri' column to the 'calling_report' table
                database.execSQL("ALTER TABLE calling_report ADD COLUMN photoUri TEXT")
            }
        }



    }

}
