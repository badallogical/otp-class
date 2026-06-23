package com.harekrishna.otpClasses.ui.events

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import com.harekrishna.otpClasses.ui.theme.CardBg
import com.harekrishna.otpClasses.ui.theme.DividerColor
import com.harekrishna.otpClasses.ui.theme.GoldAccent
import com.harekrishna.otpClasses.ui.theme.GreenLight
import com.harekrishna.otpClasses.ui.theme.GreenSuccess
import com.harekrishna.otpClasses.ui.theme.Saffron
import com.harekrishna.otpClasses.ui.theme.SaffronDark
import com.harekrishna.otpClasses.ui.theme.SaffronLight
import com.harekrishna.otpClasses.ui.theme.SurfaceWhite
import com.harekrishna.otpClasses.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


data class RegisteredStudent(
    val id: String,
    val name: String,
    val phone: String,
    val registeredOn: String = ""
)

/**
 * A devotee who physically came to the Harinaam event.
 * Attendance is captured on the day of the event.
 * Devotees are NOT the same as registered students —
 * they are people who joined the Harinaam kirtan.
 */
data class HarinaanDevotee(
    val id: String,
    val name: String,
    val phone: String,
    val markedAt: String = ""   // time they were marked e.g. "07:12 AM"
)

data class HarinaanEvent(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    // Students who have registered to come to the class
    val registrations: MutableList<RegisteredStudent> = mutableListOf(),
    // Devotees who physically attended the Harinaam on the day
    val attendance: MutableList<HarinaanDevotee> = mutableListOf()
) {
    val isUpcoming: Boolean get() = attendance.isEmpty()
}

// ─────────────────────────────────────────────────────────────────
// VIEWMODEL
// ─────────────────────────────────────────────────────────────────

class HarinaanViewModel : ViewModel() {

    private val _events = mutableStateOf(buildDummyData())
    val events: State<List<HarinaanEvent>> = _events

    // Global devotee directory — built from past attendance across all events
    // Used for search when marking attendance
    val devoteeDirectory: List<HarinaanDevotee>
        get() = _events.value
            .flatMap { it.attendance }
            .distinctBy { it.phone }

    // ── Events ─────────────────────────────────────
    fun createEvent(title: String, date: String, time: String, location: String) {
        val evt = HarinaanEvent(
            id = "evt_${System.currentTimeMillis()}",
            title = title, date = date, time = time, location = location
        )
        _events.value = listOf(evt) + _events.value
    }

    fun getEvent(id: String) = _events.value.firstOrNull { it.id == id }

    // ── Student Registration ────────────────────────
    fun registerStudent(eventId: String, name: String, phone: String) {
        _events.value = _events.value.map { evt ->
            if (evt.id != eventId) return@map evt
            val student = RegisteredStudent(
                id = "stu_${System.currentTimeMillis()}",
                name = name, phone = phone, registeredOn = "Today"
            )
            evt.copy(registrations = (evt.registrations + student).toMutableList())
        }
    }

    // ── Harinaam Attendance ─────────────────────────
    /**
     * Add a devotee to the attendance list for an event.
     * This is a SEPARATE action from student registration.
     */
    fun addAttendance(eventId: String, name: String, phone: String) {
        _events.value = _events.value.map { evt ->
            if (evt.id != eventId) return@map evt
            // avoid duplicates by phone
            if (evt.attendance.any { it.phone == phone }) return@map evt
            val devotee = HarinaanDevotee(
                id = "dev_${System.currentTimeMillis()}",
                name = name, phone = phone, markedAt = "Now"
            )
            evt.copy(attendance = (evt.attendance + devotee).toMutableList())
        }
    }

    fun removeAttendance(eventId: String, devoteeId: String) {
        _events.value = _events.value.map { evt ->
            if (evt.id != eventId) return@map evt
            evt.copy(attendance = evt.attendance.filterNot { it.id == devoteeId }.toMutableList())
        }
    }

