package com.example.feature.keymanagement
import com.example.feature.vault.EntropyStrengthBar
import com.example.feature.vault.ProviderIconBadge

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ProviderPreset
import com.example.core.model.ProviderPresets
import com.example.core.security.VaultSecurity
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.LocalKeyNestColors
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.TextMuted
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import com.example.core.designsystem.VaultCardColor

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.example.core.designsystem.CyberCyan
import com.example.core.util.ApiKeyFormatting

@Composable
fun ProviderPresetCarousel(
    selectedProvider: String,
    onSelectProvider: (ProviderPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownSelectorField(
        label = "PROVIDER TEMPLATE",
        selectedValue = selectedProvider,
        options = ProviderPresets.list.map { it.name },
        onSelect = { selected ->
            ProviderPresets.list.find { it.name == selected }?.let { onSelectProvider(it) }
        },
        modifier = modifier,
        icon = {
            val preset = ProviderPresets.findByName(selectedProvider)
            ProviderIconBadge(
                provider = selectedProvider,
                colorHex = preset.defaultColorHex,
                size = 20
            )
        },
        optionIcon = { option ->
            val preset = ProviderPresets.findByName(option)
            ProviderIconBadge(
                provider = option,
                colorHex = preset.defaultColorHex,
                size = 20
            )
        }
    )
}

@Composable
fun ApiKeyInputSection(
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    placeholderKey: String,
    isKeyVisible: Boolean,
    onToggleVisibility: () -> Unit,
    clipboardManager: ClipboardManager,
    onProviderDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        OutlinedTextField(
            value = apiKey,
            onValueChange = { newValue ->
                onApiKeyChange(newValue)
                if (newValue.length >= 2) {
                    val detected = VaultSecurity.detectProviderFromKey(newValue)
                    if (detected != "Custom / Other") {
                        onProviderDetected(detected)
                    }
                }
            },
            label = { Text("API Key / Token *", color = TextSecondary) },
            placeholder = { Text(placeholderKey, color = TextMuted, fontSize = 12.sp) },
            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            textStyle = MonospaceCodeStyle.copy(color = CyberEmerald),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_api_key"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberEmerald,
                unfocusedBorderColor = ObsidianBorder,
                focusedTextColor = CyberEmerald,
                unfocusedTextColor = CyberEmerald,
                focusedContainerColor = ObsidianSurfaceElevated,
                unfocusedContainerColor = ObsidianSurfaceElevated
            ),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility",
                            tint = if (isKeyVisible) CyberGold else TextTertiary
                        )
                    }
                    IconButton(
                        onClick = {
                            clipboardManager.getText()?.text?.let {
                                val pasted = it.trim()
                                onApiKeyChange(pasted)
                                if (pasted.isNotEmpty()) {
                                    val detected = VaultSecurity.detectProviderFromKey(pasted)
                                    if (detected != "Custom / Other") {
                                        onProviderDetected(detected)
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste from Clipboard",
                            tint = CyberGold
                        )
                    }
                }
            }
        )

        // Live Provider Auto-Detection Badge Indicator
        val detectedLiveProvider = remember(apiKey) {
            if (apiKey.length >= 2) VaultSecurity.detectProviderFromKey(apiKey) else null
        }
        AnimatedVisibility(
            visible = detectedLiveProvider != null && detectedLiveProvider != "Custom / Other",
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
        ) {
            detectedLiveProvider?.let { provider ->
                val p = ProviderPresets.findByName(provider)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberEmerald.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.35f)),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProviderIconBadge(provider = provider, colorHex = p.defaultColorHex, size = 18)
                        Text(
                            text = "✨ Auto-detected: $provider (${p.category})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberEmerald,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        if (apiKey.isNotBlank()) {
            EntropyStrengthBar(apiKey = apiKey)
        }
    }
}

@Composable
fun CategoryPickerRow(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownSelectorField(
        label = "CATEGORY",
        selectedValue = selectedCategory,
        options = ProviderPresets.categories.filter { it != "All" },
        onSelect = onCategorySelect,
        modifier = modifier
    )
}

