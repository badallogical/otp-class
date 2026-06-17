package com.harekrishna.otpClasses.data.api

import com.google.firebase.firestore.FirebaseFirestore
import com.harekrishna.otpClasses.data.models.StudentDTO
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val studentsCollection = firestore.collection("students")

    // Register a student
    suspend fun registerStudent(student: StudentDTO): Boolean {
        return try {
            studentsCollection.document(student.phone).set(student).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Modify a student
    suspend fun updateStudent(student: StudentDTO): Boolean {
        return try {
            studentsCollection.document(student.phone).update(
                mapOf(
                    "name" to student.name,
                    "facilitator" to student.facilitator,
                    "batch" to student.batch,
                    "profession" to student.profession,
                    "address" to student.address,
                    "photoUri" to student.photoUri
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get a student by phone
    suspend fun getStudentByPhone(phone: String): StudentDTO? {
        return try {
            val document = studentsCollection.document(phone).get().await()
            document.toObject(StudentDTO::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Delete a student
    suspend fun deleteStudent(phone: String): Boolean {
        return try {
            studentsCollection.document(phone).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
