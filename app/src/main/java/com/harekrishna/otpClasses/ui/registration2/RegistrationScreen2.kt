package com.harekrishna.otpClasses.ui.registration2

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harekrishna.otpClasses.MyApplication.Companion.toCamelCase
import com.harekrishna.otpClasses.domain.model.InterestLevel
import com.harekrishna.otpClasses.domain.model.StudentCategory
import com.harekrishna.otpClasses.sendWhatsappMesssage
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import com.harekrishna.otpClasses.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun RegistrationFormScreen(
    modifier: Modifier = Modifier,
    viewModel: RegistrationScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 1. Get LocalContext HERE (in composable scope)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    // 2. Use the 'context' variable inside LaunchedEffect
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is RegistrationUiEvent.LaunchWhatsApp -> {
                    context.sendWhatsappMesssage(
                         event.phoneNumber,
                         event.message
                    )
                }
            }
        }
    }

    RegistrationScreenContent(
        modifier = modifier,
        uiState = uiState,
        onPhotoChange = viewModel::onPhotoChange,
        onPhoneChange = viewModel::onPhoneChange,
        onNameChange = viewModel::onNameChange,
        onAddressChange = viewModel::onAddressChange,
        onEducationChange = viewModel::onEducationChange,
        onOccupationChange = viewModel::onOccupationChange,
        onCategoryChange = viewModel::onCategoryChange,
        onInterestLevelChange = viewModel::onInterestLevelChange,
        onSubmit = viewModel::onRegisterClick
    )

    // Handle Sending Invite Dialog State
    if (uiState.dialogState == RegDialogState.SENDING_INVITE) {
        SendingInviteDialog(
            phone = uiState.phone,
            isSubmitting = uiState.isSubmitting,
            onVerifyAndSubmit = viewModel::onVerifyAndSubmit,
            onNotVerifyAndSubmit = viewModel::onNotVerifyAndSubmit,
            onCancel = viewModel::onCancelSendingDialog
        )
    }

    // Handle Registration Success Dialog State
    val registeredStudentID = uiState.registeredUserID
    if (uiState.dialogState == RegDialogState.SUCCESS && registeredStudentID != null) {
        RegistrationSuccessDialog(
            studentID = registeredStudentID,
            name = uiState.name,
            phone = uiState.phone,
            isRejoin = uiState.isExistingStudent,
            onRegisterAnother = viewModel::onRegisterAnother
        )
    }
}

