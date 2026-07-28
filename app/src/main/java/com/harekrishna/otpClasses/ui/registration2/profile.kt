package com.harekrishna.otpClasses.ui.registration2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harekrishna.otpClasses.domain.model.InterestLevel
import com.harekrishna.otpClasses.domain.model.StudentCategory
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import com.harekrishna.otpClasses.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =========================================================================================
// ICON NOTE
// -----------------------------------------------------------------------------------------
// Only icons from `material-icons-core` are used below (Call, Edit, Create, DateRange,
// LocationOn, Person, Star, Email, ArrowBack) so this file compiles without adding the
// `material-icons-extended` dependency. If you already depend on material-icons-extended,
// swap: Email -> Icons.Filled.ChatBubbleOutline (Message), Person -> Icons.Filled.Groups
// (Facilitator), DateRange -> Icons.Filled.School (Batch/Education) for closer visual match
// to the screenshots.
//
// COLOR NOTE
// -----------------------------------------------------------------------------------------
// Every color below comes from MaterialTheme.colorScheme (OrangeLightColorScheme /
// OrangeDarkColorScheme in your Theme.kt) so light & dark mode are handled automatically.
// Status/category badge colors are derived from colorScheme.primary / tertiary / secondary /
// error via alpha blending — nothing is hardcoded outside the theme.
// =========================================================================================

// -----------------------------------------------------------------------------------------
// DOMAIN MODELS (mirrors students/{studentId} Firestore document)
// -----------------------------------------------------------------------------------------

enum class StudentStatus { ACTIVE, ON_HOLD, LEFT, GRADUATED }

data class RegisteredBy(val uid: String, val name: String)
data class BatchInfo(val id: String, val name: String)
data class FacilitatorInfo(val uid: String, val name: String)
data class RelocationDetails(val city: String, val dateLabel: String)

data class StudentMetrics(
    val totalAttendanceCount: Int,
    val currentYearAttendanceCount: Int,
    val lastAttendanceDateLabel: String,
    val isEligibleForDYS: Boolean,
    val movedToDYSBy: String? = null // "AUTO" | "Facilitator"
)

data class LifecycleLog(
    val deactivatedOnLabel: String? = null,
    val deactivatedBy: String? = null,
    val mode: String? = null, // "auto" | "manual"
    val reason: String? = null
)

data class StudentProfile(
    val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val education: String,
    val occupation: String,
    val category: StudentCategory,
    val interestLevel: InterestLevel,
    val status: StudentStatus,
    val isRelocated: Boolean = false,
    val relocationDetails: RelocationDetails? = null,
    val registeredBy: RegisteredBy,
    val firstRegistrationDateLabel: String,
    val lastRegistrationDateLabel: String,
    val eventsJoined: List<String> = emptyList(),
    val currentBatch: BatchInfo,
    val facilitator: FacilitatorInfo,
    val metrics: StudentMetrics,
    val lifecycleLog: LifecycleLog? = null
) {
    val initials: String
        get() = name.trim().split(" ").filter { it.isNotBlank() }
            .take(2).joinToString("") { it.first().uppercase() }
}

enum class CallLogStatus { CONNECTED, NO_ANSWER }

data class CallLogEntry(
    val callerName: String,
    val dateTimeLabel: String,
    val status: CallLogStatus,
    val note: String? = null
)

enum class FeedbackType { STATUS_CHANGE, COMMENT, REGISTRATION }

data class FeedbackEntry(
    val type: FeedbackType,
    val dateLabel: String,
    val description: String
) {
    val title: String
        get() = when (type) {
            FeedbackType.STATUS_CHANGE -> "Status Change"
            FeedbackType.COMMENT -> "Comment"
            FeedbackType.REGISTRATION -> "Registration"
        }
}

data class AttendanceEntry(
    val dateLabel: String,
    val present: Boolean
)

enum class ProfileTab { ATTENDANCE, CALL_LOGS, FEEDBACK }

// -----------------------------------------------------------------------------------------
// UI STATE
// -----------------------------------------------------------------------------------------

data class ProfileUiState(
    val isLoading: Boolean = false,
    val studentProfile: StudentProfile? = null,
    val attendance: List<AttendanceEntry> = emptyList(),
    val callLogs: List<CallLogEntry> = emptyList(),
    val feedback: List<FeedbackEntry> = emptyList(),
    val selectedTab: ProfileTab = ProfileTab.ATTENDANCE
)