    companion object {
        fun buildDummyData(): List<HarinaanEvent> {
            // Global devotee pool (people who have attended past harinaam)
            val globalDevotees = listOf(
                HarinaanDevotee("gd1", "Radha Devi Dasi",    "+91 98765 43210", "06:10 AM"),
                HarinaanDevotee("gd2", "Govinda Das",         "+91 87654 32109", "06:22 AM"),
                HarinaanDevotee("gd3", "Tulasi Devi Dasi",   "+91 91234 56789", "06:35 AM"),
                HarinaanDevotee("gd4", "Madhav Prabhu",      "+91 80123 45678", "06:45 AM"),
                HarinaanDevotee("gd5", "Krishnananda Das",   "+91 43210 98765", "07:00 AM"),
                HarinaanDevotee("gd6", "Gauranga Das",       "+91 85210 36741", "07:12 AM"),
                HarinaanDevotee("gd7", "Bhakti Devi Dasi",  "+91 96321 47852", "06:50 AM"),
                HarinaanDevotee("gd8", "Vrinda Kumari",      "+91 74109 25630", "07:05 AM"),
            )

            val e1 = HarinaanEvent(
                id = "1", title = "SGPGI Harinaam Kirtan",
                date = "23 Jun 2025", time = "06:30 AM – 09:00 AM",
                location = "SGPGI Campus, Lucknow",
                registrations = mutableListOf(
                    RegisteredStudent("s1", "Priya Sharma",   "+91 76543 21098", "20 Jun"),
                    RegisteredStudent("s2", "Anand Mishra",   "+91 65432 10987", "21 Jun"),
                    RegisteredStudent("s3", "Sunita Gupta",   "+91 54321 09876", "22 Jun"),
                ),
                attendance = mutableListOf()   // not yet taken — upcoming
            )

            val e2 = HarinaanEvent(
                id = "2", title = "Vrindavan Harinaam Sankirtan",
                date = "15 Jun 2025", time = "05:00 AM – 08:00 AM",
                location = "ISKCON Temple, Vrindavan",
                registrations = mutableListOf(
                    RegisteredStudent("s4", "Ravi Shankar Das", "+91 77665 54433", "12 Jun"),
                    RegisteredStudent("s5", "Meera Pandey",     "+91 66554 43322", "13 Jun"),
                    RegisteredStudent("s6", "Chandan Kumar",    "+91 70012 34567", "14 Jun"),
                    RegisteredStudent("s7", "Yamuna Devi",      "+91 60001 23456", "14 Jun"),
                    RegisteredStudent("s8", "Suresh Patel",     "+91 99887 76655", "15 Jun"),
                ),
                attendance = mutableListOf(
                    globalDevotees[0], globalDevotees[1], globalDevotees[2],
                    globalDevotees[3], globalDevotees[4], globalDevotees[5],
                    HarinaanDevotee("gd9", "Hari Prasad", "+91 63098 14529", "05:40 AM"),
                )
            )

            val e3 = HarinaanEvent(
                id = "3", title = "Janmashtami Maha Sankirtan",
                date = "16 Aug 2025", time = "08:00 PM – 12:00 AM",
                location = "Dwarka Puri, Gujarat",
                registrations = mutableListOf(
                    RegisteredStudent("s9",  "Geeta Mehta",  "+91 88776 65544", "10 Aug"),
                    RegisteredStudent("s10", "Nikhil Gupta", "+91 77123 45678", "12 Aug"),
                ),
                attendance = mutableListOf()
            )

            val e4 = HarinaanEvent(
                id = "4", title = "Gaura Purnima Sankirtan",
                date = "22 Mar 2025", time = "04:00 AM – 07:00 AM",
                location = "Mayapur Dham, West Bengal",
                registrations = mutableListOf(
                    RegisteredStudent("s11", "Bhakta Ramesh",  "+91 96321 00001", "18 Mar"),
                    RegisteredStudent("s12", "Savitri Devi",   "+91 85210 00002", "19 Mar"),
                    RegisteredStudent("s13", "Tarun Prabhu",   "+91 74109 00003", "20 Mar"),
                ),
                attendance = mutableListOf(
                    globalDevotees[6], globalDevotees[7],
                    globalDevotees[0].copy(id = "gd1b"),
                    globalDevotees[1].copy(id = "gd2b"),
                    globalDevotees[2].copy(id = "gd3b"),
                    HarinaanDevotee("gd10", "Srila Prabhu", "+91 52987 03418", "04:20 AM"),
                    HarinaanDevotee("gd11", "Nandini Devi", "+91 41876 92307", "04:30 AM"),
                    HarinaanDevotee("gd12", "Akash Das",    "+91 30765 81296", "04:45 AM"),
                )
            )

            return listOf(e1, e2, e3, e4)
        }
    }
}

