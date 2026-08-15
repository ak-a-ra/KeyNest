package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeyItem
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberRose
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.ObsidianSurfaceHighlight
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Expiration and rotation lifecycle calculation state.
 */
enum class ExpirationAlertLevel {
    FRESH,
    APPROACHING_EXPIRY,
    OVERDUE
}

data class KeyRotationStatus(
    val level: ExpirationAlertLevel,
    val daysOld: Int,
    val totalRotationDays: Int,
    val daysRemaining: Int,
    val progress: Float, // 0.0f (brand new) to 1.0f (reached/exceeded expiry)
    val statusLabel: String,
    val detailMessage: String,
    val alertColor: Color,
    val alertIcon: ImageVector
)

@Composable
fun rememberKeyRotationStatus(item: ApiKeyItem): KeyRotationStatus {
    val successColor = StatusSuccess
    val warningColor = StatusWarning
    val dangerColor = StatusDanger

    return remember(item.createdAt, item.rotationDays, item.expiresAt, successColor, warningColor, dangerColor) {
        val now = System.currentTimeMillis()
        val totalRotationDays = item.rotationDays ?: 90
        val daysOld = ((now - item.createdAt) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        
        // If explicit expiresAt is provided, compare against that; otherwise use rotation cycle
        val daysRemaining = if (item.expiresAt != null && item.expiresAt > 0) {
            ((item.expiresAt - now) / (1000 * 60 * 60 * 24)).toInt()
        } else {
            totalRotationDays - daysOld
        }

        val progress = if (totalRotationDays > 0) {
            (daysOld.toFloat() / totalRotationDays.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        when {
            daysRemaining <= 0 -> {
                KeyRotationStatus(
                    level = ExpirationAlertLevel.OVERDUE,
                    daysOld = daysOld,
                    totalRotationDays = totalRotationDays,
                    daysRemaining = daysRemaining,
                    progress = 1.0f,
                    statusLabel = "Rotation Overdue",
                    detailMessage = if (daysRemaining == 0) "Expired today. Replace key immediately." else "Overdue by ${-daysRemaining} days (${daysOld}d total age). Stale keys increase leak vulnerability.",
                    alertColor = dangerColor,
                    alertIcon = Icons.Default.Error
                )
            }
            daysRemaining <= 15 -> {
                KeyRotationStatus(
                    level = ExpirationAlertLevel.APPROACHING_EXPIRY,
                    daysOld = daysOld,
                    totalRotationDays = totalRotationDays,
                    daysRemaining = daysRemaining,
                    progress = progress,
                    statusLabel = "Rotation Approaching",
                    detailMessage = "$daysRemaining days remaining in $totalRotationDays-day cycle. Prepare replacement key in staging.",
                    alertColor = warningColor,
                    alertIcon = Icons.Default.HourglassTop
                )
            }
            else -> {
                KeyRotationStatus(
                    level = ExpirationAlertLevel.FRESH,
                    daysOld = daysOld,
                    totalRotationDays = totalRotationDays,
                    daysRemaining = daysRemaining,
                    progress = progress,
                    statusLabel = "Key Fresh & Healthy",
                    detailMessage = "$daysRemaining days remaining until next scheduled rotation (${daysOld}d active).",
                    alertColor = successColor,
                    alertIcon = Icons.Default.CheckCircle
                )
            }
        }
    }
}

/**
 * Full Material 3 Card component for Key Expiration Status & Rotation Tracker
 */
@Composable
fun KeyExpirationStatusCard(
    item: ApiKeyItem,
    modifier: Modifier = Modifier,
    onRotateClick: (() -> Unit)? = null
) {
    val status = rememberKeyRotationStatus(item)

    val animatedBarColor by animateColorAsState(
        targetValue = status.alertColor,
        animationSpec = tween(300),
        label = "status_bar_color"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = status.progress,
        animationSpec = tween(400),
        label = "status_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("key_expiration_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ObsidianSurfaceElevated
        ),
        border = BorderStroke(
            width = 1.2.dp,
            color = status.alertColor.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Color-Coded Icon & Alert Badge
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(status.alertColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = status.alertIcon,
                            contentDescription = status.statusLabel,
                            tint = status.alertColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "KEY ROTATION TRACKER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = status.statusLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = status.alertColor
                        )
                    }
                }

                // Days pill indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = status.alertColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, status.alertColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (status.daysRemaining <= 0) "${-status.daysRemaining}d OVERDUE" else "${status.daysRemaining}d LEFT",
                        color = status.alertColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Progress Bar Visualizing Key Lifecycle Age vs Expiration
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Lifecycle: ${status.daysOld}d / ${status.totalRotationDays}d",
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(status.progress * 100).toInt()}% elapsed",
                        fontSize = 11.5.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = animatedBarColor,
                    trackColor = ObsidianSurfaceHighlight
                )
            }

            // Contextual Alert & Recommendation Details
            Text(
                text = status.detailMessage,
                fontSize = 12.5.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )

            // Optional Quick Rotation CTA Button
            if (onRotateClick != null) {
                OutlinedButton(
                    onClick = onRotateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("button_rotate_key_${item.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (status.level == ExpirationAlertLevel.OVERDUE) StatusDanger else TextPrimary
                    ),
                    border = BorderStroke(1.dp, status.alertColor.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = status.alertColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (status.level == ExpirationAlertLevel.OVERDUE) "Rotate Key Immediately" else "Update / Rotate Secret",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Compact Chip / Badge for List and Grid views
 */
@Composable
fun KeyExpirationBadge(
    item: ApiKeyItem,
    modifier: Modifier = Modifier
) {
    val status = rememberKeyRotationStatus(item)

    Surface(
        modifier = modifier.testTag("key_expiration_badge_${item.id}"),
        shape = RoundedCornerShape(6.dp),
        color = status.alertColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, status.alertColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = status.alertIcon,
                contentDescription = null,
                tint = status.alertColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (status.daysRemaining <= 0) "Overdue (${-status.daysRemaining}d)"
                       else if (status.daysRemaining <= 15) "${status.daysRemaining}d left"
                       else "Fresh (${status.daysRemaining}d)",
                color = status.alertColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
