package com.harekrishna.otpClasses.ui.events

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.harekrishna.otpClasses.R
import com.harekrishna.otpClasses.data.models.SangkirtanRegistrationStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HarinaamListScreen(
    navController: NavController,
    viewModel: HarinaamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getRegistration()
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(16.dp),
                onClick = { navController.navigate("harinaam_form") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Registration") },
                text = { Text("New Registration") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            HeaderSection(viewModel, uiState.isSyncing, uiState.isInSelectionMode)
            
            if (uiState.isInSelectionMode) {
                SelectionHeader(uiState, viewModel)
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                HarinaamListView(uiState.registrations, uiState, viewModel,navController)
            }

            DeleteConfirmationDialog(uiState, viewModel)
        }
    }
}

@Composable
fun HeaderSection(viewModel: HarinaamViewModel, syncing: Boolean, isInSelectionMode: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Harinaam Registrations",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { if (!syncing) { if (isInSelectionMode) viewModel.syncRegistrations() else viewModel.syncUnsyncedRegistrations() } }) {
            if (syncing) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Icon(painter = painterResource(R.drawable.baseline_sync_24), contentDescription = "Sync")
        }
    }
}

@Composable
fun SelectionHeader(uiState: HarinaamUiState, viewModel: HarinaamViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${uiState.selectedRegistrations.size} Selected", color = MaterialTheme.colorScheme.primary)
        Row {
            TextButton(onClick = { viewModel.clearSelections() }) { Text("Cancel") }
            TextButton(onClick = { viewModel.onClickDelete() }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun HarinaamListView(registrations: List<SangkirtanRegistrationStatus>, uiState: HarinaamUiState, viewModel: HarinaamViewModel, navController: NavController) {
    if (registrations.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No registrations available")
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(registrations) { reg ->
                HarinaamItem(
                    data = reg,
                    isSelected = uiState.selectedRegistrations.contains(reg.date),
                    onLongClick = {
                        if (!uiState.isInSelectionMode) {
                            viewModel.toggleSelectionMode()
                            viewModel.toggleRegistrationSelection(reg.date)
                        }
                    },
                    onClick = {
                        if (uiState.isInSelectionMode) {
                            viewModel.toggleRegistrationSelection(reg.date)
                        }
                        else{
                            navController.navigate("harinaam_details/${reg.date}")
                        }
                    }

                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HarinaamItem(
    data: SangkirtanRegistrationStatus,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val formattedDate = try { LocalDate.parse(data.date).format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")) } catch(e: Exception) { data.date }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

            Column {
                Text(
                    text = "Date: $formattedDate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Registrations: ${data.counts}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (data.synced) Color.Green else Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(if (data.synced) R.drawable.baseline_done_outline_24 else R.drawable.baseline_sync_24),
                    contentDescription = if (data.synced) "Synced" else "Not Synced",
                    modifier = Modifier.size(10.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(uiState: HarinaamUiState, viewModel: HarinaamViewModel) {
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDelete() },
            title = { Text("Delete Selected") },
            text = { Text("Are you sure you want to delete these registrations?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSelectedRegistrations() }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDelete() }) { Text("Cancel") }
            }
        )
    }
}
