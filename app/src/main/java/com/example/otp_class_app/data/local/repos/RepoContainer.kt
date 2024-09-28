package com.example.otp_class_app.data.local.repos

import android.content.Context
import com.example.otp_class_app.data.local.db.StudentDatabase

class RepoContainer(private val context : Context) {
    val studentRepository : StudentRepository by lazy{
        StudentRepository(StudentDatabase.getStudentDatabase(context).getStudentDao())
    }

}