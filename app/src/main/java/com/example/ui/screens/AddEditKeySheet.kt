package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeyItem
import com.example.data.model.ProviderPreset
import com.example.data.model.ProviderPresets
import com.example.data.security.VaultSecurity
import com.example.ui.components.ApiKeyInputSection
import com.example.ui.components.CategoryPickerRow
import com.example.ui.components.EnvironmentPickerRow
import com.example.ui.components.ProviderIconBadge
import com.example.ui.components.ProviderPresetCarousel
import com.example.ui.components.RotationIntervalPicker
import com.example.ui.components.VaultColorDotPicker
import com.example.ui.theme.CyberGold
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditKeySheet(
    existingItem: ApiKeyItem? = null,
    initialPreset: ProviderPreset? = null,
    initialKeyText: String = "",
    existingTitles: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (ApiKeyItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

    val defaultProvider = when {
        existingItem != null -> existingItem.provider
        initialPreset != null -> initialPreset.name
        initialKeyText.isNotBlank() -> VaultSecurity.detectProviderFromKey(initialKeyText)
        else -> "OpenAI"
    }

    var selectedProvider by remember { mutableStateOf(defaultProvider) }

    val preset = remember(selectedProvider) { ProviderPresets.findByName(selectedProvider) }

    val defaultTitle = when {
        existingItem != null -> existingItem.title
        initialPreset != null -> "${initialPreset.name} API Key"
        initialKeyText.isNotBlank() -> "${VaultSecurity.detectProviderFromKey(initialKeyText)} Key"
        else -> "OpenAI Main Key"
    }

    var title by remember { mutableStateOf(defaultTitle) }

    var apiKey by remember { mutableStateOf(existingItem?.apiKey ?: initialKeyText) }
    var secretKey by remember { mutableStateOf(existingItem?.secretKey ?: "") }
    var selectedCategory by remember { mutableStateOf(existingItem?.category ?: preset.category) }
    var selectedEnvironment by remember { mutableStateOf(existingItem?.environment ?: "Production") }
    var endpointUrl by remember { mutableStateOf(existingItem?.endpointUrl ?: preset.defaultEndpoint) }
    var organizationId by remember { mutableStateOf(existingItem?.organizationId ?: "") }
    var modelOrProject by remember { mutableStateOf(existingItem?.modelOrProject ?: "") }
    var notes by remember { mutableStateOf(existingItem?.notes ?: "") }
    var tags by remember { mutableStateOf(existingItem?.tags ?: "") }
    var isPinned by remember { mutableStateOf(existingItem?.isPinned ?: false) }
    var rotationDays by remember { mutableStateOf(existingItem?.rotationDays ?: 90) }
    var selectedColorHex by remember { mutableStateOf<String?>(existingItem?.colorHex ?: preset.defaultColorHex) }

    var isKeyVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showAdvancedSettings by remember { mutableStateOf(existingItem != null) }

    var showDuplicateWarning by remember { mutableStateOf(false) }
    var pendingItemToSave by remember { mutableStateOf<ApiKeyItem?>(null) }
    var dismissAfterSave by remember { mutableStateOf(false) }

    val handleSaveRequest: (Boolean) -> Unit = { shouldDismiss ->
        if (title.isBlank()) {
            errorMessage = "Please provide a name or description for this key"
        } else if (apiKey.isBlank()) {
            errorMessage = "Please enter or paste an API Key"
        } else {
            val item = ApiKeyItem(
                id = existingItem?.id ?: 0L,
                title = title.trim(),
                apiKey = apiKey.trim(),
                secretKey = secretKey.trim(),
                provider = selectedProvider,
                category = selectedCategory,
                environment = selectedEnvironment,
                endpointUrl = endpointUrl.trim(),
                organizationId = organizationId.trim(),
                modelOrProject = modelOrProject.trim(),
                notes = notes.trim(),
                tags = tags.trim(),
                isPinned = isPinned,
                copyCount = existingItem?.copyCount ?: 0,
                lastCopiedAt = existingItem?.lastCopiedAt,
                createdAt = existingItem?.createdAt ?: System.currentTimeMillis(),
                rotationDays = rotationDays,
                colorHex = selectedColorHex ?: preset.defaultColorHex
            )

            val hasDuplicate = existingTitles.any { it.equals(title.trim(), ignoreCase = true) }
            if (hasDuplicate) {
                pendingItemToSave = item
                dismissAfterSave = shouldDismiss
                showDuplicateWarning = true
            } else {
                onSave(item)
                if (shouldDismiss) {
                    onDismiss()
                } else {
                    title = ""
                    apiKey = ""
                    secretKey = ""
                    notes = ""
                    tags = ""
                    errorMessage = null
                }
            }
        }
    }

    if (showDuplicateWarning && pendingItemToSave != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDuplicateWarning = false },
            title = { Text("Duplicate Label", color = TextPrimary) },
            text = { Text("A key with the label '${pendingItemToSave?.title}' already exists. Are you sure you want to save another key with the exact same label?", color = TextSecondary) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val item = pendingItemToSave!!
                        showDuplicateWarning = false
                        onSave(item)
                        if (dismissAfterSave) {
                            onDismiss()
                        } else {
                            title = ""
                            apiKey = ""
                            secretKey = ""
                            notes = ""
                            tags = ""
                            errorMessage = null
                        }
                        pendingItemToSave = null
                    }
                ) { Text("Save Anyway", color = CyberGold) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDuplicateWarning = false; pendingItemToSave = null }
                ) { Text("Cancel", color = TextSecondary) }
            },
            containerColor = ObsidianSurfaceElevated
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProviderIconBadge(
                        provider = selectedProvider,
                        colorHex = preset.defaultColorHex,
                        size = 36
                    )
                    Column {
                        Text(
                            text = if (existingItem == null) "Add Secret / API Key" else "Edit Secret",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Stored locally & protected",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                }
            }

            // Quick Provider Preset Selector
            ProviderPresetCarousel(
                selectedProvider = selectedProvider,
                onSelectProvider = { item ->
                    selectedProvider = item.name
                    if (title.isEmpty() || title.contains("Key", ignoreCase = true)) {
                        title = "${item.name} Key"
                    }
                    if (existingItem == null) {
                        selectedCategory = item.category
                        if (endpointUrl.isEmpty() || endpointUrl == preset.defaultEndpoint) {
                            endpointUrl = item.defaultEndpoint
                        }
                    }
                }
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Name / Description", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_title"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberGold,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = ObsidianSurfaceElevated,
                    unfocusedContainerColor = ObsidianSurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // API Key Input with Live Provider Auto-Detection Badge & Paste Button & Show/Hide
            ApiKeyInputSection(
                apiKey = apiKey,
                onApiKeyChange = { apiKey = it },
                placeholderKey = preset.placeholderKey,
                isKeyVisible = isKeyVisible,
                onToggleVisibility = { isKeyVisible = !isKeyVisible },
                clipboardManager = clipboardManager,
                onProviderDetected = { detected ->
                    selectedProvider = detected
                    if (title.isEmpty() || title.endsWith("Key") || title.endsWith("API Key")) {
                        title = "$detected Key"
                    }
                }
            )

            // Optional Secret / Second Key (e.g. Webhook secret, Private Key)
            OutlinedTextField(
                value = secretKey,
                onValueChange = { secretKey = it },
                label = { Text("Secret Key / Key ID / Webhook Secret (Optional)", color = TextSecondary) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                textStyle = MonospaceCodeStyle.copy(color = TextPrimary),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberGold,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = ObsidianSurfaceElevated,
                    unfocusedContainerColor = ObsidianSurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Environment Picker
            EnvironmentPickerRow(
                selectedEnvironment = selectedEnvironment,
                onEnvironmentSelect = { selectedEnvironment = it }
            )

            // Category Picker
            CategoryPickerRow(
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it }
            )

            // Pastel Note Color Picker
            VaultColorDotPicker(
                selectedColorHex = selectedColorHex,
                onSelectColor = { selectedColorHex = it }
            )

            androidx.compose.material3.TextButton(
                onClick = { showAdvancedSettings = !showAdvancedSettings },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showAdvancedSettings) "Hide Advanced Settings" else "Show Advanced Settings", color = CyberGold)
            }

            androidx.compose.animation.AnimatedVisibility(visible = showAdvancedSettings) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Optional Metadata Row (Endpoint URL, Organization ID)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = endpointUrl,
                            onValueChange = { endpointUrl = it },
                            label = { Text("Base Endpoint URL", color = TextSecondary, fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = MonospaceCodeStyle.copy(fontSize = 11.sp, color = TextPrimary),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGold,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ObsidianSurfaceElevated,
                                unfocusedContainerColor = ObsidianSurfaceElevated
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = organizationId,
                            onValueChange = { organizationId = it },
                            label = { Text("Org / Project ID", color = TextSecondary, fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = MonospaceCodeStyle.copy(fontSize = 11.sp, color = TextPrimary),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGold,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = ObsidianSurfaceElevated,
                                unfocusedContainerColor = ObsidianSurfaceElevated
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Tags & Notes
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated, e.g. client, prod, llm)", color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ObsidianSurfaceElevated,
                            unfocusedContainerColor = ObsidianSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Internal Developer Notes", color = TextSecondary) },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ObsidianSurfaceElevated,
                            unfocusedContainerColor = ObsidianSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Rotation interval
                    RotationIntervalPicker(
                        rotationDays = rotationDays,
                        onRotationDaysChange = { rotationDays = it }
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = StatusDanger,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Save Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (existingItem == null) {
                    Button(
                        onClick = { handleSaveRequest(false) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("button_save_and_add_another"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "Save & Add Another",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { handleSaveRequest(true) },
                    modifier = Modifier
                        .weight(if (existingItem == null) 1f else 2f)
                        .height(52.dp)
                        .testTag("button_save_key"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberGold,
                        contentColor = Color(0xFF1E1400)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingItem == null) "Save Key" else "Update Secret",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
