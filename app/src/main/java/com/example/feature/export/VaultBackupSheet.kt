package com.example.feature.export

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.CyberGoldLight
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianBorderLight
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.StatusDanger
import com.example.core.designsystem.StatusWarning
import com.example.core.designsystem.TextMuted
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import com.example.core.designsystem.VibrantPillBg
import com.example.core.model.ApiKeyItem
import com.example.core.security.VaultBackupCrypto
import com.example.core.security.VaultSecurity
import com.example.feature.vault.VaultViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultBackupSheet(
    viewModel: VaultViewModel,
    keys: List<ApiKeyItem>,
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 1)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VibrantPillBg)
                            .border(1.dp, CyberCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeTab == 0) Icons.Default.CloudUpload else Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = if (activeTab == 0) CyberCyan else CyberGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Encrypted Vault Backup",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "AES-256-GCM Portable Backups (.keynest)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = ObsidianSurfaceElevated,
                contentColor = CyberCyan,
                divider = { HorizontalDivider(color = ObsidianBorder) },
                indicator = { tabPositions ->
                    if (activeTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                            color = if (activeTab == 0) CyberCyan else CyberGold,
                            height = 3.dp
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Export Backup", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    selectedContentColor = CyberCyan,
                    unselectedContentColor = TextSecondary
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Restore Vault", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    selectedContentColor = CyberGold,
                    unselectedContentColor = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Views
            if (activeTab == 0) {
                ExportBackupView(
                    keys = keys,
                    viewModel = viewModel,
                    onExportSuccess = {
                        onDismiss()
                    }
                )
            } else {
                RestoreBackupView(
                    viewModel = viewModel,
                    onRestoreSuccess = {
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
private fun ExportBackupView(
    keys: List<ApiKeyItem>,
    viewModel: VaultViewModel,
    onExportSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val entropyResult = remember(password) { VaultSecurity.calculateEntropy(password) }

    val defaultFileName = remember {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        "keynest_backup_$dateStr.keynest"
    }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            isExporting = true
            errorMessage = null
            scope.launch {
                val result = viewModel.createAndExportBackup(
                    uri = uri,
                    passphrase = password.toCharArray(),
                    keys = keys
                )
                isExporting = false
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: keys.size
                    Toast.makeText(context, "Encrypted backup created with $count secrets!", Toast.LENGTH_LONG).show()
                    onExportSuccess()
                } else {
                    errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to write backup"
                    Toast.makeText(context, "Export error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explanatory Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianSurfaceElevated)
                .border(1.dp, ObsidianBorderLight, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Hardware-Independent Encrypted Backup",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Android KeyStore keys cannot leave this physical device. This export encrypts your entire vault with PBKDF2 (100,000 rounds) + AES-256-GCM using your password, allowing secure cross-device transfer or cold storage.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        // Vault summary pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(VibrantPillBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Key, contentDescription = null, tint = CyberGold, modifier = Modifier.size(16.dp))
                Text("Secrets to Include", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "${keys.size} items",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = CyberGold
            )
        }

        // Password Input
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "BACKUP PASSPHRASE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = { Text("Choose a strong backup password...", color = TextMuted) },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = TextSecondary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = ObsidianSurfaceElevated,
                    unfocusedContainerColor = ObsidianSurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Password Strength Indicator
        if (password.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Strength: ${entropyResult.strength}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(android.graphics.Color.parseColor(entropyResult.colorHex))
                    )
                    Text(
                        text = "${entropyResult.entropyBits} bits entropy",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                LinearProgressIndicator(
                    progress = { entropyResult.strengthPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(android.graphics.Color.parseColor(entropyResult.colorHex)),
                    trackColor = ObsidianBorder
                )
            }
        }

        // Confirm Password Input
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "CONFIRM PASSPHRASE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                placeholder = { Text("Re-enter backup password...", color = TextMuted) },
                singleLine = true,
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = ObsidianSurfaceElevated,
                    unfocusedContainerColor = ObsidianSurfaceElevated,
                    errorBorderColor = StatusDanger
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                Text("Passwords do not match", color = StatusDanger, fontSize = 11.sp)
            }
        }

        // Error message banner
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            errorMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusDanger.copy(alpha = 0.15f))
                        .border(1.dp, StatusDanger.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                    Text(msg, color = StatusDanger, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Export Action Button
        Button(
            onClick = {
                if (keys.isEmpty()) {
                    errorMessage = "Cannot export an empty vault."
                    return@Button
                }
                if (password.isBlank()) {
                    errorMessage = "Please enter a passphrase to encrypt your backup."
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match."
                    return@Button
                }
                createDocLauncher.launch(defaultFileName)
            },
            enabled = !isExporting && keys.isNotEmpty() && password.isNotBlank() && password == confirmPassword,
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberCyan,
                contentColor = Color.Black,
                disabledContainerColor = ObsidianBorder,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isExporting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Encrypting & Writing...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Encrypted Backup (.keynest)", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RestoreBackupView(
    viewModel: VaultViewModel,
    onRestoreSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var backupMetadata by remember { mutableStateOf<VaultBackupCrypto.BackupMetadata?>(null) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var replaceExisting by remember { mutableStateOf(false) }
    var isRestoring by remember { mutableStateOf(false) }
    var isInspecting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            errorMessage = null
            isInspecting = true
            scope.launch {
                val metaResult = viewModel.inspectBackupMetadata(uri)
                isInspecting = false
                if (metaResult.isSuccess) {
                    backupMetadata = metaResult.getOrNull()
                } else {
                    backupMetadata = null
                    errorMessage = "File is not a valid KeyNest encrypted backup."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Instructions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianSurfaceElevated)
                .border(1.dp, ObsidianBorderLight, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = CyberGold, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Cross-Device Migration & Restore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "Select an encrypted `.keynest` backup file created on any Android device. After entering the backup passphrase, all secrets will be decrypted and securely re-encrypted into this device's hardware KeyStore.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        // File Selection Box
        if (selectedUri == null) {
            OutlinedButton(
                onClick = { openDocLauncher.launch(arrayOf("*/*", "application/octet-stream", "application/json")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CyberGold
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberGold.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select .keynest Backup File", fontWeight = FontWeight.Bold)
            }
        } else {
            // Selected File Details Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(VibrantPillBg)
                    .border(1.dp, CyberEmerald.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isInspecting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberEmerald, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Backup File Selected", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            backupMetadata?.let { meta ->
                                val dateFormatted = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(meta.createdAt))
                                Text(
                                    text = "${meta.itemCount} secrets • Created $dateFormatted",
                                    fontSize = 11.sp,
                                    color = CyberEmerald
                                )
                            } ?: Text("Reading backup metadata...", fontSize = 11.sp, color = TextSecondary)
                        }
                    }

                    IconButton(onClick = {
                        selectedUri = null
                        backupMetadata = null
                        password = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Change File", tint = TextSecondary)
                    }
                }
            }
        }

        // Decryption Password Input
        if (selectedUri != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "BACKUP PASSPHRASE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    placeholder = { Text("Enter the password for this backup...", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                tint = TextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGold,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = ObsidianSurfaceElevated,
                        unfocusedContainerColor = ObsidianSurfaceElevated
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Restore Strategy Choice
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "IMPORT STRATEGY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                // Strategy 1: Merge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!replaceExisting) CyberGoldLight else ObsidianSurfaceElevated)
                        .border(1.dp, if (!replaceExisting) CyberGold.copy(alpha = 0.5f) else ObsidianBorder, RoundedCornerShape(10.dp))
                        .clickable { replaceExisting = false }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (!replaceExisting) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (!replaceExisting) CyberGold else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text("Merge with Existing Vault", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text("Keep all current secrets and import new ones from backup", fontSize = 11.sp, color = TextSecondary)
                    }
                }

                // Strategy 2: Replace
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (replaceExisting) StatusDanger.copy(alpha = 0.12f) else ObsidianSurfaceElevated)
                        .border(1.dp, if (replaceExisting) StatusDanger.copy(alpha = 0.5f) else ObsidianBorder, RoundedCornerShape(10.dp))
                        .clickable { replaceExisting = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (replaceExisting) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (replaceExisting) StatusDanger else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text("Replace Entire Vault", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (replaceExisting) StatusDanger else TextPrimary)
                        Text("Wipe current local vault database and load backup fresh", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Error message banner
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            errorMessage?.let { msg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusDanger.copy(alpha = 0.15f))
                        .border(1.dp, StatusDanger.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(16.dp))
                    Text(msg, color = StatusDanger, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Restore Action Button
        Button(
            onClick = {
                val uri = selectedUri ?: return@Button
                if (password.isBlank()) {
                    errorMessage = "Please enter the backup passphrase."
                    return@Button
                }

                isRestoring = true
                errorMessage = null
                scope.launch {
                    val result = viewModel.restoreEncryptedBackup(
                        uri = uri,
                        passphrase = password.toCharArray(),
                        replaceExisting = replaceExisting
                    )
                    isRestoring = false
                    if (result.isSuccess) {
                        val count = result.getOrNull() ?: 0
                        Toast.makeText(context, "Successfully restored $count secrets to your vault!", Toast.LENGTH_LONG).show()
                        onRestoreSuccess()
                    } else {
                        val error = result.exceptionOrNull()
                        errorMessage = error?.localizedMessage ?: "Decryption failed: check password."
                        Toast.makeText(context, "Restore failed: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = selectedUri != null && password.isNotBlank() && !isRestoring,
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberGold,
                contentColor = Color.Black,
                disabledContainerColor = ObsidianBorder,
                disabledContentColor = TextMuted
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isRestoring) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Decrypting & Restoring Vault...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unlock & Restore Vault", fontWeight = FontWeight.Bold)
            }
        }
    }
}