// -----------------------------------------------------------------------------------------
// VIEWMODEL
// -----------------------------------------------------------------------------------------

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // In a real app: fetch students/{studentId} from Firestore here.
        loadDummyData()
    }

    fun onTabSelected(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onCallClick() { /* TODO: launch dialer intent with studentProfile.phone */ }
    fun onMessageClick() { /* TODO: launch SMS/WhatsApp intent with studentProfile.phone */ }
    fun onEditClick() { /* TODO: navigate to edit-studentProfile screen */ }
    fun onBackClick() { /* TODO: navController.popBackStack() */ }

    private fun loadDummyData() {
        val studentProfile = StudentProfile(
            id = "STUD_789XYZ",
            name = "Rahul Verma",
            phone = "+91 99887 76655",
            address = "Gomti Nagar, Lucknow",
            education = "B.Tech CSE",
            occupation = "StudentProfile",
            category = StudentCategory.YOUTH,
            interestLevel = InterestLevel.HIGH,
            status = StudentStatus.ACTIVE,
            registeredBy = RegisteredBy("USER_ABC123", "Amit Sharma"),
            firstRegistrationDateLabel = "12 Jan 2026",
            lastRegistrationDateLabel = "12 Jan 2026",
            eventsJoined = listOf("RATHYATRA_2026"),
            currentBatch = BatchInfo("BATCH_2026_Q3", "Sunday Morning A"),
            facilitator = FacilitatorInfo("USER_ABC123", "Amit Sharma"),
            metrics = StudentMetrics(
                totalAttendanceCount = 24,
                currentYearAttendanceCount = 12,
                lastAttendanceDateLabel = "6 Jul 2026",
                isEligibleForDYS = true,
                movedToDYSBy = null
            )
        )

        val attendance = listOf(
            AttendanceEntry("12 Jul", true),
            AttendanceEntry("5 Jul", true),
            AttendanceEntry("28 Jun", false),
            AttendanceEntry("21 Jun", true),
            AttendanceEntry("14 Jun", true)
        )

        val callLogs = listOf(
            CallLogEntry("Amit Sharma", "8 Jul, 6:40 PM", CallLogStatus.CONNECTED, "Confirmed for Sunday"),
            CallLogEntry("Amit Sharma", "1 Jul, 7:10 PM", CallLogStatus.NO_ANSWER),
            CallLogEntry("Amit Sharma", "24 Jun, 6:55 PM", CallLogStatus.CONNECTED, "Will join next week")
        )

        val feedback = listOf(
            FeedbackEntry(
                FeedbackType.STATUS_CHANGE,
                "28 Jun",
                "Marked on-hold — moving to college hostel for two months."
            ),
            FeedbackEntry(
                FeedbackType.COMMENT,
                "14 Jun",
                "Shows strong interest in the philosophy sessions, asked for extra reading material."
            ),
            FeedbackEntry(
                FeedbackType.REGISTRATION,
                "12 Jan",
                "Registered by Amit Sharma at the campus outreach stall."
            )
        )

        _uiState.update {
            it.copy(
                isLoading = false,
                studentProfile = studentProfile,
                attendance = attendance,
                callLogs = callLogs,
                feedback = feedback
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// ROOT COMPOSABLE (wires ViewModel)
// -----------------------------------------------------------------------------------------

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileScreen(
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onCallClick = viewModel::onCallClick,
        onMessageClick = viewModel::onMessageClick,
        onEditClick = viewModel::onEditClick,
        onBackClick = onBackClick
    )
}

// -----------------------------------------------------------------------------------------
// STATELESS SCREEN
// -----------------------------------------------------------------------------------------

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onTabSelected: (ProfileTab) -> Unit = {},
    onCallClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val student = uiState.studentProfile

        if (student == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (uiState.isLoading) "Loading..." else "StudentProfile not found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Surface
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ProfileTopBar(studentId = student.id, onBackClick = onBackClick) }

            item {
                ProfileHeader(
                    studentProfile = student,
                    onCallClick = onCallClick,
                    onMessageClick = onMessageClick,
                    onEditClick = onEditClick
                )
            }

            item { DetailsCard(studentProfile = student) }

            item { AttendanceSummaryCard(studentProfile = student) }

            item {
                SegmentedTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelected = onTabSelected
                )
            }

            when (uiState.selectedTab) {
                ProfileTab.ATTENDANCE -> {
                    uiState.attendance.chunked(2).forEach { pair ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { entry ->
                                    AttendanceChip(entry = entry, modifier = Modifier.weight(1f))
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                ProfileTab.CALL_LOGS -> {
                    items(uiState.callLogs) { log -> CallLogCard(entry = log) }
                }

                ProfileTab.FEEDBACK -> {
                    items(uiState.feedback) { entry -> FeedbackCard(entry = entry) }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TOP BAR
// -----------------------------------------------------------------------------------------

@Composable
private fun ProfileTopBar(studentId: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = studentId,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    IconButton(onClick = onBackClick, modifier = Modifier.size(32.dp)) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// -----------------------------------------------------------------------------------------
// HEADER: avatar, name, phone, status badges, action buttons
// -----------------------------------------------------------------------------------------

@Composable
private fun ProfileHeader(
    studentProfile: StudentProfile,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        )
                    )
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = studentProfile.initials,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = studentProfile.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = studentProfile.phone,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(
                text = studentProfile.status.name.replace("_", " "),
                color = statusColor(studentProfile.status)
            )
            StatusBadge(
                text = studentProfile.category.name,
                color = categoryColor(studentProfile.category)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(icon = Icons.Default.Call, label = "Call", onClick = onCallClick)
            ActionButton(icon = Icons.Default.Email, label = "Message", onClick = onMessageClick)
            ActionButton(icon = Icons.Default.Edit, label = "Edit", onClick = onEditClick)
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    )
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun statusColor(status: StudentStatus): Color = when (status) {
    StudentStatus.ACTIVE -> MaterialTheme.colorScheme.tertiary
    StudentStatus.ON_HOLD -> MaterialTheme.colorScheme.primary
    StudentStatus.LEFT -> MaterialTheme.colorScheme.error
    StudentStatus.GRADUATED -> MaterialTheme.colorScheme.secondary
}

@Composable
private fun categoryColor(category: StudentCategory): Color = when (category) {
    StudentCategory.YOUTH -> MaterialTheme.colorScheme.primary
    StudentCategory.CONGREGATION -> MaterialTheme.colorScheme.secondary
}

// -----------------------------------------------------------------------------------------
// DETAILS CARD
// -----------------------------------------------------------------------------------------

@Composable
private fun DetailsCard(studentProfile: StudentProfile) {
    SectionCard {
        SectionLabel(text = "DETAILS")
        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DetailItem(
                icon = Icons.Default.DateRange,
                label = "BATCH",
                value = studentProfile.currentBatch.name,
                modifier = Modifier.weight(1f)
            )
            DetailItem(
                icon = Icons.Default.Person,
                label = "FACILITATOR",
                value = studentProfile.facilitator.name,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailItem(
                icon = Icons.Default.Create,
                label = "REGISTERED BY",
                value = studentProfile.registeredBy.name,
                modifier = Modifier.weight(1f)
            )
            DetailItem(
                icon = Icons.Default.DateRange,
                label = "FIRST REGISTERED",
                value = studentProfile.firstRegistrationDateLabel,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DetailItem(
                icon = Icons.Default.Star,
                label = "EDUCATION",
                value = studentProfile.education,
                modifier = Modifier.weight(1f)
            )
            DetailItem(
                icon = Icons.Default.LocationOn,
                label = "ADDRESS",
                value = studentProfile.address,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DetailItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.4.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// ATTENDANCE SUMMARY CARD (ring + stats + DYS pill)
// -----------------------------------------------------------------------------------------

@Composable
private fun AttendanceSummaryCard(studentProfile: StudentProfile) {
    SectionCard {
        SectionLabel(text = "ATTENDANCE")
        Spacer(modifier = Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            AttendanceRing(
                total = studentProfile.metrics.totalAttendanceCount,
                progressCount = studentProfile.metrics.currentYearAttendanceCount
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = "${studentProfile.metrics.currentYearAttendanceCount} sessions this year",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Last attended ${studentProfile.metrics.lastAttendanceDateLabel}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (studentProfile.metrics.isEligibleForDYS) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                RoundedCornerShape(50)
                            )
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Eligible for DYS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRing(total: Int, progressCount: Int) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = MaterialTheme.colorScheme.primary
    val fraction = if (total > 0) (progressCount.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

    Box(modifier = Modifier.size(76.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(76.dp)) {
            val strokeWidth = 8.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$total",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "TOTAL",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// SEGMENTED TABS
// -----------------------------------------------------------------------------------------

@Composable
private fun SegmentedTabs(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabChip(
            text = "Attendance",
            selected = selectedTab == ProfileTab.ATTENDANCE,
            modifier = Modifier.weight(1f)
        ) { onTabSelected(ProfileTab.ATTENDANCE) }

        TabChip(
            text = "Call Logs",
            selected = selectedTab == ProfileTab.CALL_LOGS,
            modifier = Modifier.weight(1f)
        ) { onTabSelected(ProfileTab.CALL_LOGS) }

        TabChip(
            text = "Feedback",
            selected = selectedTab == ProfileTab.FEEDBACK,
            modifier = Modifier.weight(1f)
        ) { onTabSelected(ProfileTab.FEEDBACK) }
    }
}

@Composable
private fun TabChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (selected) {
                    Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary, shape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                } else {
                    Modifier
                }
            )
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB CONTENT: ATTENDANCE CHIPS
// -----------------------------------------------------------------------------------------

@Composable
private fun AttendanceChip(entry: AttendanceEntry, modifier: Modifier = Modifier) {
    val dotColor = if (entry.present) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${entry.dateLabel} — ${if (entry.present) "Present" else "Absent"}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB CONTENT: CALL LOGS
// -----------------------------------------------------------------------------------------

@Composable
private fun CallLogCard(entry: CallLogEntry) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = entry.callerName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.dateTimeLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val (statusText, statusColor) = when (entry.status) {
            CallLogStatus.CONNECTED -> ("Connected" + (entry.note?.let { " · $it" } ?: "")) to MaterialTheme.colorScheme.tertiary
            CallLogStatus.NO_ANSWER -> "No Answer" to MaterialTheme.colorScheme.error
        }

        Text(
            text = statusText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = statusColor,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(statusColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB CONTENT: FEEDBACK / TIMELINE
// -----------------------------------------------------------------------------------------

@Composable
private fun FeedbackCard(entry: FeedbackEntry) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = entry.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = entry.dateLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.description,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

// -----------------------------------------------------------------------------------------
// SHARED LOW-LEVEL BUILDING BLOCKS
// -----------------------------------------------------------------------------------------

@Composable
private fun SectionCard(content: @Composable ColumnScopeMarkerHolder.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            ColumnScopeMarkerHolder.content()
        }
    }
}

// Small trick so SectionCard's lambda can be a plain @Composable () -> Unit without
// pulling in ColumnScope explicitly at call sites; keeps call sites clean.
private object ColumnScopeMarkerHolder

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// -----------------------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------------------

private fun dummyProfileState(tab: ProfileTab = ProfileTab.ATTENDANCE) = ProfileUiState(
    studentProfile = StudentProfile(
        id = "STUD_789XYZ",
        name = "Rahul Verma",
        phone = "+91 99887 76655",
        address = "Gomti Nagar, Lucknow",
        education = "B.Tech CSE",
        occupation = "StudentProfile",
        category = StudentCategory.YOUTH,
        interestLevel = InterestLevel.HIGH,
        status = StudentStatus.ACTIVE,
        registeredBy = RegisteredBy("USER_ABC123", "Amit Sharma"),
        firstRegistrationDateLabel = "12 Jan 2026",
        lastRegistrationDateLabel = "12 Jan 2026",
        currentBatch = BatchInfo("BATCH_2026_Q3", "Sunday Morning A"),
        facilitator = FacilitatorInfo("USER_ABC123", "Amit Sharma"),
        metrics = StudentMetrics(
            totalAttendanceCount = 24,
            currentYearAttendanceCount = 12,
            lastAttendanceDateLabel = "6 Jul 2026",
            isEligibleForDYS = true
        )
    ),
    attendance = listOf(
        AttendanceEntry("12 Jul", true),
        AttendanceEntry("5 Jul", true),
        AttendanceEntry("28 Jun", false),
        AttendanceEntry("21 Jun", true),
        AttendanceEntry("14 Jun", true)
    ),
    callLogs = listOf(
        CallLogEntry("Amit Sharma", "8 Jul, 6:40 PM", CallLogStatus.CONNECTED, "Confirmed for Sunday"),
        CallLogEntry("Amit Sharma", "1 Jul, 7:10 PM", CallLogStatus.NO_ANSWER),
        CallLogEntry("Amit Sharma", "24 Jun, 6:55 PM", CallLogStatus.CONNECTED, "Will join next week")
    ),
    feedback = listOf(
        FeedbackEntry(FeedbackType.STATUS_CHANGE, "28 Jun", "Marked on-hold — moving to college hostel for two months."),
        FeedbackEntry(FeedbackType.COMMENT, "14 Jun", "Shows strong interest in the philosophy sessions, asked for extra reading material."),
        FeedbackEntry(FeedbackType.REGISTRATION, "12 Jan", "Registered by Amit Sharma at the campus outreach stall.")
    ),
    selectedTab = tab
)

@Preview(showBackground = true, name = "Attendance Tab - Light")
@Composable
private fun ProfileScreenAttendanceLightPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        var state by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(dummyProfileState()) }
        ProfileScreen(uiState = state, onTabSelected = { state = state.copy(selectedTab = it) })
    }
}

@Preview(showBackground = true, name = "Call Logs Tab - Light")
@Composable
private fun ProfileScreenCallLogsLightPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        var state by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(dummyProfileState(ProfileTab.CALL_LOGS)) }
        ProfileScreen(uiState = state, onTabSelected = { state = state.copy(selectedTab = it) })
    }
}

@Preview(showBackground = true, name = "Feedback Tab - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenFeedbackDarkPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        var state by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(dummyProfileState(ProfileTab.FEEDBACK)) }
        ProfileScreen(uiState = state, onTabSelected = { state = state.copy(selectedTab = it) })
    }
}

@Preview(showBackground = true, name = "Attendance Tab - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProfileScreenAttendanceDarkPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        var state by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(dummyProfileState()) }
        ProfileScreen(uiState = state, onTabSelected = { state = state.copy(selectedTab = it) })
    }
}