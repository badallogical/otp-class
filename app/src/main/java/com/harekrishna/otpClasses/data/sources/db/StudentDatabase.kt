package com.harekrishna.otpClasses.data.sources.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.harekrishna.otpClasses.data.models.*
import com.harekrishna.otpClasses.data.sources.db.dao.*

@Database(
    entities = [
        StudentDTO::class,
        CallingReportPOJO::class,
        AttendanceResponse::class,
        AttendanceDate::class,
        SangkirtanStudentDTO::class
    ],
    version = 5,
    exportSchema = true
)
abstract class StudentDatabase : RoomDatabase() {

    abstract fun getStudentDao(): StudentDao
    abstract fun getCallingReportDao(): CallingReportDao
    abstract fun getAttendanceResponseDao(): AttendanceDao
    abstract fun getSangkirtanStudentDao(): SangkirtanStudentDao

    companion object {
        @Volatile
        private var Instance: StudentDatabase? = null

        fun getStudentDatabase(context: Context): StudentDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context = context,
                    klass = StudentDatabase::class.java,
                    name = "devotees"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { Instance = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE students ADD COLUMN photoUri TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE calling_report ADD COLUMN photoUri TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN janCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN febCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN marCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN aprCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN mayCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN junCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN julCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN augCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN sepCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN octCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN novCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN decCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_response ADD COLUMN totalCount INTEGER NOT NULL DEFAULT 0")

                database.execSQL("ALTER TABLE attendance_dates ADD COLUMN present INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE attendance_dates ADD COLUMN leftEarly INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_dates ADD COLUMN leftEarlyTime TEXT")
                database.execSQL("ALTER TABLE attendance_dates ADD COLUMN deleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE attendance_dates ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sangkirtan_students` (
                        `_name` TEXT NOT NULL, 
                        `_phone` TEXT NOT NULL, 
                        `_category` TEXT NOT NULL, 
                        `_location` TEXT NOT NULL, 
                        `_date` TEXT NOT NULL, 
                        `_by` TEXT, 
                        `sync` INTEGER NOT NULL DEFAULT 0, 
                        `photoUri` TEXT, 
                        PRIMARY KEY(`_phone`)
                    )
                """)
            }
        }
    }
}
