package com.harekrishna.otpClasses.ui.attendance

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File

// Dummy Data Class
data class StudentAttendee(
    val name: String,
    val phone: String,
    val facilitator: String?,
    val repeatedTimes: Int,
    val isNew: Boolean,
    val regDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDetailsScreen(
    date: String,
    attendees: List<StudentAttendee>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val totalAttendees = attendees.size
    val totalNew = attendees.count { it.isNew }
    val totalRepeated = attendees.count { it.repeatedTimes > 1 }
    val assignedCount = attendees.count { it.facilitator != null }

    var selectedFilter by remember { mutableStateOf("All") }
    val scrollState = rememberScrollState()
    val filteredAttendees = when (selectedFilter) {
        "New" -> attendees.filter { it.isNew }
        "Repeated" -> attendees.filter { it.repeatedTimes > 1 }
        "Unassigned" -> attendees.filter { it.facilitator == null }
        "Assigned" -> attendees.filter { it.facilitator != null }
        else -> attendees
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Attendance Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        val csvData = buildString {
                            append("Name,Phone,Facilitator,RepeatedTimes,IsNew,RegDate\n")
                            attendees.forEach {
                                append("\"${it.name}\",\"${it.phone}\",\"${it.facilitator ?: "Unassigned"}\",${it.repeatedTimes},${it.isNew},\"${it.regDate}\"\n")
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
                            Intent.createChooser(
                                shareIntent,
                                "Export Attendance"
                            )
                        )
                    }) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = "Export",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = {
                        val message = buildString {
                            append("Attendance Report for $date\n")
                            append("Total: $totalAttendees\nNew: $totalNew\n\n")
                            attendees.forEachIndexed { index, attendee ->
                                append("${index + 1}. ${attendee.name} - ${attendee.phone}\n")
                            }
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share via"))
                    }) {
                        Icon(
                            Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
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
            // Date header
            Text(
                "Report for $date",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Statistics Cards - 2x2 Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticCard(
                    title = "Total",
                    value = totalAttendees.toString(),
                    icon = Icons.Filled.PeopleAlt,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                StatisticCard(
                    title = "New",
                    value = totalNew.toString(),
                    icon = Icons.Filled.PersonAdd,
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticCard(
                    title = "Repeat",
                    value = totalRepeated.toString(),
                    icon = Icons.Filled.Repeat,
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                StatisticCard(
                    title = "Assigned",
                    value = "$assignedCount",
                    icon = Icons.Filled.Assignment,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "New", "Repeated", "Unassigned", "Assigned")
                filters.forEach { label ->
                    Material3FilterChip(
                        label = label,
                        selected = selectedFilter == label,
                        onClick = { selectedFilter = label }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter info
            Text(
                "${filteredAttendees.size} ${if (selectedFilter == "All") "attendees" else "$selectedFilter attendees"}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
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
                        EnhancedAttendeeCard(attendee = attendee)
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

@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .height(100.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                Text(
                    title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3FilterChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = false,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            disabledSelectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            borderWidth = 1.dp,
            selectedBorderWidth = 2.dp
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAttendeeCard(attendee: StudentAttendee) {
    val backgroundColor = when {
        attendee.isNew -> Color(0xFFF0FFF0)
        attendee.repeatedTimes > 3 -> Color(0xFFFFF8E1)
        else -> Color(0xFFF8F9FA)
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
                // Avatar/Profile initial
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = attendee.name.first().toString().uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        attendee.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

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


@Preview(showBackground = true)
@Composable
fun AttendanceDetailsScreenPreview() {
    val dummyList = listOf(
        StudentAttendee(
            name = "Radha Devi",
            phone = "9999999999",
            facilitator = "Shyam Lal",
            repeatedTimes = 2,
            isNew = true,
            regDate = "2024-04-01"
        ),
        StudentAttendee(
            name = "Mohan Kumar",
            phone = "8888888888",
            facilitator = null,
            repeatedTimes = 4,
            isNew = false,
            regDate = "2024-03-25"
        )
    )

    MaterialTheme(colorScheme = lightColorScheme()) {
        AttendanceDetailsScreen(date = "2024-04-07", attendees = dummyList)
    }
}
