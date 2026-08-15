package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ProviderPresets
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberRose
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantAvatarBg
import com.example.ui.viewmodel.VaultViewModel

@Composable
fun SearchScreen(
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

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
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (searchQuery.isEmpty()) {
                    Text("Search Vault", color = TextSecondary, fontSize = 16.sp)
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    cursorBrush = SolidColor(CyberGold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Explorer Categories & Labels
        if (searchQuery.isEmpty()) {
            SearchSectionHeader(title = "Types")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(ProviderPresets.categories) { category ->
                    SearchCategoryItem(
                        label = category,
                        icon = getCategoryIcon(category),
                        onClick = {
                            viewModel.setSelectedCategory(category)
                            onNavigateBack()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            SearchSectionHeader(title = "Labels")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(listOf("Production", "Staging", "Development")) { env ->
                    SearchCategoryItem(
                        label = env,
                        icon = Icons.Default.DataUsage,
                        onClick = {
                            viewModel.setSelectedEnvironment(env)
                            onNavigateBack()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            SearchSectionHeader(title = "Colors")
            val colors = listOf(CyberGold, CyberCyan, CyberEmerald, CyberRose, Color(0xFF6366F1), Color(0xFFEAB308))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(VibrantAvatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatColorReset,
                            contentDescription = "Default Color",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                items(colors) { color ->
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}

@Composable
private fun SearchCategoryItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(VibrantAvatarBg)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
