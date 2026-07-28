package com.harekrishna.otpClasses.data.sources.repos

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.harekrishna.otpClasses.domain.model.Student
import com.harekrishna.otpClasses.domain.repository.RegistrationRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistrationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RegistrationRepository {

    private val studentsCollection = firestore.collection("students")

    override suspend fun registerStudent(student: Student): Result<String> = runCatching {
        val documentRef = if (student.id.isBlank()) {
            studentsCollection.document() // Generate a new document reference ID
        } else {
            studentsCollection.document(student.id)
        }

        // Stamp updatedAt automatically if missing
        val studentData = student.copy(
            id = documentRef.id,
            joinedOn = student.joinedOn ?: Timestamp.now(),
            updatedAt = Timestamp.now()
        )

        documentRef.set(studentData).await()
        documentRef.id
    }

    override suspend fun updateStudent(
        studentId: String,
        updates: Map<String, Any?>
    ): Result<Unit> = runCatching {

        require(studentId.isNotBlank()) { "StudentProfile ID cannot be empty for updates" }

        //  overwrite the updatedAt timestamp in the update payload
        val finalUpdates = updates.toMutableMap().apply {
            put("updatedAt", Timestamp.now())
        }

        studentsCollection.document(studentId)
            .update(finalUpdates)
            .await()
    }

    override suspend fun fetchStudentByPhone(phone: String): Result<Student?> = runCatching {
        val querySnapshot = studentsCollection
            .whereEqualTo("phone", phone)
            .limit(1)
            .get()
            .await()

        if (querySnapshot.isEmpty) {
            null
        } else {
            querySnapshot.documents.first().toObject(Student::class.java)
        }
    }
}