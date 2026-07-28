package com.harekrishna.otpClasses.ui.registration2

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import com.harekrishna.otpClasses.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =========================================================================================
// Registration Detail Screen
// -------------------------------------------------------------------------------------------
// Opens when a day's row is tapped on the Registrations list screen. Shows every individual
// registration recorded that day with basic details, and lets the user:
//  1. Share the ENTIRE day's list instantly via WhatsApp (top bar icon), or
//  2. Long-press / tap "Select" to enter selection mode, pick one or more entries with
//     checkboxes, then share just the selection via WhatsApp from the bottom bar.
//
// All colors come from MaterialTheme.colorScheme (Theme.kt) exactly like the list screen,
// so it automatically follows the app's light/dark Orange theme. No hardcoded hex colors.
// =========================================================================================

// -----------------------------------------------------------------------------------------
// MODELS
// -----------------------------------------------------------------------------------------

data class DetailRegistrationEntry(
    val id: String,
    val name: String,
    val phone: String,
    val time: String,          // e.g. "10:24 AM"
    val facilitator: String,
    val verified: Boolean = true
)

// -----------------------------------------------------------------------------------------
// UI STATE
// -----------------------------------------------------------------------------------------

data class RegistrationDetailUiState(
    val isLoading: Boolean = false,
    val dayLabel: String = "",
    val entries: List<DetailRegistrationEntry> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet()
) {
    val totalCount: Int get() = entries.size
    val verifiedCount: Int get() = entries.count { it.verified }
    val selectedCount: Int get() = selectedIds.size
    val isAllSelected: Boolean get() = entries.isNotEmpty() && selectedIds.size == entries.size
}

// -----------------------------------------------------------------------------------------
// VIEWMODEL
// -----------------------------------------------------------------------------------------

class RegistrationDetailViewModel(
    initialDayLabel: String = "Sun, Jul 12, 2026"
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationDetailUiState(dayLabel = initialDayLabel))
    val uiState: StateFlow<RegistrationDetailUiState> = _uiState.asStateFlow()

    init {
        loadDummyData()
    }

    private fun loadDummyData() {
        _uiState.update {
            it.copy(
                isLoading = false,
                entries = listOf(
                    DetailRegistrationEntry("1", "Amit Sharma", "+91 98765 43210", "9:02 AM", "Priya Singh"),
                    DetailRegistrationEntry("2", "Sunita Verma", "+91 91234 56780", "9:14 AM", "Priya Singh"),
                    DetailRegistrationEntry("3", "Rohit Kumar", "+91 99887 66554", "9:20 AM", "Amit Sharma"),
                    DetailRegistrationEntry("4", "Neha Gupta", "+91 90909 12345", "9:31 AM", "Amit Sharma"),
                    DetailRegistrationEntry("5", "Vikram Rao", "+91 98123 45670", "9:45 AM", "Rohit Kumar", verified = false),
                    DetailRegistrationEntry("6", "Kavita Joshi", "+91 97654 32109", "10:02 AM", "Priya Singh"),
                    DetailRegistrationEntry("7", "Manoj Tiwari", "+91 96543 21098", "10:15 AM", "Amit Sharma"),
                    DetailRegistrationEntry("8", "Anjali Mishra", "+91 95432 10987", "10:28 AM", "Rohit Kumar"),
                    DetailRegistrationEntry("9", "Deepak Yadav", "+91 94321 09876", "10:40 AM", "Priya Singh"),
                    DetailRegistrationEntry("10", "Pooja Nair", "+91 93210 98765", "11:05 AM", "Amit Sharma", verified = false),
                    DetailRegistrationEntry("11", "Sanjay Pillai", "+91 92109 87654", "11:22 AM", "Rohit Kumar"),
                    DetailRegistrationEntry("12", "Ritu Chawla", "+91 91098 76543", "11:40 AM", "Priya Singh"),
                    DetailRegistrationEntry("13", "Arjun Malhotra", "+91 90987 65432", "12:05 PM", "Amit Sharma"),
                    DetailRegistrationEntry("14", "Sneha Kapoor", "+91 89876 54321", "12:30 PM", "Rohit Kumar")
                )
            )
        }
    }

    fun onRowClick(id: String) {
        val selectionMode = _uiState.value.isSelectionMode
        if (selectionMode) toggleSelect(id)
    }

    fun onRowLongPress(id: String) {
        _uiState.update {
            val newSelected = it.selectedIds + id
            it.copy(isSelectionMode = true, selectedIds = newSelected)
        }
    }

    fun toggleSelect(id: String) {
        _uiState.update {
            val newSelected = if (id in it.selectedIds) it.selectedIds - id else it.selectedIds + id
            it.copy(selectedIds = newSelected)
        }
    }

    fun enterSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = true) }
    }

    fun exitSelectionMode() {
        _uiState.update { it.copy(isSelectionMode = false, selectedIds = emptySet()) }
    }

    fun toggleSelectAll() {
        _uiState.update {
            if (it.isAllSelected) {
                it.copy(selectedIds = emptySet())
            } else {
                it.copy(selectedIds = it.entries.map { e -> e.id }.toSet())
            }
        }
    }

    /** Text for sharing ALL of the day's registrations. */
    fun buildShareTextForAll(): String = buildShareText(_uiState.value.dayLabel, _uiState.value.entries)

    /** Text for sharing only the currently selected registrations. */
    fun buildShareTextForSelection(): String {
        val state = _uiState.value
        val selected = state.entries.filter { it.id in state.selectedIds }
        return buildShareText(state.dayLabel, selected)
    }

    private fun buildShareText(dayLabel: String, entries: List<DetailRegistrationEntry>): String {
        val sb = StringBuilder()
        sb.appendLine("📋 Registrations — $dayLabel")
        sb.appendLine("Total: ${entries.size}")
        sb.appendLine()
        entries.forEachIndexed { index, e ->
            sb.appendLine("${index + 1}. ${e.name} — ${e.phone}")
        }
        return sb.toString().trimEnd()
    }
}

