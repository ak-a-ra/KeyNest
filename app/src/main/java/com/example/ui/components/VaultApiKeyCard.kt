package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeyItem
import com.example.data.security.VaultSecurity
import com.example.ui.theme.CyberGold
import com.example.ui.theme.LocalKeyNestColors
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary

/**
 * Data clump refactoring: bundles key item actions cleanly.
 */
data class KeyCardActions(
    val onClick: (ApiKeyItem) -> Unit,
    val onCopy: (ApiKeyItem) -> Unit,
    val onTogglePin: (ApiKeyItem) -> Unit
)

@Composable
fun ApiKeyCard(
    item: ApiKeyItem,
    actions: KeyCardActions,
    modifier: Modifier = Modifier
) {
    val isDark = LocalKeyNestColors.current.isDark
    val cardColor = remember(item.colorHex, isDark) {
        try {
            val baseColor = Color(android.graphics.Color.parseColor(item.colorHex))
            baseColor.copy(alpha = 0.15f)
        } catch (_: Exception) {
            Color.Transparent
        }
    }

    Surface(
        onClick = { actions.onClick(item) },
        modifier = modifier
            .fillMaxWidth()
            .testTag("key_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        color = if (cardColor == Color.Transparent) ObsidianBg else cardColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isPinned) CyberGold else ObsidianBorder.copy(alpha = 0.5f)
        ),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Header
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
                    ProviderIconBadge(
                        provider = item.provider,
                        colorHex = item.colorHex,
                        size = 40
                    )
                    Column {
                        Text(
                            text = "${item.provider.uppercase()} ${item.environment.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGold,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = item.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { actions.onTogglePin(item) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Pin Key",
                            tint = if (item.isPinned) CyberGold else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Key Masked Row & 1-Tap Tactile Copy Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurfaceHighlight)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = VaultSecurity.maskKey(item.apiKey, 4),
                    style = MonospaceCodeStyle.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                TactileCopyButton(
                    onCopy = { actions.onCopy(item) },
                    label = "Copy"
                )
            }

            // Footer info (Copied X times, color-coded rotation status badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.copyCount > 0) "Copied ${item.copyCount}x" else "Never copied",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
                KeyExpirationBadge(item = item)
            }
        }
    }
}
