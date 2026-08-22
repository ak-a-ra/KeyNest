package com.example.feature.vault

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.CyberGoldLight
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary

@Composable
fun VaultTagFilterCarousel(
    tags: List<String>,
    selectedTag: String?,
    onlyFavorites: Boolean = false,
    favoritesCount: Int = 0,
    onTagSelected: (String) -> Unit,
    onClearTagFilter: () -> Unit,
    onToggleFavorites: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty() && favoritesCount == 0 && !onlyFavorites) return

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tag_filter_carousel"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favorites / Starred chip
        if (favoritesCount > 0 || onlyFavorites) {
            item {
                FilterChip(
                    selected = onlyFavorites,
                    onClick = onToggleFavorites,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CyberGold,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (favoritesCount > 0) "Starred ($favoritesCount)" else "Starred",
                            fontSize = 12.sp,
                            fontWeight = if (onlyFavorites) FontWeight.Bold else FontWeight.Medium,
                            color = if (onlyFavorites) CyberGold else TextPrimary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = ObsidianSurfaceElevated,
                        selectedContainerColor = CyberGold.copy(alpha = 0.22f),
                        labelColor = TextPrimary,
                        selectedLabelColor = CyberGold
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (onlyFavorites) CyberGold else ObsidianBorder.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("tag_filter_favorites")
                )
            }
        }

        // "Clear tag" chip if a tag is currently selected
        if (selectedTag != null) {
            item {
                FilterChip(
                    selected = true,
                    onClick = onClearTagFilter,
                    label = {
                        Text(
                            text = "Clear tag (#$selectedTag)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGold
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear tag filter",
                            tint = CyberGold,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberGold.copy(alpha = 0.18f),
                        selectedLabelColor = CyberGold
                    ),
                    border = BorderStroke(1.dp, CyberGold.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("tag_filter_clear")
                )
            }
        }

        items(tags, key = { it }) { tag ->
            val isSelected = tag.equals(selectedTag, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onTagSelected(tag) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = if (isSelected) CyberCyan else TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                },
                label = {
                    Text(
                        text = "#$tag",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyberCyan else TextPrimary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = ObsidianSurfaceElevated,
                    selectedContainerColor = CyberCyan.copy(alpha = 0.18f),
                    labelColor = TextPrimary,
                    selectedLabelColor = CyberCyan
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) CyberCyan.copy(alpha = 0.7f) else ObsidianBorder.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("tag_filter_chip_$tag")
            )
        }
    }
}