// -----------------------------------------------------------------------------------------
// WHATSAPP SHARE HELPER
// -----------------------------------------------------------------------------------------

private fun shareViaWhatsApp(context: Context, text: String) {
    val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage("com.whatsapp")
    }
    try {
        context.startActivity(whatsappIntent)
    } catch (e: ActivityNotFoundException) {
        // WhatsApp not installed — fall back to the system share sheet.
        val chooserIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(chooserIntent, "Share registrations via"))
    }
}

// -----------------------------------------------------------------------------------------
// ROOT COMPOSABLE (wires ViewModel + Context for sharing)
// -----------------------------------------------------------------------------------------

@Composable
fun RegistrationDetailRoute(
    dayLabel: String = "Sun, Jul 12, 2026",
    onBack: () -> Unit = {},
    viewModel: RegistrationDetailViewModel = viewModel { RegistrationDetailViewModel(dayLabel) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    RegistrationListDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRowClick = viewModel::onRowClick,
        onRowLongPress = viewModel::onRowLongPress,
        onEnterSelectionMode = viewModel::enterSelectionMode,
        onExitSelectionMode = viewModel::exitSelectionMode,
        onToggleSelectAll = viewModel::toggleSelectAll,
        onShareAll = { shareViaWhatsApp(context, viewModel.buildShareTextForAll()) },
        onShareSelected = { shareViaWhatsApp(context, viewModel.buildShareTextForSelection()) }
    )
}

