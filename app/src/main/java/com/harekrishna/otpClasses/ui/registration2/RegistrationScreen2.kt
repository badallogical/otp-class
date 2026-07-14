package com.harekrishna.otpClasses.ui.registeration2

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.harekrishna.otpClasses.ui.theme.Otp_class_appTheme
import com.harekrishna.otpClasses.ui.theme.ThemeMode
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* ============================================================================================
 *  MODELS — dummy/local only, no Firestore or backend types here.
 * ============================================================================================ */

enum class RegCategory(val label: String) { YOUTH("Youth"), CONGREGATION("Congregation") }

enum class InterestLevel(val label: String) { LOW("Low"), MEDIUM("Medium"), HIGH("High") }

enum class PhoneVerification { NOT_SENT, SENDING, SENT }

enum class RegFlowScreen { REGISTRATION, SUCCESS }

data class RegisteredStudent(
    val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val education: String,
    val occupation: String,
    val category: RegCategory,
    val interestLevel: InterestLevel,
    val batchName: String
)

data class PosterTemplate(
    val id: String,
    val title: String,
    val emoji: String,
    val body: String // may contain {name} / {batch} placeholders
) {
    fun render(name: String, batch: String) = body.replace("{name}", name).replace("{batch}", batch)
}

/* ============================================================================================
 *  UI STATE
 * ============================================================================================ */

data class RegistrationUiState(
    val currentBatchName: String = "Sunday Morning Batch A", // auto-assigned, read-only
    val photoUri: Uri? = null,
    val name: String = "",
    val phone: String = "",
    val phoneVerification: PhoneVerification = PhoneVerification.NOT_SENT,
    val address: String = "",
    val education: String = "",
    val occupation: String = "student",
    val category: RegCategory = RegCategory.YOUTH,
    val interestLevel: InterestLevel = InterestLevel.MEDIUM,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean get() = name.isNotBlank() && phone.isNotBlank() && address.isNotBlank()
}

data class SuccessUiState(
    val student: RegisteredStudent? = null,
    val templates: List<PosterTemplate> = emptyList(),
    val selectedTemplateId: String? = null,
    val renderedMessage: String = "",
    val justCopied: Boolean = false
)

data class RegistrationFlowUiState(
    val screen: RegFlowScreen = RegFlowScreen.REGISTRATION,
    val registration: RegistrationUiState = RegistrationUiState(),
    val success: SuccessUiState = SuccessUiState()
)

/* ============================================================================================
 *  VIEWMODEL — dummy data only, simulates network delay for the invite + submit so the UI feels
 *  real; nothing here talks to Firestore or any backend.
 * ============================================================================================ */

class RegistrationFlowViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegistrationFlowUiState())
    val uiState: StateFlow<RegistrationFlowUiState> = _uiState.asStateFlow()

    private val templates = listOf(
        PosterTemplate("welcome", "Welcome to OTP", "\uD83E\uDE94", "Hare Krishna {name} \uD83D\uDE4F Welcome to OTP — {batch}! We're so glad to have you with us. See you this Sunday, 10 AM."),
        PosterTemplate("reminder", "Sunday Reminder", "\uD83D\uDD49", "Hare Krishna {name}! A gentle reminder — our OTP class begins 10 AM this Sunday. Looking forward to seeing you."),
        PosterTemplate("festival", "Janmashtami Invite", "\uD83E\uDD6F", "Hare Krishna {name}! Join us this Janmashtami for kirtan, prasadam and celebration. Bring your family along."),
        PosterTemplate("reengage", "We Miss You", "\uD83C\uDF31", "Hare Krishna {name}, it's been a while! We'd love to see you back at {batch} this Sunday.")
    )

    fun onNameChange(v: String) = updateReg { it.copy(name = v, errorMessage = null) }
    fun onPhotoChange(uri: Uri?) = updateReg { it.copy(photoUri = uri) }
    fun onPhoneChange(v: String) = updateReg { it.copy(phone = v, phoneVerification = PhoneVerification.NOT_SENT, errorMessage = null) }
    fun onAddressChange(v: String) = updateReg { it.copy(address = v, errorMessage = null) }
    fun onEducationChange(v: String) = updateReg { it.copy(education = v) }
    fun onOccupationChange(v: String) = updateReg { it.copy(occupation = v) }
    fun onCategoryChange(v: RegCategory) = updateReg { it.copy(category = v) }
    fun onInterestLevelChange(v: InterestLevel) = updateReg { it.copy(interestLevel = v) }

    fun onSendInvite() {
        val phone = _uiState.value.registration.phone
        if (phone.isBlank()) return
        updateReg { it.copy(phoneVerification = PhoneVerification.SENDING) }
        viewModelScope.launch {
            delay(900) // simulated SMS round-trip
            updateReg { it.copy(phoneVerification = PhoneVerification.SENT) }
        }
    }

    fun onSubmit() {
        val reg = _uiState.value.registration
        if (!reg.isValid) {
            updateReg { it.copy(errorMessage = "Name, phone, and address are required.") }
            return
        }
        updateReg { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            delay(600) // simulated write
            val student = RegisteredStudent(
                id = "STUD_" + UUID.randomUUID().toString().take(8).uppercase(),
                name = reg.name,
                phone = reg.phone,
                address = reg.address,
                education = reg.education,
                occupation = reg.occupation,
                category = reg.category,
                interestLevel = reg.interestLevel,
                batchName = reg.currentBatchName
            )
            _uiState.update {
                it.copy(
                    screen = RegFlowScreen.SUCCESS,
                    registration = RegistrationUiState(),
                    success = SuccessUiState(
                        student = student,
                        templates = templates,
                        selectedTemplateId = templates.first().id,
                        renderedMessage = templates.first().render(student.name, student.batchName)
                    )
                )
            }
        }
    }

    fun onTemplateSelected(templateId: String) {
        val student = _uiState.value.success.student ?: return
        val template = templates.firstOrNull { it.id == templateId } ?: return
        _uiState.update {
            it.copy(
                success = it.success.copy(
                    selectedTemplateId = templateId,
                    renderedMessage = template.render(student.name, student.batchName),
                    justCopied = true
                )
            )
        }
        viewModelScope.launch {
            delay(1200)
            _uiState.update { it.copy(success = it.success.copy(justCopied = false)) }
        }
    }

    /** Lets the screen re-run after a poster tap already auto-copied the message. */
    fun clearCopiedFlag() = _uiState.update { it.copy(success = it.success.copy(justCopied = false)) }

    fun onRegisterAnother() = _uiState.update { RegistrationFlowUiState() }

    private inline fun updateReg(block: (RegistrationUiState) -> RegistrationUiState) =
        _uiState.update { it.copy(registration = block(it.registration)) }
}

/* ============================================================================================
 *  ROOT — swap between Registration and Success with a soft crossfade.
 * ============================================================================================ */

@Composable
fun RegistrationFlowRoot(
    modifier: Modifier = Modifier,
    viewModel: RegistrationFlowViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Crossfade(targetState = uiState.screen, modifier = modifier, label = "registration-flow") { screen ->
        when (screen) {
            RegFlowScreen.REGISTRATION -> RegistrationScreen(
                uiState = uiState.registration,
                onNameChange = viewModel::onNameChange,
                onPhotoChange = viewModel::onPhotoChange,
                onPhoneChange = viewModel::onPhoneChange,
                onSendInvite = viewModel::onSendInvite,
                onAddressChange = viewModel::onAddressChange,
                onEducationChange = viewModel::onEducationChange,
                onOccupationChange = viewModel::onOccupationChange,
                onCategoryChange = viewModel::onCategoryChange,
                onInterestLevelChange = viewModel::onInterestLevelChange,
                onSubmit = viewModel::onSubmit
            )
            RegFlowScreen.SUCCESS -> SuccessScreen(
                uiState = uiState.success,
                onTemplateSelected = { id ->
                    val student = uiState.success.student
                    val template = uiState.success.templates.firstOrNull { it.id == id }
                    if (student != null && template != null) {
                        clipboardManager.setText(AnnotatedString(template.render(student.name, student.batchName)))
                    }
                    viewModel.onTemplateSelected(id)
                },
                onShareWhatsApp = { shareToWhatsApp(context, uiState.success.renderedMessage) },
                onRegisterAnother = viewModel::onRegisterAnother
            )
        }
    }
}

/* ============================================================================================
 *  REGISTRATION SCREEN
 * ============================================================================================ */