sealed class Screen {
    object EventList : Screen()
    data class EventDetail(val eventId: String) : Screen()
    data class TakeAttendance(val eventId: String) : Screen()
}

@Composable
fun HarinaanApp(viewModel: HarinaanViewModel = remember { HarinaanViewModel() }) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.EventList) }

        when (val scr = currentScreen) {
            is Screen.EventList     -> EventListScreen(viewModel) {
                currentScreen = Screen.EventDetail(it.id)
            }
            is Screen.EventDetail   -> {
                val evt = viewModel.getEvent(scr.eventId)
                if (evt != null) EventDetailScreen(
                    event = evt, viewModel = viewModel,
                    onBack = { currentScreen = Screen.EventList },
                    onTakeAttendance = { currentScreen = Screen.TakeAttendance(evt.id) }
                )
            }
            is Screen.TakeAttendance -> {
                val evt = viewModel.getEvent(scr.eventId)
                if (evt != null) TakeAttendanceScreen(
                    event = evt, viewModel = viewModel,
                    onBack = { currentScreen = Screen.EventDetail(scr.eventId) }
                )
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen(
    viewModel: HarinaanViewModel,
    onEventClick: (HarinaanEvent) -> Unit
) {
    val events by viewModel.events
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Harinaam Sankirtan", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Plan Event", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OverviewBanner(events)
                Spacer(Modifier.height(6.dp))
                Text("All Events", fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
            }
            items(events, key = { it.id }) { event ->
                EventCard(event, onClick = { onEventClick(event) })
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onCreate  = { t, d, ti, l ->
                viewModel.createEvent(t, d, ti, l)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun OverviewBanner(events: List<HarinaanEvent>) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))).padding(18.dp)
    ) {
        Column {
            Text("Overview", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                BannerStat("${events.sumOf { it.registrations.size }}", "Students")
                BannerStat("${events.sumOf { it.attendance.size }}", "Devotees")
            }
        }
    }
}

@Composable
fun BannerStat(value: String, label: String) {
    Column {
        Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
    }
}

@Composable
fun EventCard(event: HarinaanEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors( MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top ) {
            // Icon box
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(event.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(6.dp))
                    StatusBadge(event.isUpcoming)
                }
                Spacer(Modifier.height(3.dp))
                InfoRow(Icons.Default.CalendarToday, "${event.date}  •  ${event.time}")
                Spacer(Modifier.height(1.dp))
                InfoRow(Icons.Default.LocationOn, event.location)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun StatusBadge(isUpcoming: Boolean) {
    val (text, fg, bg) = if (isUpcoming)
        Triple("Upcoming", GreenSuccess, GreenLight)
    else
        Triple("Done", MaterialTheme.colorScheme.onSecondary, MaterialTheme.colorScheme.secondary)

    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(text, fontSize = 10.sp, color = fg, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(11.dp))
        Text(text, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun CountChip(icon: ImageVector, count: Int, label: String, color: Color, bg: Color) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
        Text("$count", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.75f))
    }
}

// ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit // title, date, time (duration), location
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Date & Time States
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    // Dialog Visibility States
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val valid = title.isNotBlank() && date.isNotBlank() && location.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        date = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // --- Start Time Picker Dialog ---
    if (showStartTimePicker) {
        val startTimePickerState = rememberTimePickerState()
        TimePickerDialogWrapper(
            onDismissRequest = { showStartTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, startTimePickerState.hour)
                    set(Calendar.MINUTE, startTimePickerState.minute)
                }
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                startTime = sdf.format(cal.time)
                showStartTimePicker = false
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }

    // --- End Time Picker Dialog ---
    if (showEndTimePicker) {
        val endTimePickerState = rememberTimePickerState()
        TimePickerDialogWrapper(
            onDismissRequest = { showEndTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, endTimePickerState.hour)
                    set(Calendar.MINUTE, endTimePickerState.minute)
                }
                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                endTime = sdf.format(cal.time)
                showEndTimePicker = false
            }
        ) {
            TimePicker(state = endTimePickerState)
        }
    }

    // --- Main Dialog UI ---
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), // Replace with your SaffronLight
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.EventAvailable, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Plan New Event", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Schedule a Harinaam Kirtan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Spacer(Modifier.height(14.dp))

                // Inputs
                FormField("Event Name", title, { title = it }, "e.g. SGPGI Harinaam Kirtan", Icons.Default.MusicNote)
                Spacer(Modifier.height(10.dp))

                // Date Field (Clickable)
                ClickableTextField("Date", date, "dd-mm-yyyy", Icons.Default.CalendarToday) {
                    showDatePicker = true
                }
                Spacer(Modifier.height(10.dp))

                // Time Fields (Row with Start and End)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        ClickableTextField("From", startTime, "Start Time", Icons.Default.Schedule) {
                            showStartTimePicker = true
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ClickableTextField("To", endTime, "End Time", Icons.Default.Schedule) {
                            showEndTimePicker = true
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                FormField("Location", location, { location = it }, "e.g. SGPGI Campus, Lucknow", Icons.Default.LocationOn)
                Spacer(Modifier.height(22.dp))

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val combinedTime = "$startTime - $endTime"
                            onCreate(title.trim(), date.trim(), combinedTime, location.trim())
                        },
                        enabled = valid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // Replace with your Saffron
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}



@Composable
fun ClickableTextField(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    // Observe the pressed state of the TextField
    val isPressed by interactionSource.collectIsPressedAsState()

    // Trigger the onClick action whenever the field is pressed
    LaunchedEffect(isPressed) {
        if (isPressed) {
            onClick()
        }
    }

    OutlinedTextField(
        value = value,
        onValueChange = {}, // Leave empty since we don't want manual typing
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        readOnly = true, // CRITICAL: Prevents the software keyboard from opening
        singleLine = true,
        interactionSource = interactionSource, // Pass the interaction source here
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(11.dp),
        enabled = true, // Set to true so it can receive focus and touch ripples
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = DividerColor,
            focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface

        )
    )
}