@Composable
private fun RegistrationScreenContent(
    modifier: Modifier = Modifier,
    uiState: RegistrationUiState,
    onPhotoChange: (Uri?) -> Unit,
    onPhoneChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onEducationChange: (String) -> Unit,
    onOccupationChange: (String) -> Unit,
    onCategoryChange: (StudentCategory) -> Unit,
    onInterestLevelChange: (InterestLevel) -> Unit,
    onSubmit: () -> Unit
) {
    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                uiState.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = onSubmit,
                    enabled = uiState.phoneCheckStatus != PhoneCheckStatus.CHECKING && !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    val label = if (uiState.isExistingStudent) "Update Student Details" else "Register Student"
                    Text(label, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "🙏 Hari Bol",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            item {
                PhotoPicker(photoUri = uiState.photoUri, onPhotoChange = onPhotoChange)
            }

            item {
                LabeledField("Phone Number") {
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = onPhoneChange,
                        placeholder = { Text("9876543210") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                PhoneStatusBanner(status = uiState.phoneCheckStatus)
            }

            item {
                LabeledField("Full Name") {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = onNameChange,
                        placeholder = { Text("e.g. Rahul Verma") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                LabeledField("Address") {
                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = onAddressChange,
                        placeholder = { Text(" Street / Area / City") },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        LabeledField("Education") {
                            OutlinedTextField(
                                value = uiState.education,
                                onValueChange = onEducationChange,
                                placeholder = { Text("B.Tech CSE") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        LabeledField("Occupation") {
                            OutlinedTextField(
                                value = uiState.occupation,
                                onValueChange = onOccupationChange,
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                LabeledField("Category") {
                    SegmentedRow(
                        options = StudentCategory.entries.map { it.name },
                        selectedIndex = uiState.category.ordinal,
                        onSelected = { onCategoryChange(StudentCategory.entries[it]) }
                    )
                }
            }

            item {
                LabeledField("Interest Level") {
                    SegmentedRow(
                        options = InterestLevel.entries.map { it.name },
                        selectedIndex = uiState.interestLevel.ordinal,
                        onSelected = { onInterestLevelChange(InterestLevel.entries[it]) }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun PhoneStatusBanner(status: PhoneCheckStatus, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = status != PhoneCheckStatus.IDLE, modifier = modifier) {
        val containerColor: Color
        val contentColor: Color
        val label: String

        when (status) {
            PhoneCheckStatus.CHECKING -> {
                containerColor = MaterialTheme.colorScheme.surfaceVariant
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                label = "Checking phone number…"
            }
            PhoneCheckStatus.EXISTING -> {
                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
                contentColor = MaterialTheme.colorScheme.tertiary
                label = "Already Registered — details auto-filled"
            }
            PhoneCheckStatus.NEW -> {
                containerColor = MaterialTheme.colorScheme.secondaryContainer
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                label = "New Student Registration"
            }
            PhoneCheckStatus.IDLE -> {
                containerColor = Color.Transparent
                contentColor = Color.Transparent
                label = ""
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (status) {
                PhoneCheckStatus.CHECKING -> CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = contentColor
                )
                PhoneCheckStatus.EXISTING -> Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                PhoneCheckStatus.NEW -> Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                PhoneCheckStatus.IDLE -> {}
            }
            Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
private fun SendingInviteDialog(
    phone: String,
    isSubmitting: Boolean,
    onVerifyAndSubmit: () -> Unit,
    onNotVerifyAndSubmit: () -> Unit,
    onCancel: () -> Unit
) {

    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 1400))
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onCancel() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Sending Invite Message",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Sending a WhatsApp invite to ${phone.ifBlank { "the student" }}…",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(100)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(22.dp))

                // Primary Action: Verified & Submit
                Button(
                    onClick = onVerifyAndSubmit,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = if (isSubmitting) "Submitting…" else "Verified & Submit",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Secondary Action: Submit Without WhatsApp Verification
                OutlinedButton(
                    onClick = onNotVerifyAndSubmit,
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Submit Anyway",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Cancel / Dismiss Action
                TextButton(
                    onClick = onCancel,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrationSuccessDialog(
    studentID: String,
    name: String,
    phone: String,
    isRejoin: Boolean = false,
    onRegisterAnother: () -> Unit
) {
    Dialog(onDismissRequest = { }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    if (isRejoin) "Re-join Successful" else "Registration Successful",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isRejoin)
                        "The student profile has been marked active and their details were updated."
                    else
                        "The student profile has been created and data has been synced successfully.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    IdRow("STUDENT ID", studentID)
                    IdRow("NAME", name.toCamelCase())
                    IdRow("PHONE", phone, showDivider = false)
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onRegisterAnother,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Register Another", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PhotoPicker(photoUri: Uri?, onPhotoChange: (Uri?) -> Unit) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) onPhotoChange(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) onPhotoChange(pendingCameraUri) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { galleryLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            val bitmap = rememberPhotoBitmap(photoUri)
            Crossfade(targetState = bitmap, label = "photo-crossfade") { bmp ->
                if (bmp != null) {
                    Image(
                        bitmap = bmp,
                        contentDescription = "Student photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, shape = RoundedCornerShape(100)) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Gallery")
            }
            OutlinedButton(
                onClick = {
                    val uri = createImageCaptureUri(context)
                    pendingCameraUri = uri
                    cameraLauncher.launch(uri)
                },
                shape = RoundedCornerShape(100)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Camera")
            }
        }
        AnimatedVisibility(visible = photoUri != null) {
            Text(
                "Remove photo",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clickable { onPhotoChange(null) }
            )
        }
    }
}

@Composable
private fun rememberPhotoBitmap(uri: Uri?): ImageBitmap? {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri) {
        bitmap = if (uri == null) null else withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    return bitmap
}

private fun createImageCaptureUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "student_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label.uppercase(),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )
        content()
    }
}

@Composable
private fun SegmentedRow(options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onSelected(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IdRow(label: String, value: String, showDivider: Boolean = true) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
        }
        if (showDivider) Spacer(Modifier.height(10.dp))
    }
}

private fun openWhatsAppChat(context: Context, phone: String, message: String) {
    val digitsOnly = phone.filter { it.isDigit() }
    val url = "https://wa.me/$digitsOnly?text=${Uri.encode(message)}"
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}


/* ============================================================================================
 *  PREVIEWS — drop this file in and these render immediately, no setup needed.
 * ============================================================================================ */

private val previewEmptyState = RegistrationUiState()

private val previewFilledState = RegistrationUiState(
    name = "Rahul Verma",
    phone = "+91 99887 76655",
    address = "123, Gomti Nagar, Lucknow",
    education = "B.Tech CSE",
    occupation = "Student",
    category = StudentCategory.YOUTH,
    interestLevel = InterestLevel.HIGH
)

private val previewExistingStudentState = previewFilledState.copy(
    phoneCheckStatus = PhoneCheckStatus.EXISTING,
    existingStudentId = "#HB-2025-042"
)

private val previewNewStudentState = RegistrationUiState(
    phone = "+91 91234 56789",
    phoneCheckStatus = PhoneCheckStatus.NEW
)

@Preview(showBackground = true, name = "Registration — Empty (Light)")
@Composable
private fun RegistrationScreenPreviewEmpty() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        RegistrationScreenContent(
            uiState = previewEmptyState,
            onPhotoChange = {},
            onNameChange = {},
            onPhoneChange = {},
            onAddressChange = {},
            onEducationChange = {},
            onOccupationChange = {},
            onCategoryChange = {},
            onInterestLevelChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Registration — Existing Student (Light)")
@Composable
private fun RegistrationScreenPreviewExisting() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        RegistrationScreenContent(
            uiState = previewExistingStudentState,
            onPhotoChange = {},
            onNameChange = {},
            onPhoneChange = {},
            onAddressChange = {},
            onEducationChange = {},
            onOccupationChange = {},
            onCategoryChange = {},
            onInterestLevelChange = {},
            onSubmit = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Registration — New Student (Dark)",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RegistrationScreenPreviewNew() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        RegistrationScreenContent(
            uiState = previewNewStudentState,
            onPhotoChange = {},
            onNameChange = {},
            onPhoneChange = {},
            onAddressChange = {},
            onEducationChange = {},
            onOccupationChange = {},
            onCategoryChange = {},
            onInterestLevelChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true, name = "Dialog — Sending Invite (Light)")
@Composable
private fun SendingInviteDialogPreviewLight() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            SendingInviteDialog(
                phone = "+91 98765 43210",
                isSubmitting = false,
                onVerifyAndSubmit = {},
                onNotVerifyAndSubmit = {},
                onCancel = {}
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "Dialog — Success (Dark)",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SuccessDialogPreviewDark() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            RegistrationSuccessDialog(studentID = "#HB-2026-089", name = "rahul", phone = "9988776655", onRegisterAnother = {})
        }
    }
}