@Composable
fun RotationIntervalPicker(
    rotationDays: Int,
    onRotationDaysChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianSurfaceElevated)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Rotation Reminder Interval",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Alerts when key is older than $rotationDays days",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(30, 60, 90).forEach { days ->
                val isSelected = rotationDays == days
                Surface(
                    onClick = { onRotationDaysChange(days) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) CyberGold else ObsidianSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) CyberGold else ObsidianBorder
                    )
                ) {
                    Text(
                        text = "${days}d",
                        color = if (isSelected) Color(0xFF1E1400) else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun VaultColorDotPicker(
    selectedColorHex: String?,
    onSelectColor: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CARD COLOR TINT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 0.8.sp
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(VaultCardColor.entries, key = { it.label }) { colorOption ->
                val isSelected = (colorOption.hex == null && (selectedColorHex == null || selectedColorHex.isEmpty())) ||
                        (colorOption.hex != null && colorOption.hex.equals(selectedColorHex, ignoreCase = true))
                val isDark = LocalKeyNestColors.current.isDark
                val dotColor = if (colorOption.hex == null) {
                    if (isDark) Color(0xFF3C4043) else Color(0xFFE8EAED)
                } else {
                    Color(if (isDark) colorOption.darkBg else colorOption.lightBg)
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) CyberGold else ObsidianBorder,
                            shape = CircleShape
                        )
                        .clickable { onSelectColor(colorOption.hex) }
                        .testTag("color_dot_${colorOption.label.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "${colorOption.label} selected",
                            tint = if (isDark) Color.White else Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagInputChipField(
    tagsString: String,
    onTagsChange: (String) -> Unit,
    availableTags: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var tagInputValue by remember { mutableStateOf("") }
    val currentTags = remember(tagsString) { ApiKeyFormatting.parseTags(tagsString) }

    val addTag: (String) -> Unit = { rawText ->
        val cleaned = rawText.trim().removePrefix("#").removePrefix("tag:").trim()
        if (cleaned.isNotBlank() && !currentTags.any { it.equals(cleaned, ignoreCase = true) }) {
            val updated = currentTags + cleaned.take(20)
            onTagsChange(ApiKeyFormatting.formatTags(updated))
        }
        tagInputValue = ""
    }

    val removeTag: (String) -> Unit = { tagToRemove ->
        val updated = currentTags.filterNot { it.equals(tagToRemove, ignoreCase = true) }
        onTagsChange(ApiKeyFormatting.formatTags(updated))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "TAGS & WORKSPACES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 0.8.sp
        )

        // Tag input text field
        OutlinedTextField(
            value = tagInputValue,
            onValueChange = { input ->
                if (input.endsWith(",") || input.endsWith("\n") || input.endsWith(" ")) {
                    val token = input.dropLast(1).trim()
                    if (token.isNotEmpty()) {
                        addTag(token)
                    } else {
                        tagInputValue = ""
                    }
                } else {
                    tagInputValue = input
                }
            },
            label = { Text("Add tags (press space or comma)", color = TextSecondary) },
            placeholder = { Text("e.g. prod, client-x, billing, staging", color = TextMuted, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_tag_field"),
            trailingIcon = {
                if (tagInputValue.isNotBlank()) {
                    IconButton(
                        onClick = { addTag(tagInputValue) },
                        modifier = Modifier.testTag("button_add_tag_chip")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Tag",
                            tint = CyberCyan
                        )
                    }
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
            shape = RoundedCornerShape(12.dp)
        )

        // Active tags chips (FlowRow)
        if (currentTags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentTags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = { removeTag(tag) },
                        label = {
                            Text(
                                text = "#$tag",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberCyan
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove tag $tag",
                                tint = CyberCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.15f),
                            selectedLabelColor = CyberCyan
                        ),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("tag_chip_$tag")
                    )
                }
            }
        }

        // Suggestions from existing vault tags
        val suggestions = remember(availableTags, currentTags) {
            availableTags.filter { existing -> !currentTags.any { it.equals(existing, ignoreCase = true) } }.take(6)
        }
        if (suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Suggestions:",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestions) { suggestion ->
                        Surface(
                            onClick = { addTag(suggestion) },
                            shape = RoundedCornerShape(6.dp),
                            color = ObsidianSurfaceElevated,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Text(
                                text = "+ #$suggestion",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ExtraSecretField(
    val id: String = java.util.UUID.randomUUID().toString(),
    var label: String = "",
    var value: String = "",
    var isVisible: Boolean = false
)

data class BatchKeyEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "",
    var apiKey: String = "",
    var provider: String = "OpenAI",
    var category: String = "AI & LLMs",
    var isKeyVisible: Boolean = false
)

@Composable
fun ExtraSecretFieldsSection(
    extraFields: List<ExtraSecretField>,
    onAddField: () -> Unit,
    onRemoveField: (String) -> Unit,
    onUpdateField: (String, String, String, Boolean) -> Unit,
    clipboardManager: ClipboardManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EXTRA SECRET FIELDS / BOXES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Add custom secrets (Client Secret, Private Key, Webhook)",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            androidx.compose.material3.OutlinedButton(
                onClick = onAddField,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CyberGold.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("button_add_extra_field_box")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = CyberGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("+ Add Field Box", fontSize = 11.5.sp, color = CyberGold, fontWeight = FontWeight.Bold)
            }
        }

        extraFields.forEachIndexed { index, field ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Secret Box #${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGold,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(
                            onClick = { onRemoveField(field.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Box",
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = field.label,
                        onValueChange = { newLabel ->
                            onUpdateField(field.id, newLabel, field.value, field.isVisible)
                        },
                        label = { Text("Field Label / Name", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("e.g. Webhook Secret, Private Key", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ObsidianSurface,
                            unfocusedContainerColor = ObsidianSurface
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = field.value,
                        onValueChange = { newValue ->
                            onUpdateField(field.id, field.label, newValue, field.isVisible)
                        },
                        label = { Text("Secret Value", color = TextSecondary, fontSize = 11.sp) },
                        visualTransformation = if (field.isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp, color = CyberEmerald),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberEmerald,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = CyberEmerald,
                            unfocusedTextColor = CyberEmerald,
                            focusedContainerColor = ObsidianSurface,
                            unfocusedContainerColor = ObsidianSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        onUpdateField(field.id, field.label, field.value, !field.isVisible)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (field.isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Visibility",
                                        tint = if (field.isVisible) CyberGold else TextTertiary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.getText()?.text?.let {
                                            onUpdateField(field.id, field.label, it.toString().trim(), field.isVisible)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste",
                                        tint = CyberGold
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BatchKeyBoxList(
    batchEntries: List<BatchKeyEntry>,
    onAddBox: () -> Unit,
    onRemoveBox: (String) -> Unit,
    onUpdateBox: (String, BatchKeyEntry) -> Unit,
    clipboardManager: ClipboardManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BATCH MULTI-KEY ADDITION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Fill multiple key boxes & save all in one tap",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyberGold.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, CyberGold.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "${batchEntries.size} Key Boxes",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        batchEntries.forEachIndexed { index, entry ->
            val preset = ProviderPresets.findByName(entry.provider)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batch_box_card_$index"),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProviderIconBadge(
                                provider = entry.provider,
                                colorHex = preset.defaultColorHex,
                                size = 24
                            )
                            Text(
                                text = "API KEY BOX #${index + 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (batchEntries.size > 1) {
                            IconButton(
                                onClick = { onRemoveBox(entry.id) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("button_remove_batch_box_$index")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Key Box",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Provider selector carousel for this box
                    ProviderPresetCarousel(
                        selectedProvider = entry.provider,
                        onSelectProvider = { item ->
                            val updatedTitle = if (entry.title.isBlank() || entry.title.endsWith("Key")) "${item.name} Key" else entry.title
                            onUpdateBox(
                                entry.id,
                                entry.copy(
                                    provider = item.name,
                                    title = updatedTitle,
                                    category = item.category
                                )
                            )
                        }
                    )

                    // Title Input
                    OutlinedTextField(
                        value = entry.title,
                        onValueChange = { newTitle ->
                            onUpdateBox(entry.id, entry.copy(title = newTitle))
                        },
                        label = { Text("Name / Description *", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("e.g. ${entry.provider} Staging Key", color = TextMuted, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_batch_title_$index"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberGold,
                            unfocusedBorderColor = ObsidianBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = ObsidianSurface,
                            unfocusedContainerColor = ObsidianSurface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // API Key Input
                    ApiKeyInputSection(
                        apiKey = entry.apiKey,
                        onApiKeyChange = { newKey ->
                            val detected = if (newKey.length >= 2) VaultSecurity.detectProviderFromKey(newKey) else entry.provider
                            val finalProvider = if (detected != "Custom / Other") detected else entry.provider
                            val finalTitle = if (entry.title.isBlank() || entry.title.endsWith("Key")) "$finalProvider Key" else entry.title
                            onUpdateBox(
                                entry.id,
                                entry.copy(
                                    apiKey = newKey,
                                    provider = finalProvider,
                                    title = finalTitle
                                )
                            )
                        },
                        placeholderKey = preset.placeholderKey,
                        isKeyVisible = entry.isKeyVisible,
                        onToggleVisibility = {
                            onUpdateBox(entry.id, entry.copy(isKeyVisible = !entry.isKeyVisible))
                        },
                        clipboardManager = clipboardManager,
                        onProviderDetected = { detected ->
                            val finalTitle = if (entry.title.isBlank() || entry.title.endsWith("Key")) "$detected Key" else entry.title
                            onUpdateBox(
                                entry.id,
                                entry.copy(provider = detected, title = finalTitle)
                            )
                        }
                    )
                }
            }
        }

        // Button to add another API key box
        Surface(
            onClick = onAddBox,
            shape = RoundedCornerShape(12.dp),
            color = ObsidianSurfaceElevated,
            border = BorderStroke(1.dp, CyberGold.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("button_add_another_key_box")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = CyberGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "+ Add Another API Key Box",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGold
                )
            }
        }
    }
}


