package com.harekrishna.otpClasses.data.local.repos

import android.content.Context
import com.harekrishna.otpClasses.data.local.db.StudentDatabase

class RepoContainer(private val context : Context) {

    private var database : StudentDatabase

    init{
        database = StudentDatabase.getStudentDatabase(context)
    }

    val studentRepository : StudentRepository by lazy{
        StudentRepository(database.getStudentDao(), database.getCallingReportDao())
    }

    val callingReportRepository : CallingReportRepository by lazy{
        CallingReportRepository(database.getCallingReportDao())
    }



}