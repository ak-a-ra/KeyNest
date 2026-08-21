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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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

@Composable
fun ProviderPresetCarousel(
    selectedProvider: String,
    onSelectProvider: (ProviderPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "PROVIDER TEMPLATE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 0.8.sp
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(ProviderPresets.list, key = { it.name }) { item ->
                val isSelected = item.name.equals(selectedProvider, ignoreCase = true)
                Surface(
                    onClick = { onSelectProvider(item) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) CyberGold.copy(alpha = 0.2f) else ObsidianSurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) CyberGold else ObsidianBorder
                    ),
                    modifier = Modifier.testTag("provider_chip_${item.name.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ProviderIconBadge(
                            provider = item.name,
                            colorHex = item.defaultColorHex,
                            size = 20
                        )
                        Text(
                            text = item.name,
                            color = if (isSelected) CyberGold else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
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
fun EnvironmentPickerRow(
    selectedEnvironment: String,
    onEnvironmentSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "ENVIRONMENT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 0.8.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProviderPresets.environments.forEach { env ->
                val isSelected = selectedEnvironment.equals(env, ignoreCase = true)
                Surface(
                    onClick = { onEnvironmentSelect(env) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) CyberGold.copy(alpha = 0.2f) else ObsidianSurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) CyberGold else ObsidianBorder
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = env.take(4).uppercase(),
                            color = if (isSelected) CyberGold else TextSecondary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryPickerRow(
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CATEGORY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 0.8.sp
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ProviderPresets.categories.filter { it != "All" }, key = { it }) { cat ->
                val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                Surface(
                    onClick = { onCategorySelect(cat) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) CyberEmerald.copy(alpha = 0.2f) else ObsidianSurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) CyberEmerald else ObsidianBorder
                    )
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) CyberEmerald else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
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
