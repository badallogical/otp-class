package com.harekrishna.otpClasses.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.harekrishna.otpClasses.data.sources.repos.MessageType
import com.harekrishna.otpClasses.sendWhatsappMesssage
import com.harekrishna.otpClasses.ui.registeration.StudentFormUiState
import com.harekrishna.otpClasses.ui.registeration.StudentFormViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StudentFormScreen(
    editId: String? = null,
    viewModel: StudentFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var isEligible by remember { mutableStateOf(false) }

    val facilitators = listOf(
        "NA",
        "H.G Sadhu Chaitanya Prabhu",
        "H.G Seva Actyute Prabhu",
        "H.G Rajiv Lochan Prabhu",
        "H.G Madhu Smita Prabhu",
        "Prakhar Bhatnagar Prabhu",
        "Saurabh Agarwal Prabhu"
    )
    val batches = listOf("OTP", "DYS", "TSSV", "VL2")
    val cities = listOf("Lucknow", "Outside Lucknow")

    // Camera and Image Picker Launchers
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d("CapturePhotoScreen", "Photo saved to: ${viewModel.tempPhotoUri}")
            viewModel.tempPhotoUri?.let { viewModel.onPhotoSelected(it) }
            Toast.makeText(context, "Photo captured successfully", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("CapturePhotoScreen", "Failed to capture photo")
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.createImageUri(context)?.let { uri ->
                viewModel.tempPhotoUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    fun launchCamera(viewModel: StudentFormViewModel) {
        val cameraPermissionGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            viewModel.createImageUri(context)?.let { uri ->
                viewModel.tempPhotoUri = uri
                takePictureLauncher.launch(uri)
            }
        }
    }

    // Fetch existing data if editId is not null
    LaunchedEffect(editId) {
        if (editId != null) {
            viewModel.onFetchStudentByPhone(editId)
        }
    }

    Scaffold(
        topBar = {
            TopSection { viewModel.onEditRegistration() }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(start = 20.dp,end = 20.dp, bottom = 16.dp, top = 0.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Profile Photo Section with Animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(800, easing = FastOutSlowInEasing)
                    )

                ) {
                    ProfilePhotoSection(
                        uiState = uiState,
                        imagePickerLauncher = imagePickerLauncher,
                        launchCamera = { launchCamera(viewModel) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form Fields Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Name Field
                        AnimatedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChange(it) },
                            label = "Full Name",
                            leadingIcon = Icons.Rounded.Person,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )

                        // Phone Field with Invite
                        PhoneInputSection(
                            uiState = uiState,
                            onPhoneChange = { viewModel.onPhoneChange(it) },
                            onInviteClick = { phone ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (phone.length == 10 && phone.all { it.isDigit() }) {
                                    scope.launch{
                                        val message = viewModel.getWhatsAppMessage(phone,
                                            MessageType.WELCOME)
                                        context.sendWhatsappMesssage(phone, message)
                                    }
                                    viewModel.onInvited()
                                } else {
                                    Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                                }
                            },
                            haptic = haptic
                        )

                        // Facilitator Dropdown
                        AnimatedDropdownField(
                            label = "Facilitator",
                            selectedValue = uiState.facilitator,
                            expanded = uiState.showDropdownFacilitator,
                            onToggleExpanded = { viewModel.onDropDownFacilitator() },
                            onDismissRequest = { viewModel.onDismissFacilitator() },
                            onItemSelect = { viewModel.onFacilitatorChange(it) },
                            options = facilitators,
                            leadingIcon = Icons.Outlined.Groups
                        )

                        // Batch Dropdown
                        AnimatedDropdownField(
                            label = "Batch",
                            selectedValue = uiState.batch,
                            expanded = uiState.showDropdownBatch,
                            onToggleExpanded = { viewModel.onDropDownBatch() },
                            onDismissRequest = { viewModel.onDismissBatch() },
                            onItemSelect = { viewModel.onBatchChange(it) },
                            options = batches,
                            leadingIcon = Icons.Outlined.School
                        )

                        // Profession Field
                        AnimatedTextField(
                            value = uiState.profession,
                            onValueChange = { viewModel.onProfessionChange(it) },
                            label = "Profession",
                            leadingIcon = Icons.Outlined.Work,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )

                        // Address Field
                        AnimatedTextField(
                            value = uiState.address,
                            onValueChange = { viewModel.onAddressChange(it) },
                            label = "Address",
                            leadingIcon = Icons.Rounded.LocationOn,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                            minLines = 1,
                            maxLines = 3
                        )

                        // City Dropdown
                        AnimatedDropdownField(
                            label = "City",
                            selectedValue = uiState.city,
                            expanded = uiState.showDropdownCity,
                            onToggleExpanded = { viewModel.onDropDownCity() },
                            onDismissRequest = { viewModel.onDismissCity() },
                            onItemSelect = { viewModel.onCityChange(it) },
                            options = cities,
                            leadingIcon = Icons.Outlined.LocationCity
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.Top, // Changed to Top for multi-line text
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { isEligible = !isEligible } // Makes the whole row toggle the checkbox
                ) {
                    Checkbox(
                        checked = isEligible,
                        onCheckedChange = { isEligible = it },
                        modifier = Modifier.padding(top = 2.dp) // Aligns checkbox with the first line of text
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "I confirm that this individual is eligible to register and has completed Class 12 or higher.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                }

                Spacer(modifier = Modifier.height(24.dp))

                // Enhanced Submit Button
                AnimatedSubmitButton(
                    isSubmitting = uiState.isSubmitting,
                    isEdit = editId != null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val phone = uiState.phone
                        if (!uiState.isSubmitting) {

                            if (!isEligible) {
                                Toast.makeText(
                                    context,
                                    "Only students above Class 12 (graduates) can apply",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@AnimatedSubmitButton
                            }

                            if (!(phone.length == 10 && phone.all { it.isDigit() })) {
                                Toast.makeText(context, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                                return@AnimatedSubmitButton
                            }

                            val isValid = uiState.name.matches(Regex("^[a-zA-Z ]+$"))
                            if (uiState.name.isBlank() || !isValid  ) {
                                Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show()
                                return@AnimatedSubmitButton
                            }

                            if (editId == null) viewModel.onSubmit() else viewModel.onUpdate()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(64.dp))
            }

            // Dialogs
            SuccessDialog(uiState, viewModel)
            PhoneDialog(uiState, viewModel)
            StudentNotFoundDialog(uiState, viewModel)

            // Data Fetched Snackbar
            DataFetchedSnackbar(uiState, viewModel)
        }
    }
}

@Composable
private fun TopSection(onEditRegistration: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "🙏 Hari Bol",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun ProfilePhotoSection(
    uiState: StudentFormUiState,
    imagePickerLauncher: ActivityResultLauncher<String>,
    launchCamera: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (uiState.photoUri != null) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(scale)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    if (uiState.photoUri != null) Color.Transparent
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    3.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.photoUri != null) {
                AsyncImage(
                    model = uiState.photoUri,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add Photo",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PhotoActionButton(
                icon = Icons.Outlined.PhotoLibrary,
                label = "Gallery",
                onClick = { imagePickerLauncher.launch("image/*") }
            )
            PhotoActionButton(
                icon = Icons.Outlined.PhotoCamera,
                label = "Camera",
                onClick = launchCamera
            )
        }
    }
}

@Composable
private fun PhotoActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(16.dp), // smoother radius
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent, // no fill, clean look
            contentColor = MaterialTheme.colorScheme.primary // consistent text & icon color
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = Modifier.defaultMinSize(minHeight = 48.dp) // consistent height
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
private fun AnimatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardOptions: KeyboardOptions,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth(),
        minLines = minLines,
        maxLines = maxLines
    )
}

@Composable
private fun PhoneInputSection(
    uiState: StudentFormUiState,
    onPhoneChange: (String) -> Unit,
    onInviteClick: (String) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    Box(modifier = Modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = uiState.phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            leadingIcon = {
                Icon(
                    Icons.Rounded.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = if (uiState.isInvited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Invite icon + text
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd) // 👈 aligned to the right, centered vertically
                .padding(end = 16.dp)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onInviteClick(uiState.phone)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (uiState.isInvited) Icons.Default.Check else Icons.Default.Send,
                contentDescription = "Invite",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (uiState.isInvited) "Invited" else "Invite",
                color = if (uiState.isInvited) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}


@Composable
private fun AnimatedDropdownField(
    label: String,
    selectedValue: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDismissRequest: () -> Unit,
    onItemSelect: (String) -> Unit,
    options: List<String>,
    leadingIcon: ImageVector
) {
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300)
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = { },
            label = { Text(label) },
            readOnly = true,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle Dropdown",
                        modifier = Modifier.rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(12.dp)
                )
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onItemSelect(option)
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedSubmitButton(
    isSubmitting: Boolean,
    isEdit: Boolean,
    onClick: () -> Unit
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isSubmitting) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Button(
        onClick = onClick,
        enabled = !isSubmitting,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(buttonScale)
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        AnimatedContent(
            targetState = isSubmitting,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
            }
        ) { submitting ->
            if (submitting) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Processing...",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Text(
                    text = if (isEdit) "Update Student" else "Submit",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

// Enhanced Dialog Components
@Composable
fun SuccessDialog(uiState: StudentFormUiState, viewModel: StudentFormViewModel) {
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissSuccessDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Success!", style = MaterialTheme.typography.headlineSmall)
                }
            },
            text = { Text("Student registered successfully!", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismissSuccessDialog() }) {
                    Text("OK", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun PhoneDialog(uiState: StudentFormUiState, viewModel: StudentFormViewModel) {
    if (uiState.showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissPhoneDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invalid Phone", style = MaterialTheme.typography.headlineSmall)
                }
            },
            text = { Text("Please enter a valid 10-digit phone number", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismissPhoneDialog() }) {
                    Text("OK", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun StudentNotFoundDialog(uiState: StudentFormUiState, viewModel: StudentFormViewModel) {
    if (uiState.showStudentNotFoundDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onStudentNotFound() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Student Not Found", style = MaterialTheme.typography.headlineSmall)
                }
            },
            text = { Text("No student found with this phone number.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { viewModel.onStudentNotFound() }) {
                    Text("OK", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun DataFetchedSnackbar(uiState: StudentFormUiState, viewModel: StudentFormViewModel) {
    if (uiState.showDataFetchedToast) {
        Snackbar(
            action = {
                TextButton(onClick = { viewModel.onDataFetched() }) {
                    Text("Dismiss", fontWeight = FontWeight.Medium)
                }
            },
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Data fetched successfully!")
        }
    }
}

@Preview()
@Composable
fun previewScreenRegistration(){
//    StudentFormScreen()
    PhotoActionButton(
        icon = Icons.Outlined.PhotoCamera,
        label = "Camera",
        onClick = { } 
    )
}
