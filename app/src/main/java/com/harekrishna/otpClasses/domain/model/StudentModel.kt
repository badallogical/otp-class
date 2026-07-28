package com.harekrishna.otpClasses.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Main StudentProfile document model mapping `students/{studentId}`
 */
data class Student(
    // Captures the Firestore document key automatically when reading
    @DocumentId
    val id: String = "",

    // Basic Details
    val name: String = "",
    val phone: String = "",

    @get:PropertyName("isPhoneWhatsappVerified")
    @set:PropertyName("isPhoneWhatsappVerified")
    var isPhoneWhatsappVerified: Boolean = false,

    val address: String? = null,
    val education: String? = null,
    val occupation: String? = null,

    // Classification (Stored as String in Firestore via Enums)
    val category: StudentCategory = StudentCategory.YOUTH,
    val interestLevel: InterestLevel? = null,

    // StudentProfile Status
    val status: StudentStatus = StudentStatus.ACTIVE,
    val inactiveReason: String? = null,
    val joinedOn: Timestamp? = null,
    val rejoinedOn: Timestamp? = null,

    // Nested Objects
    val registeredBy: UserSummary? = null,
    val facilitator: UserSummary? = null,
    val relocationDetails: RelocationDetails? = null,
    val metrics: StudentMetrics? = null,

    // System / Sync Tracking
    val updatedAt: Timestamp? = null,
    val syncStatus: SyncStatus? = null
)

// -----------------------------------------------------------------------------
// Nested Data Classes
// -----------------------------------------------------------------------------

data class UserSummary(
    val uid: String = "",
    val name: String = ""
)

data class RelocationDetails(
    val city: String = "",
    val state: String = "",
    val relocatedOn: Timestamp? = null,
    val reason: String? = null
)

data class StudentMetrics(
    val totalAttendanceCount: Int = 0,
    val currentYearAttendanceCount: Int = 0,
    val lastAttendanceDate: Timestamp? = null,
    val currentAttendanceStreak: Int = 0,
    val movedToDYSBy: MovedToDYSBy? = null,         // AUTO, <Name>
    val movedToDYSOn: Timestamp? = null
)

data class SyncStatus(
    val state: SyncState = SyncState.PENDING,
    val lastSyncedAt: Timestamp? = null,
    val errorMessage: String? = null
)

// -----------------------------------------------------------------------------
// Enums
// -----------------------------------------------------------------------------

enum class StudentCategory {
    YOUTH,
    CONGREGATION
}

enum class InterestLevel {
    HIGH,
    MEDIUM,
    LOW
}

enum class StudentStatus {
    ACTIVE,
    ON_HOLD,
    INACTIVE
}

enum class MovedToDYSBy {
    AUTO,
    FACILITATOR
}

enum class SyncState {
    PENDING,
    SYNCED,
    FAILED
}