// -----------------------------------------------------------------------------------------
// STATELESS SCREEN
// -----------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RegistrationListDetailScreen(
    uiState: RegistrationDetailUiState,
    onBack: () -> Unit = {},
    onRowClick: (String) -> Unit = {},
    onRowLongPress: (String) -> Unit = {},
    onEnterSelectionMode: () -> Unit = {},
    onExitSelectionMode: () -> Unit = {},
    onToggleSelectAll: () -> Unit = {},
    onShareAll: () -> Unit = {},
    onShareSelected: () -> Unit = {}
) {
    if (uiState.isSelectionMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = if (uiState.isSelectionMode) onExitSelectionMode else onBack) {
                        Icon(
                            imageVector = if (uiState.isSelectionMode) Icons.Default.Close else Icons.Default.ArrowBack,
                            contentDescription = if (uiState.isSelectionMode) "Cancel selection" else "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    if (uiState.isSelectionMode) {
                        Text(
                            text = "${uiState.selectedCount} selected",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = uiState.dayLabel,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (uiState.isSelectionMode) {
                        TextButton(onClick = onToggleSelectAll) {
                            Text(
                                text = if (uiState.isAllSelected) "Clear" else "Select all",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        IconButton(onClick = onEnterSelectionMode) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Select registrations",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onShareAll) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share all via WhatsApp",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.isSelectionMode) {
                SelectionShareBar(
                    selectedCount = uiState.selectedCount,
                    onShareSelected = onShareSelected
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DaySummaryCard(uiState = uiState)
                }

                items(uiState.entries, key = { it.id }) { entry ->
                    DetailEntryRow(
                        entry = entry,
                        isSelectionMode = uiState.isSelectionMode,
                        isSelected = entry.id in uiState.selectedIds,
                        onClick = { onRowClick(entry.id) },
                        onLongPress = { onRowLongPress(entry.id) }
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DAY SUMMARY CARD
// -----------------------------------------------------------------------------------------

@Composable
private fun DaySummaryCard(uiState: RegistrationDetailUiState) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
            MaterialTheme.colorScheme.surface
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${uiState.totalCount}",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total registrations",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.verifiedCount} verified",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// ENTRY ROW
// -----------------------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DetailEntryRow(
    entry: DetailRegistrationEntry,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.0f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                InitialsAvatar(name = entry.name)
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (entry.verified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = entry.phone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${entry.time}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun InitialsAvatar(name: String) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

// -----------------------------------------------------------------------------------------
// BOTTOM SHARE BAR (selection mode)
// -----------------------------------------------------------------------------------------

@Composable
private fun SelectionShareBar(
    selectedCount: Int,
    onShareSelected: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCount == 0) "Select registrations to share" else "$selectedCount selected",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onShareSelected,
                enabled = selectedCount > 0,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Share via WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// PREVIEWS
// -----------------------------------------------------------------------------------------

private fun dummyDetailUiState(
    isSelectionMode: Boolean = false,
    selectedIds: Set<String> = emptySet()
): RegistrationDetailUiState {
    val entries = listOf(
        DetailRegistrationEntry("1", "Amit Sharma", "+91 98765 43210", "9:02 AM", "Priya Singh"),
        DetailRegistrationEntry("2", "Sunita Verma", "+91 91234 56780", "9:14 AM", "Priya Singh"),
        DetailRegistrationEntry("3", "Rohit Kumar", "+91 99887 66554", "9:20 AM", "Amit Sharma"),
        DetailRegistrationEntry("4", "Neha Gupta", "+91 90909 12345", "9:31 AM", "Amit Sharma"),
        DetailRegistrationEntry("5", "Vikram Rao", "+91 98123 45670", "9:45 AM", "Rohit Kumar", verified = false),
        DetailRegistrationEntry("6", "Kavita Joshi", "+91 97654 32109", "10:02 AM", "Priya Singh")
    )
    return RegistrationDetailUiState(
        dayLabel = "Sun, Jul 12, 2026",
        entries = entries,
        isSelectionMode = isSelectionMode,
        selectedIds = selectedIds
    )
}

@Preview(showBackground = true, name = "Detail - Light")
@Composable
private fun RegistrationListDetailScreenLightPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        var state by remember { mutableStateOf(dummyDetailUiState()) }
        RegistrationListDetailScreen(
            uiState = state,
            onRowClick = { id ->
                if (state.isSelectionMode) {
                    val newSelected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
                    state = state.copy(selectedIds = newSelected)
                }
            },
            onRowLongPress = { id -> state = state.copy(isSelectionMode = true, selectedIds = state.selectedIds + id) },
            onEnterSelectionMode = { state = state.copy(isSelectionMode = true) },
            onExitSelectionMode = { state = state.copy(isSelectionMode = false, selectedIds = emptySet()) },
            onToggleSelectAll = {
                state = if (state.isAllSelected) state.copy(selectedIds = emptySet())
                else state.copy(selectedIds = state.entries.map { it.id }.toSet())
            }
        )
    }
}

@Preview(showBackground = true, name = "Detail - Selection Mode - Light")
@Composable
private fun RegistrationListDetailScreenSelectionLightPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        var state by remember {
            mutableStateOf(dummyDetailUiState(isSelectionMode = true, selectedIds = setOf("1", "3")))
        }
        RegistrationListDetailScreen(
            uiState = state,
            onRowClick = { id ->
                val newSelected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
                state = state.copy(selectedIds = newSelected)
            },
            onExitSelectionMode = { state = state.copy(isSelectionMode = false, selectedIds = emptySet()) },
            onToggleSelectAll = {
                state = if (state.isAllSelected) state.copy(selectedIds = emptySet())
                else state.copy(selectedIds = state.entries.map { it.id }.toSet())
            }
        )
    }
}

@Preview(showBackground = true, name = "Detail - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RegistrationListDetailScreenDarkPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        var state by remember { mutableStateOf(dummyDetailUiState()) }
        RegistrationListDetailScreen(
            uiState = state,
            onRowClick = { id ->
                if (state.isSelectionMode) {
                    val newSelected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
                    state = state.copy(selectedIds = newSelected)
                }
            },
            onRowLongPress = { id -> state = state.copy(isSelectionMode = true, selectedIds = state.selectedIds + id) },
            onEnterSelectionMode = { state = state.copy(isSelectionMode = true) },
            onExitSelectionMode = { state = state.copy(isSelectionMode = false, selectedIds = emptySet()) },
            onToggleSelectAll = {
                state = if (state.isAllSelected) state.copy(selectedIds = emptySet())
                else state.copy(selectedIds = state.entries.map { it.id }.toSet())
            }
        )
    }
}

@Preview(showBackground = true, name = "Detail - Selection Mode - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RegistrationListDetailScreenSelectionDarkPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        var state by remember {
            mutableStateOf(dummyDetailUiState(isSelectionMode = true, selectedIds = setOf("2", "4", "6")))
        }
        RegistrationListDetailScreen(
            uiState = state,
            onRowClick = { id ->
                val newSelected = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id
                state = state.copy(selectedIds = newSelected)
            },
            onExitSelectionMode = { state = state.copy(isSelectionMode = false, selectedIds = emptySet()) },
            onToggleSelectAll = {
                state = if (state.isAllSelected) state.copy(selectedIds = emptySet())
                else state.copy(selectedIds = state.entries.map { it.id }.toSet())
            }
        )
    }
}