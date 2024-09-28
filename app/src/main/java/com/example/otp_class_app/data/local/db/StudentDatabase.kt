package com.example.otp_class_app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.otp_class_app.data.local.db.dao.StudentDao
import com.example.otp_class_app.data.models.StudentDTO

@Database(entities = [StudentDTO::class], version = 1)
abstract class StudentDatabase : RoomDatabase(){
    // to get dao
    abstract fun getStudentDao(): StudentDao

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