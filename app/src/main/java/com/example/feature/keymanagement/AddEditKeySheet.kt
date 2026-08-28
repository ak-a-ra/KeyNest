package com.example.feature.keymanagement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ApiKeyItem
import com.example.core.model.ProviderPreset
import com.example.core.model.ProviderPresets
import com.example.core.security.VaultSecurity
import com.example.feature.keymanagement.ApiKeyInputSection
import com.example.feature.keymanagement.CategoryPickerRow
import com.example.feature.vault.ProviderIconBadge
import com.example.feature.keymanagement.ProviderPresetCarousel
import com.example.feature.keymanagement.RotationIntervalPicker
import com.example.feature.keymanagement.VaultColorDotPicker
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.StatusDanger
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditKeySheet(
    existingItem: ApiKeyItem? = null,
    initialPreset: ProviderPreset? = null,
    initialKeyText: String = "",
    existingTitles: List<String> = emptyList(),
    availableTags: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (ApiKeyItem) -> Unit,
    onBatchSave: ((List<ApiKeyItem>) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

    var selectedTabMode by remember { androidx.compose.runtime.mutableIntStateOf(0) } // 0 = Single Key, 1 = Batch Multi-Key

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
    var extraSecretFields by remember { mutableStateOf(listOf<ExtraSecretField>()) }

    var selectedCategory by remember { mutableStateOf(existingItem?.category ?: preset.category) }
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

    // Batch Multi-Key Entries
    var batchEntries by remember {
        mutableStateOf(
            listOf(
                BatchKeyEntry(
                    title = if (initialKeyText.isNotBlank()) "${VaultSecurity.detectProviderFromKey(initialKeyText)} Key" else "OpenAI Key",
                    apiKey = initialKeyText,
                    provider = if (initialKeyText.isNotBlank()) VaultSecurity.detectProviderFromKey(initialKeyText) else "OpenAI"
                ),
                BatchKeyEntry(
                    title = "Google Gemini Key",
                    provider = "Google Gemini"
                ),
                BatchKeyEntry(
                    title = "Anthropic Claude Key",
                    provider = "Anthropic Claude"
                )
            )
        )
    }

    val handleSaveRequest: (Boolean) -> Unit = { shouldDismiss ->
        if (selectedTabMode == 1 && existingItem == null) {
            // Batch Save
            val validEntries = batchEntries.filter { it.apiKey.isNotBlank() }
            if (validEntries.isEmpty()) {
                errorMessage = "Please enter at least one API Key across the key boxes"
            } else {
                val now = System.currentTimeMillis()
                val itemsToInsert = validEntries.mapIndexed { idx, entry ->
                    val p = ProviderPresets.findByName(entry.provider)
                    val baseTitle = entry.title.ifBlank { "${entry.provider} Key" }.trim()
                    val disambiguatedTitle = if (validEntries.count { it.provider == entry.provider } > 1 && entry.title.isBlank()) {
                        "$baseTitle #${idx + 1}"
                    } else {
                        baseTitle
                    }
                    ApiKeyItem(
                        title = disambiguatedTitle,
                        apiKey = entry.apiKey.trim(),
                        provider = entry.provider,
                        category = entry.category,
                        colorHex = p.defaultColorHex,
                        createdAt = now + idx
                    )
                }
                if (onBatchSave != null) {
                    onBatchSave(itemsToInsert)
                } else {
                    itemsToInsert.forEach { onSave(it) }
                }
                onDismiss()
            }
        } else {
            // Single Save
            if (title.isBlank()) {
                errorMessage = "Please provide a name or description for this key"
            } else if (apiKey.isBlank()) {
                errorMessage = "Please enter or paste an API Key"
            } else {
                val combinedSecretKey = buildString {
                    if (secretKey.isNotBlank()) append(secretKey.trim())
                    extraSecretFields.filter { it.label.isNotBlank() && it.value.isNotBlank() }.forEach { field ->
                        if (isNotEmpty()) append("\n")
                        append("${field.label.trim()}: ${field.value.trim()}")
                    }
                }

                val item = ApiKeyItem(
                    id = existingItem?.id ?: 0L,
                    title = title.trim(),
                    apiKey = apiKey.trim(),
                    secretKey = combinedSecretKey,
                    provider = selectedProvider,
                    category = selectedCategory,
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
                        extraSecretFields = emptyList()
                        notes = ""
                        tags = ""
                        errorMessage = null
                    }
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
                        val item = pendingItemToSave ?: return@TextButton
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
                        provider = if (selectedTabMode == 0) selectedProvider else "KeyNest Batch",
                        colorHex = preset.defaultColorHex,
                        size = 36
                    )
                    Column {
                        Text(
                            text = when {
                                existingItem != null -> "Edit Secret"
                                selectedTabMode == 1 -> "Add Multiple API Keys"
                                else -> "Add Secret / API Key"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (selectedTabMode == 1) "Fill multiple key boxes & save together" else "Stored locally & protected",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                }
            }

            // Mode Selector Pill Tabs (if creating new key)
            if (existingItem == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurfaceElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = { selectedTabMode = 0 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTabMode == 0) CyberGold else Color.Transparent
                    ) {
                        Text(
                            text = "Single Key + Extra Boxes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabMode == 0) Color(0xFF1E1400) else TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .testTag("tab_mode_single")
                        )
                    }
                    Surface(
                        onClick = { selectedTabMode = 1 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTabMode == 1) CyberGold else Color.Transparent
                    ) {
                        Text(
                            text = "Batch Multi-Key (${batchEntries.size} Boxes)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabMode == 1) Color(0xFF1E1400) else TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .testTag("tab_mode_batch")
                        )
                    }
                }
            }

            if (selectedTabMode == 1 && existingItem == null) {
                // Batch Multi-Key Box view
                BatchKeyBoxList(
                    batchEntries = batchEntries,
                    onAddBox = {
                        batchEntries = batchEntries + BatchKeyEntry(
                            title = "Key #${batchEntries.size + 1}",
                            provider = "Custom / Other"
                        )
                    },
                    onRemoveBox = { boxId ->
                        if (batchEntries.size > 1) {
                            batchEntries = batchEntries.filter { it.id != boxId }
                        }
                    },
                    onUpdateBox = { boxId, updated ->
                        batchEntries = batchEntries.map { if (it.id == boxId) updated else it }
                    },
                    clipboardManager = clipboardManager
                )
            } else {
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
                        val detectedPreset = ProviderPresets.findByName(detected)
                        if (endpointUrl.isEmpty() || endpointUrl == preset.defaultEndpoint) {
                            endpointUrl = detectedPreset.defaultEndpoint
                        }
                    }
                )

                // Base Endpoint URL (Positioned directly under API Key)
                OutlinedTextField(
                    value = endpointUrl,
                    onValueChange = { endpointUrl = it },
                    label = { Text("Base Endpoint URL", color = TextSecondary) },
                    placeholder = { Text(preset.defaultEndpoint.ifEmpty { "https://api.example.com" }, color = TextTertiary) },
                    singleLine = true,
                    textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp, color = TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_endpoint_url"),
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

                // Extra secret fields / boxes
                ExtraSecretFieldsSection(
                    extraFields = extraSecretFields,
                    onAddField = {
                        extraSecretFields = extraSecretFields + ExtraSecretField()
                    },
                    onRemoveField = { fieldId ->
                        extraSecretFields = extraSecretFields.filter { it.id != fieldId }
                    },
                    onUpdateField = { fieldId, newLabel, newValue, newVis ->
                        extraSecretFields = extraSecretFields.map {
                            if (it.id == fieldId) it.copy(label = newLabel, value = newValue, isVisible = newVis) else it
                        }
                    },
                    clipboardManager = clipboardManager
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

                Surface(
                    onClick = { showAdvancedSettings = !showAdvancedSettings },
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceElevated,
                    border = BorderStroke(1.dp, if (showAdvancedSettings) CyberGold else ObsidianBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("toggle_advanced_settings")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyberGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Advanced Settings",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (showAdvancedSettings) "Org ID, Tags, Developer Notes & Rotation" else "Tap to configure optional metadata",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = if (showAdvancedSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showAdvancedSettings) "Collapse" else "Expand",
                            tint = CyberGold
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(visible = showAdvancedSettings) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Org / Project ID
                        OutlinedTextField(
                            value = organizationId,
                            onValueChange = { organizationId = it },
                            label = { Text("Org / Project ID (Optional)", color = TextSecondary) },
                            singleLine = true,
                            textStyle = MonospaceCodeStyle.copy(fontSize = 13.sp, color = TextPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_org_id"),
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

                        // Tags
                        TagInputChipField(
                            tagsString = tags,
                            onTagsChange = { tags = it },
                            availableTags = availableTags
                        )

                        // Internal Developer Notes
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
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
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
