package com.harekrishna.otpClasses.domain.repository

import com.harekrishna.otpClasses.domain.model.Student

interface RegistrationRepository {

    /**
     * Registers a new studentProfile or overwrites an existing document if the ID exists.
     * Returns the generated/assigned Document ID on success.
     */
    suspend fun registerStudent(student: Student): Result<String>

    /**
     * Updates specific fields of an existing studentProfile profile.
     */
    suspend fun updateStudent(studentId: String, updates: Map<String, Any?>): Result<Unit>

    /**
     * Fetches a studentProfile by their registered phone number.
     * Returns null if no matching studentProfile is found.
     */
    suspend fun fetchStudentByPhone(phone: String): Result<Student?>
}