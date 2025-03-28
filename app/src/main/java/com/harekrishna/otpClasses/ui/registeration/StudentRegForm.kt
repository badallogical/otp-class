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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.harekrishna.otpClasses.ui.registeration.CallingListViewModel
import com.harekrishna.otpClasses.ui.registeration.StudentFormUiState
import com.harekrishna.otpClasses.ui.registeration.StudentFormViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StudentFormScreen(
    editId: String? = null,
    viewModel: StudentFormViewModel = viewModel(factory = StudentFormViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val facilitators = listOf(
        "NA", "H.G Sadhu Chaitanya Prabhu", "H.G Seva Actyute Prabhu",
        "H.G Rajiv Lochan Prabhu", "H.G Madhu Smita Prabhu",
        "Prakhar Bhatnagar Prabhu", "Saurabh Agarwal Prabhu"
    )
    val batches = listOf("OTP", "DYS", "TSSV", "VL2")

    val icon1 = if (uiState.showDropdownFacilitator) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    val icon2 = if (uiState.showDropdownBatch) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    val backgroundColor = MaterialTheme.colorScheme.background

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
            viewModel.onFetchStudentByPhone(editId) // ✅ Fetch and update UI state
        }
    }


    Scaffold(topBar = { TopSection { viewModel.onEditRegistration() } }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp )
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Photo selection section
            ProfilePhotoSection(
                uiState = uiState,
                imagePickerLauncher = imagePickerLauncher,
                launchCamera = { launchCamera(viewModel) }
            )

            // Form Fields and Submit
            // Name input
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Name") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Person,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(backgroundColor, shape = MaterialTheme.shapes.small)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                // Phone input field
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { viewModel.onPhoneChange(it) },
                    label = { Text("Phone") },
                    textStyle = TextStyle(
                        color = if (uiState.isInvited) MaterialTheme.colorScheme.primary else Color.Black // Set the text color based on isInvited
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Phone,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, shape = MaterialTheme.shapes.small)
                )

                // Clickable text
                Text(
                    text = "\uD83E\uDD1D Invite",
                    color = MaterialTheme.colorScheme.primary, // Or any color you want for the text
                    modifier = Modifier
                        .align(Alignment.CenterEnd) // Align to the right
                        .padding(16.dp) // Adjust padding for aesthetics
                        .clickable {
                            val phone = uiState.phone
                            if ((phone.length == 10 && phone.all { it.isDigit() })) {

                                viewModel.sendWhatsAppMessage(
                                    context,
                                    uiState.phone,
                                    uiState.name
                                )
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter a valid phone number",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                )
            }


            // Facilitator dropdown
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

                OutlinedTextField(
                    value = uiState.facilitator,
                    onValueChange = { /* No-op */ },
                    label = { Text("Facilitator") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, shape = MaterialTheme.shapes.small)
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        Icon(icon1, "contentDescription",
                            Modifier.clickable { viewModel.onDropDownFacilitator() })
                    }
                )

                DropdownMenu(
                    expanded = uiState.showDropdownFacilitator,
                    onDismissRequest = { viewModel.onDismissFacilitator() },
                    modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                ) {
                    facilitators.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                viewModel.onFacilitatorChange(option)
                            }
                        )
                    }
                }

            }

            // Batch dropdown
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

                OutlinedTextField(
                    value = uiState.batch,
                    onValueChange = { /* No-op */ },
                    label = { Text("Batch") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor, shape = MaterialTheme.shapes.small)
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        Icon(icon2, "contentDescription",
                            Modifier.clickable { viewModel.onDropDownBatch() })
                    }
                )
                DropdownMenu(
                    expanded = uiState.showDropdownBatch,
                    onDismissRequest = { viewModel.onDropDownBatch() },
                    modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                ) {
                    batches.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                viewModel.onBatchChange(option)
                            }
                        )
                    }
                }
            }

            // Profession input
            OutlinedTextField(
                value = uiState.profession,
                onValueChange = { viewModel.onProfessionChange(it) },
                label = { Text("Profession") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(backgroundColor, shape = MaterialTheme.shapes.small)
            )

            // Address input
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = { Text("Address") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                leadingIcon = {
                    Icon(
                        Icons.Rounded.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(backgroundColor, shape = MaterialTheme.shapes.small)
            )

            // Submit button
            Button(
                onClick = {
                    // viewModel.sendWhatsAppMessage(context,uiState.phone,uiState.name)
                    val phone = uiState.phone
                    if (!uiState.isSubmitting) {
                        if (!(phone.length == 10 && phone.all { it.isDigit() })) {
                            Toast.makeText(
                                context,
                                "Please enter a valid phone number",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if(editId == null )
                            viewModel.onSubmit()
                        else
                            viewModel.onUpdate()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    if( editId == null )
                        Text("Submit")
                    else
                        Text("Update")
                }
            }

            // Spacer at Bottom
            Spacer(modifier = Modifier.height(16.dp))


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
private fun TopSection(
    onEditRegistration : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hari Bol",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(3f),
            textAlign = TextAlign.Start
        )
    }
}


@Composable
fun SubmitButton(
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Button(
        onClick = onSubmit,
        enabled = !isSubmitting, // Disable the button while submitting
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("Submit")
        }
    }
}


@Composable
fun SuccessDialog(uiState: StudentFormUiState, viewModel: StudentFormViewModel) {
    if (uiState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissSuccessDialog() },
            title = { Text("Success") },
            text = { Text("Student registered successfully!") },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismissSuccessDialog() }) {
                    Text("OK")
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun PhoneDialog(uiState: StudentFormUiState, viewModel: StudentFormViewModel) {
    if (uiState.showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissPhoneDialog() },
            title = { Text("Invalid Phone Number") },
            text = { Text("Please enter a valid phone number") },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismissPhoneDialog() }) {
                    Text("OK")
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun StudentNotFoundDialog(uiState: StudentFormUiState, viewModel: StudentFormViewModel) {
    if (uiState.showStudentNotFoundDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onStudentNotFound() },
            title = { Text("Student Not Found") },
            text = { Text("No student found with this phone number.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onStudentNotFound() }) {
                    Text("OK")
                }
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun DataFetchedSnackbar(uiState: StudentFormUiState, viewModel: StudentFormViewModel) {

    if (uiState.showDataFetchedToast) {
        Snackbar(
            action = {
                TextButton(onClick = { viewModel.onDataFetched() }) {
                    Text("Undo")
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Data fetched successfully!")
        }
    }
}




@Composable
private fun ProfilePhotoSection(
    uiState: StudentFormUiState,
    imagePickerLauncher: ActivityResultLauncher<String>,
    launchCamera: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
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
                        imageVector = Icons.Default.Person,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // imagePickerLauncher.launch("image/*")
                TextButton(onClick = {  }) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = "Gallery")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gallery")
                }
                TextButton(onClick = launchCamera) {
                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Camera")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Camera")
                }
            }
        }
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { if (leadingIcon != null) Icon(leadingIcon, null) },
        keyboardOptions = keyboardOptions,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun PhoneInputSection(
    uiState: StudentFormUiState,
    onPhoneChange: (String) -> Unit,
    onInviteClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        InputField(
            value = uiState.phone,
            onValueChange = onPhoneChange,
            label = "Phone",
            leadingIcon = Icons.Rounded.Phone,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next)
        )
        Text(
            text = "\uD83E\uDD1D Invite",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
                .clickable { onInviteClick(uiState.phone) }
        )
    }
}

@Composable
private fun DropdownSection(
    label: String,
    selectedValue: String,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onItemSelect: (String) -> Unit,
    options: List<String>,
    icon: ImageVector
) {
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    OutlinedTextField(
        value = selectedValue,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates -> textFieldSize = coordinates.size.toSize() },
        trailingIcon = {
            IconButton(onClick = { onDismissRequest() }) {
                Icon(imageVector = icon, contentDescription = null)
            }
        }
    )
}
