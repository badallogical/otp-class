package com.harekrishna.otpClasses.ui.attendance

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.TurnedIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.harekrishna.otpClasses.data.models.StudentAttendee
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTopAppBar(
    date: String,
    totalAttendees: Int,
    presentCount: Int,
    leftCount: Int,
    totalNew: Int,
    attendeesList: List<StudentAttendee>, // Replace with your actual StudentAttendee data class
    context: Context,
    onSyncClick: () -> Unit
) {
    var showShareMenu by remember { mutableStateOf(false) }
    val shareButtonPosition = remember { mutableStateOf(Offset.Zero) }

            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Attendance Details",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Report for $date",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // Sync Button
                    IconButton(onClick = onSyncClick) {
                        Icon(
                            Icons.Outlined.Sync,
                            contentDescription = "Sync",
                            tint = Color.White
                        )
                    }

                    // Share Button with Popup Menu
                    Box {
                        IconButton(
                            onClick = { showShareMenu = true },
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    shareButtonPosition.value = coordinates.positionInParent()
                                }
                        ) {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            // Raw Data Option
                            DropdownMenuItem(
                                text = { Text("Raw Data") },
                                onClick = {
                                    showShareMenu = false
                                    val message = buildString {
                                        append("Attendance Report for $date\n")
                                        append("Total: $totalAttendees | Present: $presentCount | Left: $leftCount\n")
                                        append("New: $totalNew\n\n")
                                        attendeesList.forEachIndexed { index, attendee ->
                                            append("${index + 1}. ${attendee.name} - ${attendee.phone}")
                                            if (attendee.hasLeft) {
                                                append(" (Left at ${attendee.leftTime})")
                                            }
                                            append("\n")
                                        }
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Share Attendance Report")
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.TextFields,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )

                            // Export as Excel Option
                            DropdownMenuItem(
                                text = { Text("Export as Excel") },
                                onClick = {
                                    showShareMenu = false
                                    val csvData = buildString {
                                        append("Name,Phone,Facilitator,RepeatedTimes,IsNew,RegDate,HasLeft,LeftTime\n")
                                        attendeesList.forEach {
                                            append("\"${it.name}\",\"${it.phone}\",\"${it.facilitator ?: "Unassigned"}\",${it.repeatedTimes},${it.isNew},\"${it.regDate}\",${it.hasLeft},\"${it.leftTime ?: ""}\"\n")
                                        }
                                    }

                                    val fileName = "Attendance_Report_$date.csv"
                                    val file = File(context.cacheDir, fileName)
                                    file.writeText(csvData)

                                    val uri: Uri = FileProvider.getUriForFile(
                                        context,
                                        context.packageName + ".provider",
                                        file
                                    )

                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Export Attendance")
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.TableChart,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                }
            )
    }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDetailsScreen(
    date: String,
    viewModel: AttendanceViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Convert to mutable state list to handle operations
    val uiState by viewModel.attendanceDetailsUiState.collectAsState()

    var attendeesList = uiState.filteredAttendees

    LaunchedEffect(Unit){
        viewModel.loadAttendanceDetailData(date)
    }

    // Calculate statistics
    val totalAttendees = attendeesList.size
    val totalNew = attendeesList.count { it.isNew }
    val totalRepeated = attendeesList.count { it.repeatedTimes > 1 }
    val assignedCount = attendeesList.count { it.facilitator != null }
    val leftCount = attendeesList.count { it.hasLeft }
    val presentCount = attendeesList.count { !it.hasLeft }

    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLeftDialog by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }
    var selectedAttendee by remember { mutableStateOf<StudentAttendee?>(null) }

    var isSearchMode by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    var selectedFilter by remember { mutableStateOf("All") }
    val scrollState = rememberScrollState()

    if( uiState.isLoading ){
        CircularProgressIndicator()
    }


    val filteredAttendees = if (isSearchMode) {
        attendeesList.filter { attendee ->
            attendee.name.contains(searchText, ignoreCase = true) ||
                    attendee.phone.contains(searchText, ignoreCase = true) ||
                    (attendee.facilitator?.contains(searchText, ignoreCase = true) ?: false)
        }
    } else {
        when (selectedFilter) {
            "New" -> attendeesList.filter { it.isNew }
            "Repeated" -> attendeesList.filter { it.repeatedTimes > 1 }
            "Unassigned" -> attendeesList.filter { it.facilitator == null }
            "Assigned" -> attendeesList.filter { it.facilitator != null }
            "Left" -> attendeesList.filter { it.hasLeft }
            "Present" -> attendeesList.filter { !it.hasLeft }
            else -> attendeesList
        }
    }

    // Delete dialog
    if (showDeleteDialog && selectedAttendee != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Attendance") },
            text = { Text("Are you sure you want to delete ${selectedAttendee?.name}'s attendance record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedAttendee?.id?.let { id ->
                            //onDeleteAttendee(id)
                            attendeesList = attendeesList.filter { it.id != id }
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Mark as left dialog
    if (showLeftDialog && selectedAttendee != null) {
        AlertDialog(
            onDismissRequest = { showLeftDialog = false },
            title = { Text("Mark as Left") },
            text = { Text("Mark ${selectedAttendee?.name} as having left the class?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedAttendee?.id?.let { id ->
                            val currentTime = LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("hh:mm a")
                            )
                            //onMarkAsLeft(id)
                            attendeesList = attendeesList.map { attendee ->
                                if (attendee.id == id) {
                                    attendee.copy(hasLeft = true, leftTime = currentTime)
                                } else {
                                    attendee
                                }
                            }
                        }
                        showLeftDialog = false
                    }
                ) {
                    Text("Mark Left")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeftDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Mark as left dialog
    if (showReturnDialog && selectedAttendee != null) {
        AlertDialog(
            onDismissRequest = { showReturnDialog = false },
            title = { Text("Mark as Present") },
            text = { Text("Mark ${selectedAttendee?.name} as returned back in the class?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedAttendee?.id?.let { id ->
                            val currentTime = LocalDateTime.now().format(
                                DateTimeFormatter.ofPattern("HH:mm:ss")
                            )
                            attendeesList = attendeesList.map { attendee ->
                                if (attendee.id == id) {
                                    attendee.copy(hasLeft = false, leftTime = "")
                                } else {
                                    attendee
                                }
                            }
                        }
                        showReturnDialog = false
                    }
                ) {
                    Text("Mark Again")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReturnDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AttendanceTopAppBar(
                date = date,
                totalAttendees = totalAttendees,
                presentCount = presentCount,
                leftCount = leftCount,
                totalNew = totalNew,
                attendeesList = attendeesList,
                context = context,
                onSyncClick = {
                    // Handle sync action (e.g., trigger data refresh)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Stats Card
            SmartStatisticsCard(totalAttendees, presentCount, leftCount, totalNew, totalRepeated, assignedCount)

            Spacer(modifier = Modifier.height(8.dp))

            // Attendance Filter
            AttendanceFilterRow(
                searchText = searchText,
                onSearchTextChange = { searchText = it },
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                isSearchMode = isSearchMode,
                onSearchModeToggle = { isSearchMode = it }
            )

            // Filter info
            Text(
                "${filteredAttendees.size} ${if (selectedFilter == "All") "attendees" else "$selectedFilter attendees"}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            // List of attendees
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredAttendees) { attendee ->
                        EnhancedAttendeeCard(
                            attendee = attendee,
                            onDeleteClick = {
                                selectedAttendee = attendee
                                showDeleteDialog = true
                            },
                            onMarkLeftClick = {
                                if (!attendee.hasLeft) {
                                    selectedAttendee = attendee
                                    showLeftDialog = true
                                }
                            },
                            onJoinedBackClick = {
                                if(attendee.hasLeft){
                                    selectedAttendee = attendee
                                    showReturnDialog = true
                                }
                            }
                        )
                    }

                    // Add some padding at the bottom
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AttendanceFilterRow(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    isSearchMode: Boolean,
    onSearchModeToggle: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Crossfade(targetState = isSearchMode) { searchMode ->
                if (searchMode) {
                    IconButton(onClick = {
                        onSearchModeToggle(false)
                        onSearchTextChange("") // Clear search text when exiting search mode
                    }) {
                        Icon(
                            Icons.Outlined.FilterList,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    IconButton(onClick = {
                        onSearchModeToggle(true)
                    }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = isSearchMode,
            transitionSpec = {
                if (targetState) {
                    // Search mode: slide in from right + fade in
                    (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> -width } + fadeOut())
                } else {
                    // Filter mode: slide in from left + fade in
                    (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                        slideOutHorizontally { width -> width } + fadeOut())
                }.using(SizeTransform(clip = false))
            }
        ) { searchMode ->
            if (searchMode) {
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp),
                    placeholder = { Text("search phone or name") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp), // Rounded borders
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { onSearchTextChange("") }) {
                                Icon(
                                    Icons.Outlined.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "All",
                        "Present",
                        "Left",
                        "New",
                        "Repeated",
                        "Unassigned",
                        "Assigned"
                    )
                    filters.forEach { label ->
                        FilterChip(
                            selected = selectedFilter == label,
                            onClick = { onFilterSelected(label) },
                            label = { Text(label) },
                            modifier = Modifier.animateContentSize() // Smooth chip size animation
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun SmartStatisticsCard(
    total: Int,
    present: Int,
    new: Int,
    left: Int,
    repeat: Int,
    assigned: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Row: Main Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MainStatItem(
                    icon = Icons.Filled.PeopleAlt,
                    label = "Total",
                    value = total
                )
                VerticalDivider2()
                MainStatItem(
                    icon = Icons.Filled.PersonPin,
                    label = "Present",
                    value = present
                )
                VerticalDivider2()
                MainStatItem(
                    icon = Icons.Filled.PersonAdd,
                    label = "New",
                    value = new
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalChip(label = "Left", value = left)
                HorizontalChip(label = "Repeat", value = repeat)
                HorizontalChip(label = "Assigned", value = assigned)
            }
        }
    }
}

@Composable
fun MainStatItem(icon: ImageVector, label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.toString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label.uppercase(),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HorizontalChip(label: String, value: Int) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
        modifier = Modifier
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$label:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun VerticalDivider2() {
    VerticalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .height(48.dp)
            .width(1.dp)
            .padding(horizontal = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3FilterChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    // Color based on label for certain filters
    val chipColor = when(label) {
        "Left" -> MaterialTheme.colorScheme.errorContainer
        "Present" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when(label) {
        "Left" -> MaterialTheme.colorScheme.onErrorContainer
        "Present" -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    }

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) textColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(16.dp),
                    tint = textColor
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = chipColor,
            selectedLabelColor = textColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            selectedBorderColor = chipColor,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            disabledSelectedBorderColor = chipColor.copy(alpha = 0.2f),
            borderWidth = 1.dp,
            selectedBorderWidth = 2.dp
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAttendeeCard(
    attendee: StudentAttendee,
    onDeleteClick: () -> Unit,
    onMarkLeftClick: () -> Unit,
    onJoinedBackClick: () -> Unit
) {
    val backgroundColor = when {
        attendee.hasLeft -> Color(0xFFFFEBEE) // Soft red for left
        attendee.isNew -> Color(0xFFF0FFF0)   // Soft green for new
        attendee.repeatedTimes > 3 -> Color(0xFFFFF8E1) // Warm yellow for repeat
        else -> Color(0xFFF8F9FA)              // Default light background
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar/Profile initial with status indicator
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (attendee.hasLeft) {
                                    listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer)
                                } else {
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                                }
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (attendee.hasLeft) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Left",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = attendee.name.first().toString().uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            attendee.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        // If has left, show time
                        if (attendee.hasLeft) {
                            Text(
                                "Left at ${attendee.leftTime}",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(end = 4.dp),
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        attendee.phone,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Status indicator
                if (attendee.isNew) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                "NEW",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            labelColor = MaterialTheme.colorScheme.tertiary
                        ),
                        border = null
                    )
                } else {
                    AssistChip(
                        onClick = { },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                "${attendee.repeatedTimes}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            labelColor = MaterialTheme.colorScheme.secondary,
                            leadingIconContentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Facilitator info
                InfoBadge(
                    icon = Icons.Outlined.Badge,
                    text = attendee.facilitator ?: "Unassigned",
                    modifier = Modifier.weight(1f),
                    isHighlighted = attendee.facilitator == null
                )

                // Registration date
                InfoBadge(
                    icon = Icons.Outlined.CalendarToday,
                    text = attendee.regDate,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Mark as Left button (only show if not already left)
                if (!attendee.hasLeft) {
                    OutlinedButton(
                        onClick = onMarkLeftClick,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.ExitToApp,
                            contentDescription = "Mark Left",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Left", fontSize = 12.sp)
                    }
                }else{
                    OutlinedButton(
                        onClick = onJoinedBackClick,
                        modifier = Modifier.padding(end = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.TurnedIn,
                            contentDescription = "Join Back",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Join Back", fontSize = 12.sp)
                    }
                }

                // Delete button
                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InfoBadge(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val backgroundColor = if (isHighlighted)
        MaterialTheme.colorScheme.errorContainer
    else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    val contentColor = if (isHighlighted)
        MaterialTheme.colorScheme.error
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .padding(end = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text,
                fontSize = 12.sp,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

        }
    }
}

