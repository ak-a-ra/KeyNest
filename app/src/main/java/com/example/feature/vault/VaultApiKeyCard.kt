package com.example.feature.vault
import com.example.feature.keymanagement.KeyExpirationBadge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.example.core.model.ApiKeyItem
import com.example.core.security.VaultSecurity
import com.example.core.util.ApiKeyFormatting
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.LocalKeyNestColors
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBg
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceHighlight
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextTertiary
import com.example.core.designsystem.VaultCardColor

/**
 * Data clump refactoring: bundles key item actions cleanly.
 */
data class KeyCardActions(
    val onClick: (ApiKeyItem) -> Unit,
    val onCopy: (ApiKeyItem) -> Unit,
    val onTogglePin: (ApiKeyItem) -> Unit,
    val onTagClick: ((String) -> Unit)? = null
)

@Composable
fun ApiKeyCard(
    item: ApiKeyItem,
    actions: KeyCardActions,
    modifier: Modifier = Modifier
) {
    val isDark = LocalKeyNestColors.current.isDark
    val cardColor = remember(item.colorHex, isDark) {
        val matchedPalette = VaultCardColor.fromHex(item.colorHex)
        if (matchedPalette != VaultCardColor.DEFAULT) {
            Color(if (isDark) matchedPalette.darkBg else matchedPalette.lightBg)
        } else if (!item.colorHex.isNullOrBlank()) {
            try {
                val parsed = Color(android.graphics.Color.parseColor(item.colorHex))
                parsed.copy(alpha = if (isDark) 0.22f else 0.15f)
            } catch (_: Exception) {
                if (isDark) Color(0xFF202124) else Color(0xFFFFFFFF)
            }
        } else {
            if (isDark) Color(0xFF202124) else Color(0xFFFFFFFF)
        }
    }

    Surface(
        onClick = { actions.onClick(item) },
        modifier = modifier
            .fillMaxWidth()
            .testTag("key_card_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (item.isPinned) CyberGold else ObsidianBorder.copy(alpha = 0.6f)
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
                            text = item.provider.uppercase(),
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
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("pin_key_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (item.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (item.isPinned) "Unpin ${item.title}" else "Pin ${item.title}",
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

            // Tags Pill Row (if tags exist)
            val parsedTags = remember(item.tags) { ApiKeyFormatting.parseTags(item.tags) }
            if (parsedTags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    parsedTags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ObsidianSurfaceHighlight)
                                .clickable(enabled = actions.onTagClick != null) {
                                    actions.onTagClick?.invoke(tag)
                                }
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.5.sp,
                                color = CyberCyan,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (parsedTags.size > 3) {
                        Text(
                            text = "+${parsedTags.size - 3}",
                            fontSize = 10.5.sp,
                            color = TextTertiary
                        )
                    }
                }
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
