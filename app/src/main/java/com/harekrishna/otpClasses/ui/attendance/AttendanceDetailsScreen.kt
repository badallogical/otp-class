package com.harekrishna.otpClasses.ui.attendance

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.material.icons.outlined.Undo
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harekrishna.otpClasses.data.models.StudentAttendee
import com.harekrishna.otpClasses.ui.theme.AttendanceColors
import kotlinx.coroutines.delay
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceDetailsScreen(
    date: String,
    viewModel: AttendanceDetailViewModel = viewModel( factory = AttendanceDetailViewModel.Factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Convert to mutable state list to handle operations
    val uiState by viewModel.attendanceDetailsUiState.collectAsState()

    LaunchedEffect(Unit){
        viewModel.loadAttendanceDetailData(date)
    }

    val scrollState = rememberScrollState()

    if( uiState.isLoading ){
        CircularProgressIndicator()
    }


    val filteredAttendees by remember {
        derivedStateOf {
            if (uiState.isSearchMode) {
                uiState.attendanceList.filter { attendee ->
                    attendee.name.contains(uiState.searchText, ignoreCase = true) ||
                            attendee.phone.contains(uiState.searchText, ignoreCase = true) ||
                            (attendee.facilitator?.contains(uiState.searchText, ignoreCase = true) ?: false)
                }
            } else {
                when (uiState.selectedFilter) {
                    "New" -> uiState.attendanceList.filter { it.repeatedTimes == 1 && !it.deleted }
                    "Repeated" -> uiState.attendanceList.filter { it.repeatedTimes > 1 && !it.deleted }
                    "Unassigned" -> uiState.attendanceList.filter {
                        (it.facilitator.isNullOrBlank() || it.facilitator == "NA") && !it.deleted
                    }
                    "Assigned" -> uiState.attendanceList.filter {
                        !it.facilitator.isNullOrBlank() && it.facilitator != "NA" && !it.deleted
                    }
                    "Left" -> uiState.attendanceList.filter { it.hasLeft && !it.deleted }
                    "Present" -> uiState.attendanceList.filter { !it.hasLeft && !it.deleted }
                    "Deleted" -> uiState.attendanceList.filter { it.deleted }
                    else -> uiState.attendanceList.filter { !it.deleted }
                }
            }
        }
    }




    // Delete dialog
    if (uiState.showDeleteDialog && uiState.selectedAttendee != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteDialog() },
            title = { Text("${if (uiState.selectedAttendee!!.deleted ) "Undo" else "Delete"} Attendance") },
            text = { Text("Are you sure you want to ${if( uiState.selectedAttendee!!.deleted) "undo" else "delete"} ${uiState.selectedAttendee?.name}'s attendance record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                       viewModel.onDeleteAttendee()
                    }
                ) {
                    Text(if( uiState.selectedAttendee!!.deleted ) "Undo" else "Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Mark as left dialog
    if (uiState.showLeftDialog && uiState.selectedAttendee != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissMarkLeftDialog() },
            title = { Text("Mark as Left") },
            text = { Text("Mark ${uiState.selectedAttendee?.name} as having left the class?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onMarkLeft()
                    }
                ) {
                    Text("Mark Left")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissMarkLeftDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Mark as return dialog
    if (uiState.showReturnDialog && uiState.selectedAttendee != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissJoinedDialog() },
            title = { Text("Mark as Present") },
            text = { Text("Mark ${uiState.selectedAttendee?.name} as returned back in the class?") },
            confirmButton = {
                TextButton(
                    onClick = {
                       viewModel.onReturnBack()
                    }
                ) {
                    Text("Mark Again")
                }
            },
            dismissButton = {
                TextButton(onClick = {viewModel.onDismissJoinedDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AttendanceTopAppBar(
                date = date,
                totalAttendees = uiState.totalAttendees,
                presentCount = uiState.totalPresent,
                leftCount = uiState.totalLeft,
                totalNew = uiState.totalNew,
                attendeesList = uiState.attendanceList,
                context = context,
                onSyncClick = {
                    viewModel.syncAttendance()
                },
                onExportExcel = {
                    viewModel.exportAttendanceToExcel(
                        context = context,
                        date = date,
                        attendeesList = uiState.attendanceList
                    ) { uri ->
                        uri?.let {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, "Export Attendance")
                            )
                        } ?: run {
                            Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                        }
                    }
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
            SmartStatisticsCard(uiState.totalAttendees, uiState.totalPresent, uiState.totalNew, uiState.totalLeft, uiState.totalRepeated, uiState.assignedCount, uiState.isSyncing, uiState.syncStatus, uiState.totalSynced)

            Spacer(modifier = Modifier.height(8.dp))

            // Attendance Filter
            AttendanceFilterRow(
                searchText = uiState.searchText,
                onSearchTextChange = { viewModel.onSearchQuery(it) },
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = { viewModel.onFilter(it) },
                isSearchMode = uiState.isSearchMode,
                onSearchModeToggle = { viewModel.onSearchMode(it) }
            )

            // Filter info
            Text(
                "${filteredAttendees.size} ${if (uiState.selectedFilter == "All") "attendees" else "${uiState.selectedFilter} attendees"}",
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
                                viewModel.onDeleteAttendanceDialog(attendee)
                            },
                            onMarkLeftClick = {
                                viewModel.onMarkLeftDialog(attendee)
                            },
                            onJoinedBackClick = {
                                viewModel.onReturnBackDialog(attendee)
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
    onSyncClick: () -> Unit,
    onExportExcel: () -> Unit
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
                                attendeesList.filter{ !it.deleted }.forEachIndexed { index, attendee ->
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

                    DropdownMenuItem(
                        text = { Text("Export as Excel") },
                        onClick = {
                            showShareMenu = false
                            onExportExcel()
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
                        "Assigned",
                        "Deleted"
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
    isSyncing: Boolean,
    syncStatus: Boolean,
    totalSync: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()  // Use fillMaxWidth instead of fillMaxSize
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

            // Only show sync progress components if we're syncing or just finished
            if (isSyncing || syncStatus) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Add our simplified sync progress component
                SyncProgressIndicator(
                    isSyncing = isSyncing,
                    syncResult = if(isSyncing) null else syncStatus,
                    totalSynced = totalSync,
                    totalPresent = present
                )
            }
        }
    }
}

/**
 * A composable that shows sync progress and success/failure status
 */
@Composable
fun SyncProgressIndicator(
    isSyncing: Boolean,
    syncResult: Boolean?, // null when not synced yet, true for success, false for failure
    totalSynced: Int,
    totalPresent: Int,
    modifier: Modifier = Modifier
) {
    var showSuccessIcon by remember { mutableStateOf(false) }
    val animatedProgress = animateFloatAsState(
        targetValue = when {
            isSyncing -> (((totalSynced.toFloat() / totalPresent.takeIf { it > 0 }!!) ?: 1f))
            syncResult == true -> 1f
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "progressAnimation"
    )

    // Show success icon with delay
    LaunchedEffect(isSyncing, syncResult) {
        // When sync completes with success
        if (!isSyncing && syncResult == true) {
            delay(300) // Wait for progress bar to fill
            showSuccessIcon = true
            delay(1500) // Show success for 1.5 seconds
            showSuccessIcon = false
        } else {
            showSuccessIcon = false
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = !showSuccessIcon,
                exit = fadeOut()
            ) {
                Text(
                    text = when {
                        isSyncing -> "Syncing attendance..."
                        syncResult == true -> "Sync complete"
                        syncResult == false -> "Sync failed"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            AnimatedVisibility(
                visible = isSyncing,
                exit = fadeOut()
            ) {
                Text(
                    text = "$totalSynced/$totalPresent",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            showSuccessIcon || syncResult == true -> Color(0xFF4CAF50) // Green
                            syncResult == false -> Color(0xFFE53935) // Red
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
            )
        }

        // Success message
        AnimatedVisibility(
            visible = showSuccessIcon,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sync Complete",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sync complete",
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
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
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
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
    val isDark = isSystemInDarkTheme()

    val backgroundColor = when {
        attendee.hasLeft -> if (isDark) AttendanceColors.leftDark else AttendanceColors.leftLight
        attendee.isNew -> if (isDark) AttendanceColors.newDark else AttendanceColors.newLight
        attendee.repeatedTimes > 3 -> if (isDark) AttendanceColors.repeatDark else AttendanceColors.repeatLight
        else -> if (isDark) AttendanceColors.defaultDark else AttendanceColors.defaultLight              // Default light background
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
                    if(attendee.deleted ){
                        Icon(
                            Icons.Outlined.Undo,
                            contentDescription = "Undo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo", fontSize = 12.sp)
                    }else{
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