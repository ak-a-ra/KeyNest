package com.example.feature.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianBorderLight
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.ObsidianSurfaceHighlight
import com.example.core.designsystem.StatusDanger
import com.example.core.designsystem.StatusSuccess
import com.example.core.designsystem.StatusWarning
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import com.example.core.model.ProviderPreset
import com.example.core.model.ProviderProfile
import com.example.core.network.ConnectionResult
import com.example.core.security.VaultSecurity

@Composable
fun ProviderCard(
    profile: ProviderProfile,
    preset: ProviderPreset,
    connectionState: ConnectionResult? = null,
    isTesting: Boolean = false,
    onCardClick: () -> Unit,
    onTestConnection: () -> Unit,
    onCopyActiveKey: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onConfigure: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fallbackColor = CyberGold
    val brandColor = remember(preset.defaultColorHex, profile.colorHex, fallbackColor) {
        try {
            Color(android.graphics.Color.parseColor(preset.defaultColorHex.ifBlank { profile.colorHex }))
        } catch (_: Exception) {
            fallbackColor
        }
    }

    val isConfigured = profile.isConfigured
    val activeKey = profile.activeKey
    val maskedToken = remember(isConfigured, activeKey?.apiKey) {
        if (isConfigured && activeKey != null) {
            VaultSecurity.maskKey(activeKey.apiKey)
        } else {
            "Not configured"
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurfaceElevated,
        border = BorderStroke(
            1.dp,
            if (profile.isActive && isConfigured) ObsidianBorderLight else ObsidianBorder
        ),
        shadowElevation = if (profile.isActive) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Brand badge, Name, Category chip & Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Brand Icon Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(brandColor.copy(alpha = 0.16f))
                            .border(1.dp, brandColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.displayName.take(2).uppercase(),
                            color = brandColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = profile.displayName,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = profile.category,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 11.sp,
                                color = TextTertiary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Active toggle
                Switch(
                    checked = profile.isActive,
                    onCheckedChange = onToggleActive,
                    modifier = Modifier.size(width = 44.dp, height = 24.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyberEmerald,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = ObsidianSurfaceHighlight
                    )
                )
            }

            // Endpoint Pill
            val endpointUrl = profile.baseUrl.ifBlank { preset.defaultEndpoint }
            if (endpointUrl.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ObsidianSurfaceHighlight,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (profile.isActive) CyberEmerald else TextTertiary)
                        )
                        Text(
                            text = endpointUrl,
                            style = MonospaceCodeStyle.copy(
                                fontSize = 11.sp,
                                color = TextSecondary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Status & Key Summary Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Key indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = if (isConfigured) CyberGold else TextTertiary,
                        modifier = Modifier.size(15.dp)
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isConfigured) {
                                    "${profile.totalKeysCount} ${if (profile.totalKeysCount == 1) "Key" else "Keys"}"
                                } else {
                                    "No Keys"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isConfigured) TextPrimary else TextTertiary
                            )

                            if (isConfigured && activeKey != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CyberEmerald.copy(alpha = 0.12f),
                                    border = BorderStroke(0.5.dp, CyberEmerald.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = activeKey.label,
                                        color = CyberEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = maskedToken,
                            style = MonospaceCodeStyle.copy(
                                fontSize = 11.sp,
                                color = if (isConfigured) TextSecondary else TextTertiary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Quick Action Buttons (Ping & Copy & Configure)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Ping Live Status Badge / Button
                    if (isTesting) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ObsidianSurfaceHighlight,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(12.dp),
                                    color = CyberCyan
                                )
                                Text("Ping...", fontSize = 10.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (connectionState != null) {
                        val (bgColor, textColor, text, icon) = when (connectionState) {
                            is ConnectionResult.Success -> Quadruple(
                                StatusSuccess.copy(alpha = 0.15f),
                                StatusSuccess,
                                "${connectionState.latencyMs}ms",
                                Icons.Default.CheckCircle
                            )
                            is ConnectionResult.Failure -> Quadruple(
                                StatusDanger.copy(alpha = 0.15f),
                                StatusDanger,
                                "HTTP ${connectionState.statusCode}",
                                Icons.Default.Warning
                            )
                            is ConnectionResult.Error -> Quadruple(
                                StatusWarning.copy(alpha = 0.15f),
                                StatusWarning,
                                "Err",
                                Icons.Default.Error
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = bgColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onTestConnection() }
                                .padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
                                Text(text, fontSize = 10.sp, color = textColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onTestConnection,
                            modifier = Modifier.size(32.dp).testTag("btn_ping_${profile.id}")
                        ) {
                            Icon(
                                Icons.Default.NetworkCheck,
                                contentDescription = "Ping Provider",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Copy Active Key
                    if (isConfigured) {
                        IconButton(
                            onClick = onCopyActiveKey,
                            modifier = Modifier.size(32.dp).testTag("btn_copy_${profile.id}")
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Active Key",
                                tint = CyberEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Configure Profile
                    IconButton(
                        onClick = onConfigure,
                        modifier = Modifier.size(32.dp).testTag("btn_config_${profile.id}")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configure Profile",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
