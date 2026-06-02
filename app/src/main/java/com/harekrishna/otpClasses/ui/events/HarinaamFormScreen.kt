package com.harekrishna.otpClasses.ui.events

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.harekrishna.otpClasses.data.sources.repos.MessageType
import com.harekrishna.otpClasses.sendWhatsappMesssage
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HarinaamFormScreen(
    navController: NavController,
    viewModel: HarinaamFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val categories = listOf("Youth", "Congregation")

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp, color = MaterialTheme.colorScheme.surface) {
                Box(modifier = Modifier.fillMaxWidth().padding( start = 16.dp, top = 32.dp, bottom = 16.dp, end = 16.dp)) {
                    Text("Harinaam Registration", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))))
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AnimatedTextField(
                value = uiState.name, onValueChange = viewModel::onNameChange,
                label = "Full Name", leadingIcon = Icons.Rounded.Person,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )

            PhoneInputSection(
                phone = uiState.phone,
                onPhoneChange = viewModel::onPhoneChange,
                onInviteClick = { phone ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (phone.length == 10 && phone.all { it.isDigit() }) {
                        scope.launch {
                            val message = viewModel.getWhatsAppMessage(phone, MessageType.WELCOME)
                            context.sendWhatsappMesssage(phone, message)
                        }
                        viewModel.onInvited()
                    } else {
                        Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                    }
                },
                haptic = haptic,
                isInvited = uiState.isInvited
            )

            AnimatedDropdownField(
                label = "Category", selectedValue = uiState.category,
                expanded = uiState.showDropdown, onToggleExpanded = viewModel::toggleDropdown,
                onDismissRequest = viewModel::toggleDropdown, onItemSelect = viewModel::onCategoryChange,
                options = categories, leadingIcon = Icons.Outlined.Groups
            )

            AnimatedTextField(
                value = uiState.location, onValueChange = viewModel::onLocationChange,
                label = "Location", leadingIcon = Icons.Outlined.LocationOn,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
            )

            AnimatedSubmitButton(
                isSubmitting = uiState.isSubmitting,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val phone = uiState.phone
                    
                    if (!uiState.isSubmitting) {
                        if (!(phone.length == 10 && phone.all { it.isDigit() })) {
                            Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                            return@AnimatedSubmitButton
                        }

                        val isValidName = uiState.name.matches(Regex("^[a-zA-Z ]+$"))
                        if (uiState.name.isBlank() || !isValidName) {
                            Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                            return@AnimatedSubmitButton
                        }

                        viewModel.submitRegistration { navController.popBackStack() }
                    }
                }
            )
        }
    }

    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissSuccessDialog,
            title = { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text("Success!") } },
            text = { Text("Registered successfully!") },
            confirmButton = { TextButton(onClick = { viewModel.onDismissSuccessDialog(); navController.popBackStack() }) { Text("OK") } }
        )
    }
}

@Composable
private fun AnimatedTextField(value: String, onValueChange: (String) -> Unit, label: String, leadingIcon: ImageVector, keyboardOptions: androidx.compose.foundation.text.KeyboardOptions) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, null, tint = MaterialTheme.colorScheme.primary) },
        keyboardOptions = keyboardOptions, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PhoneInputSection(phone: String, onPhoneChange: (String) -> Unit, onInviteClick: (String) -> Unit, haptic: androidx.compose.ui.hapticfeedback.HapticFeedback, isInvited: Boolean) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = phone, onValueChange = onPhoneChange, label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Rounded.Phone, null, tint = MaterialTheme.colorScheme.primary) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onInviteClick(phone) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(if (isInvited) Icons.Default.Check else Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (isInvited) "Invited" else "Invite", color = if (isInvited) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AnimatedDropdownField(label: String, selectedValue: String, expanded: Boolean, onToggleExpanded: () -> Unit, onDismissRequest: () -> Unit, onItemSelect: (String) -> Unit, options: List<String>, leadingIcon: ImageVector) {
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue, onValueChange = {}, label = { Text(label) }, readOnly = true,
            leadingIcon = { Icon(leadingIcon, null, tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = { IconButton(onClick = onToggleExpanded) { Icon(Icons.Default.ArrowDropDown, null) } },
            shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().onGloballyPositioned { textFieldSize = it.size.toSize() }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onItemSelect(option) }) }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedSubmitButton(isSubmitting: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = !isSubmitting,
        modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
        else Text("Submit Registration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
