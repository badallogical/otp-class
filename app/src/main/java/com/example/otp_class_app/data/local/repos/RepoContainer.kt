package com.example.otp_class_app.data.local.repos

import android.content.Context
import com.example.otp_class_app.data.local.db.StudentDatabase
import com.example.otp_class_app.ui.registeration.Student

class RepoContainer(private val context : Context) {

    private var database : StudentDatabase

    init{
        database = StudentDatabase.getStudentDatabase(context)
    }

    val studentRepository : StudentRepository by lazy{
        StudentRepository(database.getStudentDao())
    }

    val callingReportRepository : CallingReportRepository by lazy{
        CallingReportRepository(database.getCallingReportDao())
    }

}