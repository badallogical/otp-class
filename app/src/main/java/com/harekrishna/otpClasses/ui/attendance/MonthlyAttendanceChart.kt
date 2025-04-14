package com.harekrishna.otpClasses.ui.attendance

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.harekrishna.otpClasses.data.models.AttendanceResponse
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthlyAttendanceChart(
    attendance: AttendanceResponse,
    modifier: Modifier = Modifier,
    maxPossibleTotal: Int = calculateSundaysInCurrentYear() // Total Sundays in current year
) {
    val monthCounts = listOf(
        "Jan" to attendance.janCount,
        "Feb" to attendance.febCount,
        "Mar" to attendance.marCount,
        "Apr" to attendance.aprCount,
        "May" to attendance.mayCount,
        "Jun" to attendance.junCount,
        "Jul" to attendance.julCount,
        "Aug" to attendance.augCount,
        "Sep" to attendance.sepCount,
        "Oct" to attendance.octCount,
        "Nov" to attendance.novCount,
        "Dec" to attendance.decCount
    )

    // Colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val currentMonthColor = MaterialTheme.colorScheme.tertiary

    val totalCount = attendance.totalCount
    val maxMonthlyCount = monthCounts.maxOfOrNull { it.second } ?: 1
    val currentMonthIndex = remember { LocalDate.now().monthValue - 1 }

    // Animation states
    val animatedProgress by animateFloatAsState(
        targetValue = totalCount / maxPossibleTotal.toFloat(),
        animationSpec = tween(1000),
        label = "progressAnimation"
    )

    var selectedMonthIndex by remember { mutableStateOf<Int?>(null) }
    var showTooltip by remember { mutableStateOf(false) }
    var tooltipOffset by remember { mutableStateOf(Offset.Zero) }
    var tooltipText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = backgroundColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Header with total attendance progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Sunday Attendance ${LocalDate.now().year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total: $totalCount/$maxPossibleTotal",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // Animated progress indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(primaryColor, secondaryColor)
                            )
                        )
                )
            }
        }

        // Reference line for better visualization
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.LightGray.copy(alpha = 0.5f))
        )

        Spacer(Modifier.height(8.dp))

        // Monthly chart
        Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            // Horizontal guide lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val yStep = size.height / 4
                for (i in 0..3) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, size.height - (i * yStep)),
                        end = Offset(size.width, size.height - (i * yStep)),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Bars
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                monthCounts.forEachIndexed { index, (month, count) ->
                    val barHeightFraction = count.toFloat() / maxMonthlyCount
                    val barHeight by animateFloatAsState(
                        targetValue = barHeightFraction,
                        animationSpec = spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "barHeightAnimation$index"
                    )

                    val isCurrentMonth = index == currentMonthIndex
                    val isSelected = index == selectedMonthIndex

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        selectedMonthIndex = index
                                        showTooltip = true
                                        tooltipOffset = offset
                                        tooltipText = "$month: $count days"
                                    }
                                )
                            }
                    ) {
                        Spacer(modifier = Modifier.weight(1f - barHeight))

                        // The bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .weight(barHeight)
                                .padding(horizontal = 2.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 4.dp,
                                        topEnd = 4.dp
                                    )
                                )
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = when {
                                            isSelected -> listOf(secondaryColor, primaryColor)
                                            isCurrentMonth -> listOf(currentMonthColor, primaryColor)
                                            else -> listOf(primaryColor.copy(alpha = 0.7f), primaryColor)
                                        }
                                    )
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = RoundedCornerShape(
                                        topStart = 4.dp,
                                        topEnd = 4.dp
                                    )
                                )
                        )

                        Spacer(Modifier.height(8.dp))

                        // Month label
                        Text(
                            text = month,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrentMonth) currentMonthColor else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Fixed: Use Box-scoped AnimatedVisibility instead of ColumnScope version
            if (showTooltip) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier
                        .offset { IntOffset(tooltipOffset.x.toInt() - 75, tooltipOffset.y.toInt() - 70) }
                ) {
                    Box(
                        modifier = Modifier
                            .width(150.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = tooltipText,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }

    // Close tooltip when clicked elsewhere
    LaunchedEffect(showTooltip) {
        if (showTooltip) {
            delay(3000)
            showTooltip = false
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateSundaysInCurrentYear(): Int {
    val year = LocalDate.now().year
    val firstDayOfYear = LocalDate.of(year, 1, 1)
    val lastDayOfYear = LocalDate.of(year, 12, 31)
    var sundayCount = 0

    // Loop through the days of the year to count Sundays
    var currentDate = firstDayOfYear
    while (currentDate <= lastDayOfYear) {
        if (currentDate.dayOfWeek == DayOfWeek.SUNDAY) {
            sundayCount++
        }
        currentDate = currentDate.plusDays(1)
    }

    return sundayCount
}






@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MonthlyAttendanceChartPreview() {
    val dummyAttendance = AttendanceResponse(
        phone = "9876543210",
        janCount = 12,
        febCount = 18,
        marCount = 21,
        aprCount = 17,
        mayCount = 20,
        junCount = 22,
        julCount = 19,
        augCount = 15,
        sepCount = 16,
        octCount = 23,
        novCount = 20,
        decCount = 25
    )

    MonthlyAttendanceChart(
        attendance = dummyAttendance
    )
}
