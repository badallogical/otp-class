package com.harekrishna.otpClasses.ui.followup

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.models.AttendeeItem
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FollowUpScreen(viewModel: FollowUpViewModel = viewModel(factory = FollowUpViewModel.Factory)) {
    val uiState by viewModel.uiState.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        2 // Total number of pages
    }

    // Custom animation spec for smoother transitions
    val animationSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    // Effect to sync pagerState with viewModel's selectedTab
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onTabSelected(pagerState.currentPage)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Follow Up",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    IconButton(
                        onClick = { viewModel.sendFollowUpReport(context) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val tabs = listOf("Attendee List", "Summary")

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50)),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .padding(horizontal = 24.dp),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    divider = { }  // Remove default divider
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        page = index
                                    )
                                }
                            },
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> Icons.Rounded.Person
                                        else -> Icons.Rounded.Info
                                    },
                                    contentDescription = title,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (pagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (pagerState.currentPage == index)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal
                                    ),
                                    color = if (pagerState.currentPage == index)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            pageSpacing = 16.dp,
            beyondBoundsPageCount = 1
        ) { page ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                when (page) {
                    0 -> AttendeeListTab(viewModel)
                    1 -> SummaryTab()
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendeeListTab(viewModel: FollowUpViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Pass filter and sort logic as lambdas
        FilterAndSortOptions(
            onFilterSelected = { filter ->
                viewModel.filterStudents(filter)
            },
            onSortSelected = { sort ->
                viewModel.sortStudents(sort)
            },
            onFilterDropDownSelected = { viewModel.onFitlerDropDownSelected() },
            onSortDropDownSelected = { viewModel.onSortDropDownSelected() },
            onDismissFilterDropDown = { viewModel.onDismissFilterDropDown() },
            onDismissSortDropDown = { viewModel.onDismissSortDropDown()},
            uiState = uiState
        )

        // Display the list of attendees or an empty state if no students exist
        if (uiState.filteredAttendee.isEmpty()) {
            EmptyState(
                icon = R.drawable.baseline_search_24,  // Replace with your actual icon
                message = "No devotee found with attendance."
            )
        } else {
            // Display the list of students if it's not empty
            LazyColumn {
                items(uiState.filteredAttendee) { student ->
                    AttendeeListItem(student, onStudentUpdated =  { updatedReport: AttendeeItem ->
                        // Handle student status update
                        viewModel.updateStudentStatus(updatedReport.phone, updatedReport.callingStatus,updatedReport.isInvited, updatedReport.isActive,updatedReport.feedback)
                    },  onMessageIconClicked = { report: AttendeeItem ->
                        viewModel.sendWhatsAppMessage(context,report.phone, report.name)
                    }, viewModel)
                }
            }
        }
    }
}


@Composable
fun FilterAndSortOptions(
    onFilterSelected: (String) -> Unit,
    onSortSelected: (String) -> Unit,
    onFilterDropDownSelected: () -> Unit,
    onSortDropDownSelected: () -> Unit,
    onDismissFilterDropDown: () -> Unit,
    onDismissSortDropDown: () -> Unit,
    uiState: FollowUpUiState
) {
    val filterOptions: List<String> = listOf(
        "All",
        "Last Present",
        "Last Absent",
        "Last 4 Weeks Present",
        "Last 4 Weeks Absent"
    )

    val sortOptions: List<String> = listOf("None", "Name", "Date", "Attendance")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Box for Filter Chip and Dropdown
        Box(modifier = Modifier.weight(1f)) { // Wrap with weight here
            Column {
                FilterChip(
                    selected = uiState.selectedFilter != "All",
                    onClick = { onFilterDropDownSelected() },
                    label = {
                        Text(
                            text = uiState.selectedFilter,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (uiState.isFilterDropdownExpanded)
                                Icons.Rounded.KeyboardArrowUp
                            else
                                Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.primary,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.width(200.dp)
                )

                // Filter Dropdown
                DropdownMenu(
                    expanded = uiState.isFilterDropdownExpanded,
                    onDismissRequest = onDismissFilterDropDown,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .width(200.dp)
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = { onFilterSelected(option) },
                            leadingIcon = {
                                RadioButton(
                                    selected = uiState.selectedFilter == option,
                                    onClick = null
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }

        // Box for Sort Chip and Dropdown
        Box(modifier = Modifier.weight(1f)) { // Wrap with weight here
            Column {
                FilterChip(
                    selected = uiState.selectedSort != "None",
                    onClick = { onSortDropDownSelected() },
                    label = {
                        Text(
                            text = if (uiState.selectedSort == "None") "Sort" else uiState.selectedSort,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = if (uiState.isSortDropdownExpanded)
                                Icons.Rounded.KeyboardArrowUp
                            else
                                Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        iconColor = MaterialTheme.colorScheme.primary,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                )

                // Sort Dropdown
                DropdownMenu(
                    expanded = uiState.isSortDropdownExpanded,
                    onDismissRequest = onDismissSortDropDown,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .width(200.dp)
                ) {
                    sortOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = { onSortSelected(option) },
                            leadingIcon = {
                                RadioButton(
                                    selected = uiState.selectedSort == option,
                                    onClick = null
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun EmptyState(icon: Int, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendeeListItem(
    student: AttendeeItem,
    onStudentUpdated: (AttendeeItem) -> Unit,
    onMessageIconClicked: (AttendeeItem) -> Unit,
    viewModel: FollowUpViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val lastFourSundays = FollowUpViewModel.getLastFourSundays()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(
                width = 0.5.dp, // Reduced border width for subtlety
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Reduced elevation for modern look
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // Using surface color for cleaner look
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing between sections
        ) {
            // Header Row: Name, Phone, and Attendance Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name and Phone section
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = student.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Attendance Counter Badge
                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = student.attendances.size.toString(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Status and Active Switch Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { showDialog = true },
                    label = {
                        Text(
                            text = student.callingStatus.split(",").firstOrNull()?.trim()
                                ?: student.callingStatus,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.width(100.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                ThinSwitch(
                    checked = student.isActive,
                    onCheckedChange = { viewModel.onSwitchChange(student) }
                )
            }

            // Progress and Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attendance Progress Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(lastFourSundays.size) { index ->
                        Surface(
                            modifier = Modifier
                                .size(width = 24.dp, height = 4.dp),
                            shape = RoundedCornerShape(2.dp),
                            color = if (student.attendances.contains(lastFourSundays[3-index]))
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ) {}
                    }
                }

                // Action Icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${student.phone}")
                            }
                            context.startActivity(intent)
                            showDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onStudentUpdated(student.copy(isInvited = true))
                            onMessageIconClicked(student)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Message",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Feedback Section
            if (student.feedback.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "\"${student.feedback}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    if (showDialog) {
        showCallingStatusDialog(
            student = student,
            onDismiss = { showDialog = false },
            onSave = { updatedStudent ->
                onStudentUpdated(updatedStudent)
                showDialog = false
            }
        )
    }
}

@Composable
fun ThinSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(checked, label = "Switch State")

    val thumbOffset by transition.animateDp(
        label = "Thumb Offset",
        transitionSpec = { spring(stiffness = Spring.StiffnessLow) }
    ) { if (it) 20.dp else 0.dp }

    Box(
        modifier = modifier
            .width(40.dp)
            .height(20.dp)
            .clip(CircleShape)
            .background(
                if (checked)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(16.dp)
                .offset(x = thumbOffset),
            shape = CircleShape,
            color = if (checked)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        ) {}
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun showCallingStatusDialog(
    student: AttendeeItem,
    onDismiss: () -> Unit,
    onSave: (AttendeeItem) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(student.callingStatus) }
    var reason by remember { mutableStateOf(TextFieldValue("")) }
    var otherReason by remember { mutableStateOf(TextFieldValue("")) }
    var feedback by remember { mutableStateOf(TextFieldValue("")) }  // New feedback variable


    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Update Calling Status") },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedStatus == "Yes",
                        onClick = { selectedStatus = "Yes" }
                    )
                    Text(text = "Yes")
                }

                Row( verticalAlignment = Alignment.CenterVertically ){
                    RadioButton(
                        selected = selectedStatus == "No",
                        onClick = { selectedStatus = "No" })
                    Text(text = "No")
                }
                if (selectedStatus == "No") {
                    TextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text(text = "Reason") }
                    )
                }

                Row( verticalAlignment = Alignment.CenterVertically ){
                    RadioButton(
                        selected = selectedStatus == "Will Try",
                        onClick = { selectedStatus = "Will Try" })
                    Text(text = "Will Try")
                }

                Row( verticalAlignment = Alignment.CenterVertically ){
                    RadioButton(
                        selected = selectedStatus == "❗",
                        onClick = { selectedStatus = "❗" })
                    Text(text = "❗")
                }
                if (selectedStatus == "❗") {
                    TextField(
                        value = otherReason,
                        onValueChange = { otherReason = it },
                        label = { Text(text = "Reason") }
                    )
                }

                // Feedback section
                TextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = { Text(text = "Feedback") },  // Feedback label
                    placeholder = { Text(text = "Enter your feedback...") }  // Optional placeholder
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Save the updated student status
                // Save the updated student status
                val formattedStatus: String = when (selectedStatus) {
                    "No" -> "No, ${reason.text}"
                    "❗" -> "❗, ${otherReason.text}"
                    else -> selectedStatus
                }

               onSave(student.copy(callingStatus = formattedStatus, feedback = feedback.text.ifEmpty { student.feedback }) )
            }) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = "Cancel")
            }
        }
    )
}



// Sample implementation for the SummaryTab
@Composable
fun SummaryTab() {
    // Implement the pie chart summary or any other content here
    Text("Summary will be displayed here.")
}

@Preview
@Composable
fun PreviewFollowUpScreen() {

    val sampleAttendee= AttendeeItem(
        name = "John Doe",
        phone = "123-456-7890",
        callingStatus = "Called",
        attendances = listOf("2023-12-20", "2023-12-22", "2023-12-27"),
        isActive = true,
        isInvited = false,
        feedback = "Great session!",
        registrationDate = "2023-12-15"
    )

//    AttendeeListItem( sampleAttendee, {},{})

}