@Composable
fun TimePickerDialogWrapper(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Cancel") } },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    hint: String,
    icon: ImageVector,
    colorAccent : Color = MaterialTheme.colorScheme.primary
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        label = { Text(label, color = colorAccent) },
        placeholder = {
            Text(
                text = hint,
                fontSize = 13.sp,
                // Using onSurfaceVariant is standard for hints/placeholders
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colorAccent,
                modifier = Modifier.size(18.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(11.dp),
        colors = OutlinedTextFieldDefaults.colors(
            // Handle ALL text states here cleanly
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

            // Border colors
            focusedBorderColor = colorAccent,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,

            // Background container colors
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        // REMOVED color from here so the colors block above can do its job
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
    )
}

// ─────────────────────────────────────────────────────────────────
// SCREEN 2 — EVENT DETAIL
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    event: HarinaanEvent,
    viewModel: HarinaanViewModel,
    onBack: () -> Unit,
    onTakeAttendance: () -> Unit
) {
    val live by remember { derivedStateOf { viewModel.getEvent(event.id) ?: event } }
    var tab by remember { mutableStateOf(0) }
    var showRegisterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(live.title, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${live.date}  •  ${live.time}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    // Location pill
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(9.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Text(live.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f))
                        StatusBadge(live.isUpcoming)
                    }

                    // ── Two separate stat cards ──────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Students registered (orange)
                        DetailStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.School,
                            value = "${live.registrations.size}",
                            label = "Students\nRegistered",
                            accentColor = MaterialTheme.colorScheme.primary,
                            bgColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        // Devotees who attended Harinaam (green)
                        DetailStatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Groups,
                            value = "${live.attendance.size}",
                            label = "Devotees\nin Harinaam",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            bgColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.5f)
                        )
                    }

                    // ── Action buttons ────────────────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Register a student
                        Button(
                            onClick = { showRegisterSheet = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Register", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        // Take Harinaam attendance
                        Button(
                            onClick = onTakeAttendance,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.HowToReg, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Attendance", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider(color = DividerColor)

                // ── Tabs ─────────────────────────────────────────────
                val tabs = listOf(
                    "Students (${live.registrations.size})",
                    "Harinaam (${live.attendance.size})"
                )
                TabRow(
                    selectedTabIndex = tab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[tab]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { i, label ->
                        Tab(
                            selected = tab == i,
                            onClick = { tab = i },
                            text = {
                                Text(
                                    label,
                                    fontWeight = if (tab == i) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (tab == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }

            // ── Tab 0 — Registered Students ──────────────────────────
            if (tab == 0) {
                if (live.registrations.isEmpty()) {
                    item { EmptyState(Icons.Default.School, "No students registered yet") }
                } else {
                    itemsIndexed(live.registrations) { i, student ->
                        StudentRow(student, i + 1)
                        if (i < live.registrations.size - 1)
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor, thickness = 0.8.dp)
                    }
                }
            }

            // ── Tab 1 — Harinaam Devotees (Attendance) ────────────────
            if (tab == 1) {
                if (live.attendance.isEmpty()) {
                    item {
                        EmptyState(Icons.Default.Groups,
                            "No attendance taken yet\nTap Attendance to mark devotees")
                    }
                } else {
                    itemsIndexed(live.attendance) { i, devotee ->
                        DevoteeAttendanceRow(devotee, i + 1,
                            onRemove = { viewModel.removeAttendance(live.id, devotee.id) })
                        if (i < live.attendance.size - 1)
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor, thickness = 0.8.dp)
                    }
                }
            }
        }
    }

    if (showRegisterSheet) {
        RegisterStudentSheet(
            eventId = live.id, viewModel = viewModel,
            onDismiss = { showRegisterSheet = false }
        )
    }
}

@Composable
fun DetailStatCard(modifier: Modifier, icon: ImageVector, value: String, label: String, accentColor: Color, bgColor: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = DividerColor, modifier = Modifier.size(52.dp))
            Spacer(Modifier.height(12.dp))
            Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
        }
    }
}

