package com.example.otp_class_app.data.local.repos

import com.example.otp_class_app.data.local.db.dao.StudentDao
import com.example.otp_class_app.data.models.StudentDTO
import com.example.otp_class_app.data.models.StudentPOJO

class StudentRepository(private val studentDao: StudentDao) {

    suspend fun insert(student : StudentDTO){
        studentDao.insert(student);
    }

    suspend fun getAllStudents() : List<StudentPOJO>?{
        return studentDao.getAllStudents()
    }
}