@Composable
fun RegistrationScreen(
    uiState: RegistrationUiState,
    onNameChange: (String) -> Unit,
    onPhotoChange: (Uri?) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSendInvite: () -> Unit,
    onAddressChange: (String) -> Unit,
    onEducationChange: (String) -> Unit,
    onOccupationChange: (String) -> Unit,
    onCategoryChange: (RegCategory) -> Unit,
    onInterestLevelChange: (InterestLevel) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                uiState.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.5.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = onSubmit,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(if (uiState.isSubmitting) "Registering…" else "Register Student", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "\uD83D\uDE4F Hari Bol",
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
                // Auto-assigned batch — read only, shown up top so the facilitator always knows
                // which cohort this registration lands in without having to pick it.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    Column {
                        Text("REGISTERING INTO", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
                        Text(uiState.currentBatchName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
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
                LabeledField("Phone Number") {
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = onPhoneChange,
                        placeholder = { Text("+91 98765 43210") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            when (uiState.phoneVerification) {
                                PhoneVerification.NOT_SENT -> TextButton(onClick = onSendInvite, enabled = uiState.phone.isNotBlank()) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Invite", fontSize = 12.5.sp)
                                }
                                PhoneVerification.SENDING -> Box(Modifier.padding(end = 12.dp)) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                }
                                PhoneVerification.SENT -> Row(
                                    modifier = Modifier.padding(end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Sent", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                LabeledField("Address") {
                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = onAddressChange,
                        placeholder = { Text("street / area / city") },
                        singleLine = true,
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
                        options = RegCategory.values().map { it.label },
                        selectedIndex = uiState.category.ordinal,
                        onSelected = { onCategoryChange(RegCategory.values()[it]) }
                    )
                }
            }

            item {
                LabeledField("Interest Level") {
                    SegmentedRow(
                        options = InterestLevel.values().map { it.label },
                        selectedIndex = uiState.interestLevel.ordinal,
                        onSelected = { onInterestLevelChange(InterestLevel.values()[it]) }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* ============================================================================================
 *  SUCCESS SCREEN
 * ============================================================================================ */

@Composable
fun SuccessScreen(
    uiState: SuccessUiState,
    onTemplateSelected: (String) -> Unit,
    onShareWhatsApp: () -> Unit,
    onRegisterAnother: () -> Unit,
    modifier: Modifier = Modifier
) {
    val student = uiState.student ?: return

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            AnimatedVisibility(visible = true, enter = scaleIn(animationSpec = tween(350)) + fadeIn(tween(350))) {
                Box(
                    modifier = Modifier.size(84.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(38.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Registration Successful", fontSize = 21.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(
                "${student.name} has joined ${student.batchName}",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
        }

        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    IdRow("Student ID", student.id)
                    IdRow("Name", student.name)
                    IdRow("Phone", student.phone, showDivider = false)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            Box(
                modifier = Modifier.size(160.dp).border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(24.dp))
                    .padding(10.dp).clip(RoundedCornerShape(14.dp)).background(Color.White),
                contentAlignment = Alignment.Center
            ) { PseudoQrCode(seed = student.id) }
            Spacer(Modifier.height(8.dp))
            Text("Scan to view profile", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
        }

        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("DETAILS", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { DetailItem(Icons.Default.School, "Batch", student.batchName) }
                        Box(Modifier.weight(1f)) { DetailItem(Icons.Default.LocationOn, "Address", student.address) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) { DetailItem(Icons.Default.Work, "Occupation", student.occupation) }
                        Box(Modifier.weight(1f)) { DetailItem(Icons.Default.School, "Education", student.education) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusChip(student.category.label)
                        StatusChip("${student.interestLevel.label} interest")
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("SEND WELCOME MESSAGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AnimatedVisibility(visible = uiState.justCopied) {
                    Text("Copied \u2713", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(uiState.templates) { template ->
                    PosterCard(
                        template = template,
                        selected = template.id == uiState.selectedTemplateId,
                        onClick = { onTemplateSelected(template.id) }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap a poster to auto-copy its message, then attach the image in WhatsApp and paste.",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp)
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { onTemplateSelected(uiState.selectedTemplateId ?: uiState.templates.first().id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy")
                }
                Button(onClick = onShareWhatsApp, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Register another student",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onRegisterAnother)
            )
        }
    }
}

/* ============================================================================================
 *  SHARED PIECES (kept private + local to this file on purpose)
 * ============================================================================================ */

/**
 * Circular photo preview + Gallery/Camera pickers. Fully self-contained — decodes the picked
 * Uri to a bitmap itself (no Coil/Glide dependency needed) and crossfades smoothly between the
 * empty placeholder and the chosen photo.
 *
 * Camera capture needs a FileProvider declared in your manifest (most apps already have one for
 * other features). If yours doesn't yet, add:
 *
 *   <provider
 *       android:name="androidx.core.content.FileProvider"
 *       android:authorities="${applicationId}.fileprovider"
 *       android:exported="false"
 *       android:grantUriPermissions="true">
 *       <meta-data android:name="android.support.FILE_PROVIDER_PATHS" android:resource="@xml/file_paths" />
 *   </provider>
 *
 * and res/xml/file_paths.xml:
 *   <paths><cache-path name="images" path="images/" /></paths>
 */
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
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Add student photo",
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
                modifier = Modifier.padding(top = 10.dp).clickable { onPhotoChange(null) }
            )
        }
    }
}

/** Decodes a picked/captured Uri to an ImageBitmap off the main thread. No image-loading
 *  library required — swap for Coil's rememberAsyncImagePainter if you already depend on it. */
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

/** Creates a fresh content:// Uri under the app's cache dir for the camera app to write into. */
private fun createImageCaptureUri(context: Context): Uri {
    val imagesDir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(imagesDir, "student_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            label.uppercase(),
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
                    option,
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
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        if (showDivider) Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Box(
            Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(13.dp)) }
        Column {
            Text(label.uppercase(), fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.4.sp)
            Text(value.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatusChip(text: String) {
    Box(
        Modifier.clip(RoundedCornerShape(100)).background(MaterialTheme.colorScheme.secondaryContainer).padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(text.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun PosterCard(template: PosterTemplate, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 134.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(width = if (selected) 2.dp else 0.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(9.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(template.emoji, fontSize = 17.sp, modifier = Modifier.align(Alignment.TopEnd))
        if (selected) {
            Box(
                Modifier.align(Alignment.TopStart).size(18.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(11.dp)) }
        }
        Text(template.title, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun PseudoQrCode(seed: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(132.dp)) {
        val cells = 9
        val cellSize = size.width / cells
        var hash = seed.hashCode()
        for (row in 0 until cells) {
            for (col in 0 until cells) {
                hash = hash * 31 + (row * cells + col)
                if ((hash and 1) == 0) {
                    drawRect(color = Color(0xFF171717), topLeft = Offset(col * cellSize, row * cellSize), size = Size(cellSize, cellSize))
                }
            }
        }
    }
}

private fun shareToWhatsApp(context: Context, message: String) {
    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message); setPackage("com.whatsapp") }
    runCatching { context.startActivity(intent) }.onFailure {
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, message)
        }, "Share message"))
    }
}

/* ============================================================================================
 *  PREVIEWS — drop this file in and these render immediately, no setup needed.
 * ============================================================================================ */

private val previewRegistrationState = RegistrationUiState(
    name = "", phone = "", phoneVerification = PhoneVerification.NOT_SENT
)

private val previewFilledRegistrationState = RegistrationUiState(
    name = "Rahul Verma",
    phone = "+91 99887 76655",
    phoneVerification = PhoneVerification.SENT,
    address = "123, Gomti Nagar, Lucknow",
    education = "B.Tech CSE",
    occupation = "student",
    category = RegCategory.YOUTH,
    interestLevel = InterestLevel.HIGH
)

private val previewTemplates = listOf(
    PosterTemplate("welcome", "Welcome to OTP", "\uD83E\uDE94", "Hare Krishna {name} \uD83D\uDE4F Welcome to OTP — {batch}!"),
    PosterTemplate("reminder", "Sunday Reminder", "\uD83D\uDD49", "Hare Krishna {name}! Class begins 10 AM this Sunday."),
    PosterTemplate("festival", "Janmashtami Invite", "\uD83E\uDD6F", "Hare Krishna {name}! Join us this Janmashtami.")
)

private val previewSuccessState = SuccessUiState(
    student = RegisteredStudent(
        id = "STUD_4F9A1C2B",
        name = "Rahul Verma",
        phone = "+91 99887 76655",
        address = "123, Gomti Nagar, Lucknow",
        education = "B.Tech CSE",
        occupation = "student",
        category = RegCategory.YOUTH,
        interestLevel = InterestLevel.HIGH,
        batchName = "Sunday Morning Batch A"
    ),
    templates = previewTemplates,
    selectedTemplateId = "welcome",
    renderedMessage = previewTemplates[0].render("Rahul Verma", "Sunday Morning Batch A")
)

@Preview(showBackground = true, name = "Registration — Empty (Light)")
@Composable
private fun RegistrationScreenPreviewEmpty() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        RegistrationScreen(previewRegistrationState, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Registration — Filled (Dark)", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RegistrationScreenPreviewFilled() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        RegistrationScreen(previewFilledRegistrationState, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Success — Light")
@Composable
private fun SuccessScreenPreviewLight() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        SuccessScreen(previewSuccessState, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Success — Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SuccessScreenPreviewDark() {
    Otp_class_appTheme(themeMode = ThemeMode.DARK) {
        SuccessScreen(previewSuccessState.copy(justCopied = true), {}, {}, {})
    }
}

/** Full interactive flow, wired to the dummy ViewModel — register a student and watch it
 *  transition straight into the Success screen with real (dummy) data. */
@Preview(showBackground = true, name = "Full Flow — Interactive")
@Composable
private fun RegistrationFlowFullPreview() {
    Otp_class_appTheme(themeMode = ThemeMode.LIGHT) {
        RegistrationFlowRoot()
    }
}