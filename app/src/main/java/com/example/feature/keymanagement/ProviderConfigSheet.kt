package com.example.feature.keymanagement

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianBorderLight
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.ObsidianSurfaceHighlight
import com.example.core.designsystem.StatusDanger
import com.example.core.designsystem.StatusSuccess
import com.example.core.designsystem.StatusWarning
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import com.example.core.model.ProviderKeyItem
import com.example.core.model.ProviderPreset
import com.example.core.model.ProviderPresets
import com.example.core.model.ProviderProfile
import com.example.core.network.ConnectionResult
import com.example.core.network.ProviderConnectionTester
import com.example.core.security.VaultSecurity
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderConfigSheet(
    profile: ProviderProfile,
    preset: ProviderPreset = ProviderPresets.findById(profile.id),
    onDismiss: () -> Unit,
    onSaveProfile: (ProviderProfile) -> Unit,
    onDeleteProfile: (() -> Unit)? = null,
    onCopyKey: (String) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var displayName by remember { mutableStateOf(profile.displayName) }
    var baseUrl by remember { mutableStateOf(profile.baseUrl.ifBlank { preset.defaultEndpoint }) }
    var customHeadersJson by remember { mutableStateOf(profile.customHeadersJson) }
    var isActive by remember { mutableStateOf(profile.isActive) }
    var keysList by remember { mutableStateOf(profile.keys) }
    var activeKeyId by remember {
        mutableStateOf(
            if (profile.activeKeyId.isNotEmpty()) profile.activeKeyId
            else profile.keys.firstOrNull()?.id.orEmpty()
        )
    }

    // New Key input form state
    var newKeyLabel by remember { mutableStateOf("") }
    var newKeyValue by remember { mutableStateOf("") }
    var isNewKeyVisible by remember { mutableStateOf(false) }

    // Live Connection Test State
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<ConnectionResult?>(null) }

    // Advanced headers expand
    var showAdvancedHeaders by remember { mutableStateOf(false) }

    val brandColor = try {
        Color(android.graphics.Color.parseColor(preset.defaultColorHex.ifBlank { profile.colorHex }))
    } catch (_: Exception) {
        CyberGold
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(brandColor.copy(alpha = 0.16f))
                            .border(1.dp, brandColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(2).uppercase(),
                            color = brandColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }

                    Column {
                        Text(
                            text = displayName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${preset.category} Provider",
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (preset.consoleUrl.isNotBlank()) {
                        IconButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(preset.consoleUrl)))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Developer Console",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Active Provider Toggle & Display Name Field
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Enable Provider", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Active in aggregator & .env export", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberEmerald,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = ObsidianSurfaceHighlight
                            )
                        )
                    }

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }

            // Base URL Section with Reset button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Base Endpoint URL", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        if (baseUrl != preset.defaultEndpoint && preset.defaultEndpoint.isNotBlank()) {
                            Text(
                                text = "Reset Default",
                                fontSize = 11.sp,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { baseUrl = preset.defaultEndpoint }
                                    .padding(4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        placeholder = { Text(preset.defaultEndpoint.ifBlank { "https://api.example.com" }) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp, color = TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }

            // Multi-Key Vault Section
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = CyberGold, modifier = Modifier.size(16.dp))
                            Text("Stored API Keys (${keysList.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text(
                            text = "Select active key below",
                            fontSize = 11.sp,
                            color = TextTertiary
                        )
                    }

                    // Existing Keys List
                    if (keysList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No API keys saved yet for this provider", color = TextTertiary, fontSize = 12.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            keysList.forEach { keyItem ->
                                val isSelectedActive = (activeKeyId == keyItem.id)
                                var isKeyRevealed by remember { mutableStateOf(false) }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelectedActive) ObsidianSurfaceHighlight else ObsidianSurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelectedActive) CyberEmerald.copy(alpha = 0.5f) else ObsidianBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { activeKeyId = keyItem.id }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelectedActive) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = if (isSelectedActive) "Active Key" else "Set as Active",
                                                tint = if (isSelectedActive) CyberEmerald else TextTertiary,
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = keyItem.label,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        color = TextPrimary
                                                    )
                                                    if (isSelectedActive) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = CyberEmerald.copy(alpha = 0.15f)
                                                        ) {
                                                            Text(
                                                                "ACTIVE",
                                                                color = CyberEmerald,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = if (isKeyRevealed) keyItem.apiKey else VaultSecurity.maskKey(keyItem.apiKey),
                                                    style = MonospaceCodeStyle.copy(
                                                        fontSize = 11.sp,
                                                        color = TextSecondary
                                                    )
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            IconButton(
                                                onClick = { isKeyRevealed = !isKeyRevealed },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isKeyRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = "Toggle Key Reveal",
                                                    tint = TextTertiary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { onCopyKey(keyItem.apiKey) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Key",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    keysList = keysList.filterNot { it.id == keyItem.id }
                                                    if (activeKeyId == keyItem.id) {
                                                        activeKeyId = keysList.firstOrNull()?.id.orEmpty()
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete Key",
                                                    tint = StatusDanger,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add Key Subform
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ObsidianSurfaceHighlight,
                        border = BorderStroke(1.dp, ObsidianBorderLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Add New Key", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)

                            OutlinedTextField(
                                value = newKeyLabel,
                                onValueChange = { newKeyLabel = it },
                                label = { Text("Label (e.g. Production, Staging, Dev)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = ObsidianBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = newKeyValue,
                                onValueChange = { newKeyValue = it },
                                label = { Text("API Key / Token Value") },
                                placeholder = { Text(preset.placeholderKey) },
                                visualTransformation = if (isNewKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { isNewKeyVisible = !isNewKeyVisible }) {
                                            Icon(
                                                imageVector = if (isNewKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle Visibility",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                val clip = clipboardManager.getText()?.text
                                                if (!clip.isNullOrBlank()) {
                                                    newKeyValue = clip.trim()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.ContentPaste,
                                                contentDescription = "Paste from Clipboard",
                                                tint = CyberCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp, color = TextPrimary),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = ObsidianBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    if (newKeyValue.isNotBlank()) {
                                        val label = newKeyLabel.ifBlank { "Key #${keysList.size + 1}" }
                                        val newKey = ProviderKeyItem(
                                            id = UUID.randomUUID().toString(),
                                            label = label,
                                            apiKey = newKeyValue.trim(),
                                            isPrimary = keysList.isEmpty()
                                        )
                                        keysList = keysList + newKey
                                        if (activeKeyId.isEmpty() || keysList.size == 1) {
                                            activeKeyId = newKey.id
                                        }
                                        newKeyLabel = ""
                                        newKeyValue = ""
                                    }
                                },
                                enabled = newKeyValue.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().testTag("btn_add_key_confirm"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add Key to Provider", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Collapsible Advanced Custom Headers
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvancedHeaders = !showAdvancedHeaders },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Custom Headers (JSON)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Icon(
                            imageVector = if (showAdvancedHeaders) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = showAdvancedHeaders) {
                        OutlinedTextField(
                            value = customHeadersJson,
                            onValueChange = { customHeadersJson = it },
                            placeholder = { Text("{\"anthropic-version\": \"2023-06-01\"}") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp, color = TextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            maxLines = 4
                        )
                    }
                }
            }

            // Live Test Connection Section
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Live Connection Tester", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isTestingConnection = true
                                    testResult = null
                                    val activeKey = keysList.find { it.id == activeKeyId } ?: keysList.firstOrNull()
                                    val tempProfile = profile.copy(
                                        baseUrl = baseUrl,
                                        customHeadersJson = customHeadersJson,
                                        keys = keysList,
                                        activeKeyId = activeKeyId
                                    )
                                    testResult = ProviderConnectionTester.testConnection(
                                        profile = tempProfile,
                                        overrideKey = activeKey?.apiKey
                                    )
                                    isTestingConnection = false
                                }
                            },
                            enabled = !isTestingConnection && keysList.isNotEmpty(),
                            modifier = Modifier.testTag("btn_run_test_connection"),
                            border = BorderStroke(1.dp, CyberEmerald),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp),
                                    color = CyberEmerald
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Testing...", fontSize = 12.sp, color = CyberEmerald)
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Ping Endpoint", fontSize = 12.sp, color = CyberEmerald, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Result Pill
                    testResult?.let { res ->
                        val (bgColor, borderColor, textColor, title, sub) = when (res) {
                            is ConnectionResult.Success -> Quintuple(
                                StatusSuccess.copy(alpha = 0.12f),
                                StatusSuccess.copy(alpha = 0.4f),
                                StatusSuccess,
                                "HTTP ${res.statusCode} Connected",
                                "Roundtrip Latency: ${res.latencyMs}ms"
                            )
                            is ConnectionResult.Failure -> Quintuple(
                                StatusDanger.copy(alpha = 0.12f),
                                StatusDanger.copy(alpha = 0.4f),
                                StatusDanger,
                                res.errorMessage,
                                res.latencyMs?.let { "Latency: ${it}ms" } ?: "Failed"
                            )
                            is ConnectionResult.Error -> Quintuple(
                                StatusWarning.copy(alpha = 0.12f),
                                StatusWarning.copy(alpha = 0.4f),
                                StatusWarning,
                                res.message,
                                "Network error"
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bgColor,
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(sub, color = textColor.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Bottom Actions: Save & Delete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onDeleteProfile != null) {
                    OutlinedButton(
                        onClick = {
                            onDeleteProfile()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, StatusDanger),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusDanger)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete")
                    }
                }

                Button(
                    onClick = {
                        val finalKeys = keysList.map { it.copy(isPrimary = (it.id == activeKeyId)) }
                        val updatedProfile = profile.copy(
                            displayName = displayName.ifBlank { preset.name },
                            baseUrl = baseUrl,
                            customHeadersJson = customHeadersJson,
                            isActive = isActive,
                            keys = finalKeys,
                            activeKeyId = activeKeyId.ifEmpty { finalKeys.firstOrNull()?.id.orEmpty() },
                            updatedAt = System.currentTimeMillis()
                        )
                        onSaveProfile(updatedProfile)
                        onDismiss()
                    },
                    modifier = Modifier.weight(2f).testTag("btn_save_provider_profile"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberEmerald,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Save Provider", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
