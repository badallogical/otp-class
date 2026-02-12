package com.harekrishna.otpClasses.di.modules

import android.content.Context
import com.harekrishna.otpClasses.data.sources.db.StudentDatabase
import com.harekrishna.otpClasses.data.sources.db.dao.AttendanceDao
import com.harekrishna.otpClasses.data.sources.db.dao.CallingReportDao
import com.harekrishna.otpClasses.data.sources.db.dao.StudentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule{

    @Provides
    @Singleton
    fun provideDatabase( @ApplicationContext context : Context) : StudentDatabase{
        return StudentDatabase.getStudentDatabase(context)
    }

    @Provides
    fun provideStudentDao( database : StudentDatabase) : StudentDao {
        return database.getStudentDao()
    }

    @Provides
    fun provideCallingReportDao( database : StudentDatabase) : CallingReportDao {
        return database.getCallingReportDao()
    }

    @Provides
    fun provideAttendanceDao( database : StudentDatabase) : AttendanceDao {
        return database.getAttendanceResponseDao()
    }



}