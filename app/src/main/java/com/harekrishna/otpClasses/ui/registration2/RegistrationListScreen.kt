package com.harekrishna.otpClasses.ui.registration2

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import com.harekrishna.otpClasses.ui.theme.ThemeMode

@Composable
fun RegistrationListScreen(
    onNewRegistrationClick: () -> Unit,
    viewModel: RegistrationListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(16.dp),
                onClick = { onNewRegistrationClick() },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Registration") },
                text = { Text("New Registration") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                RegistrationHeader(onRefresh = viewModel::onRefresh)
            }

            item {
                RegistrationSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )
            }

            item {
                LiveSummaryCard(
                    uiState = uiState,
                    onToggleLeaderboard = viewModel::onToggleLeaderboard
                )
            }

            uiState.groups.forEachIndexed { groupIndex, group ->
                item {
                    Text(
                        text = group.sectionTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                itemsIndexed(group.entries) { entryIndex, entry ->
                    RegistrationRow(entry = entry)
                }
            }
        }
    }
}


// Helper because LazyListScope.itemsIndexed on a Kotlin List needs the extension import
private inline fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexed(
    list: List<RegistrationEntry>,
    crossinline itemContent: @Composable (Int, RegistrationEntry) -> Unit
) {
    items(list.size) { index ->
        itemContent(index, list[index])
    }
}

@Composable
private fun RegistrationHeader(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Registrations",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun RegistrationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            if (query.isEmpty()) {
                Text(
                    text = "Search student by name or phone",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                ),
                cursorBrush = Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// LIVE SUMMARY / LEADERBOARD CARD
// -----------------------------------------------------------------------------------------

@Composable
private fun LiveSummaryCard(
    uiState: RegistrationListUiState,
    onToggleLeaderboard: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.50f),
            MaterialTheme.colorScheme.secondary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(gradient)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(20.dp)
    ) {
        if (uiState.showLeaderboard) {
            LeaderboardContent(
                rankings = uiState.rankings,
                onBack = onToggleLeaderboard
            )
        } else {
            SummaryContent(
                uiState = uiState,
                onLeaderboardClick = onToggleLeaderboard
            )
        }
    }
}

@Composable
private fun SummaryContent(
    uiState: RegistrationListUiState,
    onLeaderboardClick: () -> Unit
) {
    Column {
        LiveBadge(text = "LIVE · UPDATES INSTANTLY")

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "${uiState.todayCount}",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Registrations across all facilitators today",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            AvatarStack(
//                initials = uiState.facilitatorInitials,
//                extraCount = uiState.extraFacilitatorCount
//            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Leaderboard →",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onLeaderboardClick() }
            )
        }
    }
}

@Composable
private fun LeaderboardContent(
    rankings: List<RankingEntry>,
    onBack: () -> Unit
) {
    Column {
        LiveBadge(text = "TODAY'S RANKING")

        Spacer(modifier = Modifier.height(14.dp))

        rankings.forEachIndexed { index, entry ->
            RankingRow(entry = entry)
            if (index != rankings.lastIndex) {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "← Back to summary",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.clickable { onBack() }
        )
    }
}

@Composable
private fun LiveBadge(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun AvatarStack(
    initials: List<String>,
    extraCount: Int
) {
    Row {
        initials.forEachIndexed { index, initial ->
            Box(
                modifier = Modifier
                    .offset(x = (-8 * index).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
        if (extraCount > 0) {
            Box(
                modifier = Modifier
                    .offset(x = (-8 * initials.size).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$extraCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RankingRow(entry: RankingEntry) {
    val badgeColor = when (entry.rank) {
        1 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val badgeTextColor = when (entry.rank) {
        1 -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${entry.rank}",
                color = badgeTextColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = entry.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${entry.count}",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

// -----------------------------------------------------------------------------------------
// WEEKLY LIST ROWS
// -----------------------------------------------------------------------------------------

@Composable
private fun RegistrationRow(entry: RegistrationEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        RegistrationRowContent(entry)
    }
}

@Composable
private fun RegistrationRowContent(entry: RegistrationEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.dayLabel,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Registrations: ${entry.count}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (entry.verified) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Verified",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun dummyUiState(showLeaderboard: Boolean = false) = RegistrationListUiState(
    todayCount = 14,
    facilitatorInitials = listOf("AS", "PS", "RK"),
    extraFacilitatorCount = 3,
    rankings = listOf(
        RankingEntry(1, "Amit Sharma", 6),
        RankingEntry(2, "Priya Singh", 4),
        RankingEntry(3, "Rohit Kumar", 2),
        RankingEntry(4, "Neha Gupta", 2)
    ),
    groups = listOf(
        RegistrationGroup(
            sectionTitle = "All Registration",
            entries = listOf(
                RegistrationEntry("Sun, Jun 28, 2026", 6),
                RegistrationEntry("Sun, Jun 21, 2026", 5),
                RegistrationEntry("Sun, Jun 14, 2026", 2),
                RegistrationEntry("Sun, May 31, 2026", 8)
            )
        )
    ),
    showLeaderboard = showLeaderboard
)

@Preview(showBackground = true, name = "Summary - Light")
@Composable
private fun RegistrationListScreenSummaryLightPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        var state by remember { mutableStateOf(dummyUiState()) }
        RegistrationListScreen({})
    }
}

@Preview(showBackground = true, name = "Leaderboard - Light")
@Composable
private fun RegistrationListScreenLeaderboardLightPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        var state by remember { mutableStateOf(dummyUiState(showLeaderboard = true)) }
        RegistrationListScreen({})
    }
}

@Preview(
    showBackground = true,
    name = "Summary - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RegistrationListScreenSummaryDarkPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        var state by remember { mutableStateOf(dummyUiState()) }
        RegistrationListScreen({})
    }
}

@Preview(
    showBackground = true,
    name = "Leaderboard - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RegistrationListScreenLeaderboardDarkPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        var state by remember { mutableStateOf(dummyUiState(showLeaderboard = true)) }
        RegistrationListScreen({})
    }
}