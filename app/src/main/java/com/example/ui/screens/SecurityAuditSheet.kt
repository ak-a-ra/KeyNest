package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeyItem
import com.example.data.security.VaultSecurity
import com.example.ui.components.KeyExpirationStatusCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianBorderLight
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAuditSheet(
    keys: List<ApiKeyItem>,
    isPinEnabled: Boolean,
    onDismiss: () -> Unit,
    onSelectKey: (ApiKeyItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val now = System.currentTimeMillis()

    val expiredKeys = remember(keys) {
        keys.filter {
            val daysOld = ((now - it.createdAt) / (1000 * 60 * 60 * 24)).toInt()
            daysOld >= (it.rotationDays ?: 90)
        }
    }

    val weakKeys = remember(keys) {
        keys.filter { VaultSecurity.calculateEntropy(it.apiKey).entropyBits < 45 }
    }

    val healthScore = remember(keys, expiredKeys, weakKeys, isPinEnabled) {
        if (keys.isEmpty()) 100
        else {
            var score = 100
            if (!isPinEnabled) score -= 15
            score -= (expiredKeys.size * 10).coerceAtMost(30)
            score -= (weakKeys.size * 15).coerceAtMost(30)
            score.coerceIn(20, 100)
        }
    }

    val scoreColor = if (healthScore > 80) CyberEmerald else if (healthScore > 50) CyberGold else StatusDanger

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
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberGold.copy(alpha = 0.2f))
                            .border(1.dp, CyberGold, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Vault Security Health Audit",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Comprehensive key hygiene & rotation diagnostics",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                }
            }

            // Score Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, scoreColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OVERALL SECURITY SCORE",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (healthScore > 80) "Fortified & Compliant" else if (healthScore > 50) "Attention Recommended" else "Action Required",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { healthScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = scoreColor,
                            trackColor = ObsidianBorder
                        )
                    }

                    Text(
                        text = "$healthScore%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = scoreColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Key Audit Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuditStatCard(
                    modifier = Modifier.weight(1f),
                    value = "${expiredKeys.size}",
                    label = "Rotation Due",
                    color = if (expiredKeys.isNotEmpty()) StatusWarning else TextPrimary,
                    borderColor = if (expiredKeys.isNotEmpty()) StatusWarning else ObsidianBorder
                )
                AuditStatCard(
                    modifier = Modifier.weight(1f),
                    value = "${weakKeys.size}",
                    label = "Low Entropy",
                    color = if (weakKeys.isNotEmpty()) StatusDanger else TextPrimary,
                    borderColor = if (weakKeys.isNotEmpty()) StatusDanger else ObsidianBorder
                )
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianSurfaceElevated,
                    border = BorderStroke(1.dp, if (isPinEnabled) CyberEmerald else StatusWarning)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = if (isPinEnabled) Icons.Default.Lock else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isPinEnabled) CyberEmerald else StatusWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isPinEnabled) "PIN Active" else "PIN Disabled",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // List of Keys Needing Attention
            if (expiredKeys.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "KEYS DUE FOR ROTATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusDanger,
                        letterSpacing = 0.8.sp
                    )
                    expiredKeys.forEach { item ->
                        KeyExpirationStatusCard(
                            item = item,
                            onRotateClick = {
                                onDismiss()
                                onSelectKey(item)
                            }
                        )
                    }
                }
            }

            // Developer Best Practices Guide
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianSurfaceElevated)
                    .border(1.dp, ObsidianBorderLight, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "KEYNEST SECURITY RECOMMENDATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 0.6.sp
                )
                listOf(
                    "Never store raw API keys in public unencrypted Google Notes.",
                    "Rotate production keys every 60-90 days to prevent stale access leaks.",
                    "Use distinct keys for Production, Staging, and Local Development.",
                    "Set a 4-6 digit KeyNest PIN to protect vault access from physical intruders."
                ).forEach { tip ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                        )
                        Text(tip, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditStatCard(
    value: String,
    label: String,
    color: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = ObsidianSurfaceElevated,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
