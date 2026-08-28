package com.example.feature.search
import com.example.feature.keymanagement.AddEditKeySheet
import com.example.feature.keymanagement.KeyDetailSheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.model.ApiKeyItem
import com.example.core.model.ProviderPresets
import com.example.feature.vault.ApiKeyCard
import com.example.feature.vault.KeyCardActions
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.CyberRose
import com.example.core.designsystem.LocalKeyNestColors
import com.example.core.designsystem.ObsidianBg
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.ObsidianSurfaceHighlight
import com.example.core.designsystem.TextMuted
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.VaultCardColor
import com.example.core.designsystem.VibrantAvatarBg
import com.example.core.designsystem.VibrantButtonBg
import com.example.feature.vault.VaultDialogState
import com.example.feature.vault.VaultViewModel

@Composable
fun SearchScreen(
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit
) {
    val allKeys by viewModel.allKeys.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val filteredKeys by viewModel.filteredKeys.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val isDark = LocalKeyNestColors.current.isDark

    val cardActions = remember(viewModel) {
        KeyCardActions(
            onClick = { item -> viewModel.openDialog(VaultDialogState.KeyDetail(item)) },
            onCopy = { item ->
                viewModel.copyToClipboard(item.apiKey, "${item.title} API Key", isSecret = true, itemId = item.id)
            },
            onTogglePin = { item -> viewModel.togglePin(item) },
            onTagClick = { tag -> viewModel.toggleTagFilter(tag) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
    ) {
        // Top Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.clearSearch()
                    onNavigateBack()
                },
                modifier = Modifier.testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = ObsidianSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search keys, tags, providers...",
                                color = TextSecondary,
                                fontSize = 15.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_input_field"),
                            singleLine = true,
                            textStyle = TextStyle(color = TextPrimary, fontSize = 15.sp),
                            cursorBrush = SolidColor(CyberGold)
                        )
                    }
                    if (isSearching) {
                        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = CyberCyan,
                                strokeWidth = 2.dp
                            )
                        }
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearSearch() },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Live Search Results or Discovery Explorer
        if (searchQuery.isNotBlank()) {
            // Header with match count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SEARCH RESULTS",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceHighlight
                ) {
                    Text(
                        text = "${filteredKeys.size} found",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (filteredKeys.isEmpty()) {
                // Empty search results state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = ObsidianSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = CyberRose,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Text(
                            text = "No matching secrets found",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "No keys match \"$searchQuery\". Try searching by title, provider name, or tag (#tag).",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { viewModel.clearSearch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ObsidianSurfaceElevated,
                                contentColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Text("Clear Search Query", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredKeys, key = { it.id }) { item ->
                        ApiKeyCard(
                            item = item,
                            actions = cardActions,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            // Google Keep-style Explorer Hub when query is blank
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Category Types
                item {
                    Column {
                        SearchSectionHeader(title = "Types & Categories")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(ProviderPresets.categories) { category ->
                                SearchCategoryItem(
                                    label = category,
                                    icon = getCategoryIcon(category),
                                    onClick = {
                                        viewModel.setSearchQuery(category)
                                    }
                                )
                            }
                        }
                    }
                }

                // Vault Dynamic Tags
                if (availableTags.isNotEmpty()) {
                    item {
                        Column {
                            SearchSectionHeader(title = "Vault Tags")
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(availableTags) { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = ObsidianSurfaceElevated,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder.copy(alpha = 0.6f)),
                                        modifier = Modifier.clickable {
                                            viewModel.setSearchQuery("#$tag")
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalOffer,
                                                contentDescription = null,
                                                tint = CyberGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "#$tag",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Keep Pastel Color Palette Filters
                item {
                    Column {
                        SearchSectionHeader(title = "Color Tints")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(VaultCardColor.entries) { colorItem ->
                                val swatchColor = if (colorItem == VaultCardColor.DEFAULT) {
                                    if (isDark) Color(0xFF202124) else Color(0xFFFFFFFF)
                                } else {
                                    Color(if (isDark) colorItem.darkBg else colorItem.lightBg)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        if (colorItem.hex != null) {
                                            viewModel.setSearchQuery(colorItem.label)
                                        }
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(swatchColor)
                                            .border(1.dp, ObsidianBorder.copy(alpha = 0.7f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (colorItem == VaultCardColor.DEFAULT) {
                                            Icon(
                                                imageVector = Icons.Default.FormatColorReset,
                                                contentDescription = "Default Color",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = colorItem.label,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheets for Dialogs triggered from SearchScreen
    when (val state = dialogState) {
        is VaultDialogState.KeyDetail -> {
            KeyDetailSheet(
                item = state.item,
                onDismiss = { viewModel.closeDialog() },
                onEdit = { viewModel.openDialog(VaultDialogState.EditKey(it)) },
                onDelete = { viewModel.deleteKey(it) },
                onTogglePin = { viewModel.togglePin(it) },
                onCopyKey = { text, label, id ->
                    viewModel.copyToClipboard(text, label, isSecret = true, itemId = id)
                }
            )
        }
        is VaultDialogState.EditKey -> {
            val existingTitles = remember(allKeys, state.item.id) {
                allKeys.filter { it.id != state.item.id }.map { it.title }
            }
            AddEditKeySheet(
                existingItem = state.item,
                existingTitles = existingTitles,
                availableTags = availableTags,
                onDismiss = { viewModel.closeDialog() },
                onSave = { viewModel.saveKey(it) }
            )
        }
        else -> Unit
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SearchCategoryItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = ObsidianSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorder.copy(alpha = 0.6f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = CyberCyan,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getCategoryIcon(category: String): ImageVector = when (category) {
    "AI & LLMs" -> Icons.Default.Api
    "Cloud & Infra" -> Icons.Default.Cloud
    "Payments" -> Icons.Default.Payment
    "Auth & Security" -> Icons.Default.Security
    "Dev Tools" -> Icons.Default.Code
    "Database" -> Icons.Default.DataUsage
    else -> Icons.AutoMirrored.Filled.List
}