@Composable
fun StudentRow(student: RegisteredStudent, index: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center) {
            Text(student.name.first().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(student.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(student.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("#$index", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DevoteeAttendanceRow(devotee: HarinaanDevotee, index: Int, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(GreenLight.copy(alpha = 0.4f))
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(GreenLight),
            contentAlignment = Alignment.Center) {
            Text(devotee.name.first().toString(), fontWeight = FontWeight.Bold, color = GreenSuccess, fontSize = 16.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(devotee.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary)
            Text(devotee.phone, fontSize = 12.sp, color = TextSecondary)
        }
        if (devotee.markedAt.isNotEmpty()) {
            Surface(shape = RoundedCornerShape(6.dp), color = GreenLight) {
                Text(devotee.markedAt, fontSize = 10.sp, color = GreenSuccess, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
            }
        }
        Icon(Icons.Default.CheckCircle, null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
// REGISTER STUDENT BOTTOM SHEET
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStudentSheet(eventId: String, viewModel: HarinaanViewModel, onDismiss: () -> Unit) {
    var name  by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 36.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Register Student", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Add student to this event", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(18.dp))
            FormField("Student Name", name, { name = it }, "e.g. Priya Sharma", Icons.Default.Person)
            Spacer(Modifier.height(12.dp))
            FormField("Mobile Number", phone, { phone = it }, "e.g. +91 98765 43210", Icons.Default.Phone)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        viewModel.registerStudent(eventId, name.trim(), phone.trim())
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Confirm Registration", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// SCREEN 3 — TAKE HARINAAM ATTENDANCE
// This is SEPARATE from student registration.
// Here we mark which DEVOTEES physically joined the Harinaam kirtan.
// Search existing devotees from past events, or add a new one.
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeAttendanceScreen(
    event: HarinaanEvent,
    viewModel: HarinaanViewModel,
    onBack: () -> Unit
) {
    val live by remember { derivedStateOf { viewModel.getEvent(event.id) ?: event } }
    var searchQuery by remember { mutableStateOf("") }
    var showAddNewSheet by remember { mutableStateOf(false) }
    var snackbarMsg by remember { mutableStateOf<String?>(null) }

    // Devotees from all past events = searchable directory
    val directory = viewModel.devoteeDirectory
    val alreadyMarkedIds = live.attendance.map { it.phone }.toSet()

    // Filter directory: match query AND not already in attendance
    val searchResults = remember(searchQuery, directory, alreadyMarkedIds) {
        if (searchQuery.length < 2) emptyList()
        else directory.filter {
            (it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery)) &&
                    it.phone !in alreadyMarkedIds
        }
    }

    LaunchedEffect(snackbarMsg) {
        if (snackbarMsg != null) {
            kotlinx.coroutines.delay(2000)
            snackbarMsg = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Harinaam Attendance", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onPrimary)
                        Text(live.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.tertiary)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // ── Header: devotee count ──────────────────────────────
                item {
                    Column(modifier = Modifier.background(MaterialTheme.colorScheme.tertiary).padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Devotees in Harinaam", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), fontSize = 12.sp)
                                Text("${live.attendance.size}", color = MaterialTheme.colorScheme.onPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.2f)) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                                    Text("${live.date}", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // ── Search box ────────────────────────────────────────
                item {
                    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp)) {

                        Text("Mark Attendance", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 10.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search devotee by name or phone…", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.tertiary, unfocusedBorderColor = DividerColor,
                                focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        )

                        // Search results
                        if (searchQuery.length >= 2) {
                            Spacer(Modifier.height(8.dp))
                            if (searchResults.isEmpty()) {
                                // No match — offer to add new
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, DividerColor, RoundedCornerShape(11.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.PersonSearch, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("No match found for ${searchQuery}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                        Text("Add as new devotee", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Button(
                                        onClick = { showAddNewSheet = true },
                                        shape = RoundedCornerShape(9.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            } else {
                                // Show search matches
                                Column(
                                    modifier = Modifier.clip(RoundedCornerShape(11.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(11.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    searchResults.forEachIndexed { i, devotee ->
                                        SearchResultRow(devotee,
                                            onMark = {
                                                viewModel.addAttendance(live.id, devotee.name, devotee.phone)
                                                searchQuery = ""
                                                snackbarMsg = "${devotee.name} marked present"
                                            }
                                        )
                                        if (i < searchResults.size - 1)
                                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant, thickness = 0.8.dp)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // "Add new devotee" secondary button always visible
                        OutlinedButton(
                            onClick = { showAddNewSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(11.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add New Devotee", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // ── Marked attendance list ────────────────────────────
                item {
                    Spacer(Modifier.height(8.dp))
                    if (live.attendance.isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(15.dp))
                            Text("Marked Present (${live.attendance.size})", fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                if (live.attendance.isEmpty()) {
                    item {
                        EmptyState(Icons.Default.Groups, "No devotees marked yet.\nSearch above or add a new one.")
                    }
                } else {
                    itemsIndexed(live.attendance, key = { _, d -> d.id }) { i, devotee ->
                        MarkedDevoteeRow(
                            devotee = devotee,
                            index   = i + 1,
                            onUnmark = { viewModel.removeAttendance(live.id, devotee.id) }
                        )
                        if (i < live.attendance.size - 1)
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, thickness = 0.8.dp)
                    }
                }
            }

            // Snackbar
            snackbarMsg?.let { msg ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier.padding(bottom = 16.dp).padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceTint)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
                            Text(msg, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    if (showAddNewSheet) {
        AddDevoteeSheet(
            prefillName = if (searchResults.isEmpty() && searchQuery.length >= 2) searchQuery else "",
            onDismiss = { showAddNewSheet = false },
            onAdd = { name, phone ->
                viewModel.addAttendance(live.id, name, phone)
                showAddNewSheet = false
                searchQuery = ""
                snackbarMsg = "$name marked present"
            }
        )
    }
}

@Composable
fun SearchResultRow(devotee: HarinaanDevotee, onMark: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceTint),
            contentAlignment = Alignment.Center) {
            Text(devotee.name.first().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 15.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(devotee.name, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(devotee.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = onMark,
            shape = RoundedCornerShape(9.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Mark", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MarkedDevoteeRow(devotee: HarinaanDevotee, index: Int, onUnmark: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.35f))
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
            contentAlignment = Alignment.Center) {
            Text(devotee.name.first().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary, fontSize = 15.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(devotee.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(devotee.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("#$index", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 4.dp))
        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
        IconButton(onClick = onUnmark, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        }
    }
}

// Add new devotee bottom sheet
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDevoteeSheet(
    prefillName: String = "",
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String) -> Unit
) {
    var name  by remember { mutableStateOf(prefillName) }
    var phone by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 36.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceTint),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Add Devotee to Harinaam", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Mark as present in this kirtan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(18.dp))
            FormField("Devotee Name", name, { name = it }, "e.g. Radha Devi Dasi", Icons.Default.Person, MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.height(12.dp))
            FormField("Mobile Number", phone, { phone = it }, "e.g. +91 98765 43210", Icons.Default.Phone, MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { if (name.isNotBlank() && phone.isNotBlank()) onAdd(name.trim(), phone.trim()) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Mark Present", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// PREVIEWS
// ─────────────────────────────────────────────────────────────────

//@Preview(showBackground = true, showSystemUi = true, name = "01 – Full App")
//@Composable
//fun PreviewApp() = HarinaanApp()
//
//@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "02 – Dark Mode")
//@Composable
//fun PreviewList() {
//    val vm = remember { HarinaanViewModel() }
//    MaterialTheme { EventListScreen(vm) {} }
//}

//@Preview(showBackground = true, showSystemUi = true, name = "03 – Event Detail (Upcoming)")
//@Composable
//fun PreviewDetailUpcoming() {
//    val vm = remember { HarinaanViewModel() }
//    MaterialTheme {
//        EventDetailScreen(vm.events.value.first { it.isUpcoming }, vm, {}, {})
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, name = "04 – Event Detail (Done)")
//@Composable
//fun PreviewDetailDone() {
//    val vm = remember { HarinaanViewModel() }
//    MaterialTheme {
//        EventDetailScreen(vm.events.value.first { !it.isUpcoming }, vm, {}, {})
//    }
//}

@Preview(showBackground = true, showSystemUi = true, name = "05 – Take Attendance")
@Composable
fun PreviewAttendance() {
    val vm = remember { HarinaanViewModel() }
    MaterialTheme { TakeAttendanceScreen(vm.events.value.first(), vm) {} }
}

//@Preview(showBackground = true, name = "06 – Create Event Dialog")
//@Composable
//fun PreviewCreateDialog() {
//    MaterialTheme { CreateEventDialog({}) { _, _, _, _ -> } }
//}
