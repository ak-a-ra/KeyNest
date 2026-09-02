package com.example.feature.vault

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianBorderLight
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.StatusDanger
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.VibrantPillBg
import com.example.feature.vault.SortOption

@Composable
fun GoogleKeepTopSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    onOpenAudit: () -> Unit,
    onOpenGenerator: () -> Unit = {},
    onOpenDotEnvExport: () -> Unit = {},
    onOpenBackupRestore: () -> Unit = {},
    onOpenTrash: () -> Unit = {},
    onCycleTheme: () -> Unit = {},
    onToggleLockOrPinSettings: () -> Unit = {},
    trashCount: Int = 0,
    isPinConfigured: Boolean = false,
    isGridView: Boolean,
    onToggleGridView: () -> Unit,
    isSearching: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Pill
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = CircleShape,
            color = VibrantPillBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, ObsidianBorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Embedded Search Text Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search keys, tags, providers...",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input_field"),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 15.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(CyberGold)
                    )
                }

                if (isSearching) {
                    Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                    }
                } else if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Grid View Toggle Icon
                IconButton(
                    onClick = onToggleGridView,
                    modifier = Modifier.size(38.dp).testTag("button_toggle_grid")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                        contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Sort Layout Button
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = "Sort",
                            tint = TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(ObsidianSurface)
                    ) {
                        SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        color = if (sortOption == option) CyberGold else TextPrimary,
                                        fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSortOptionChange(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Profile Avatar Badge with Dropdown Quick Menu
        Box(modifier = Modifier.padding(start = 8.dp)) {
            IconButton(
                onClick = { showProfileMenu = true },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("button_top_profile")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_cat_ip_logo_1787319466857),
                            contentDescription = "Profile and Actions Menu",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = showProfileMenu,
                onDismissRequest = { showProfileMenu = false },
                modifier = Modifier
                    .background(ObsidianSurfaceElevated)
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.Security, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(20.dp))
                    },
                    text = { Text("Security Audit", color = TextPrimary, fontWeight = FontWeight.Medium) },
                    onClick = {
                        showProfileMenu = false
                        onOpenAudit()
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyberGold, modifier = Modifier.size(20.dp))
                    },
                    text = { Text("Key Generator", color = TextPrimary, fontWeight = FontWeight.Medium) },
                    onClick = {
                        showProfileMenu = false
                        onOpenGenerator()
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    },
                    text = { Text("Export / Import .env", color = TextPrimary, fontWeight = FontWeight.Medium) },
                    onClick = {
                        showProfileMenu = false
                        onOpenDotEnvExport()
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberGold, modifier = Modifier.size(20.dp))
                    },
                    text = { Text("Backup & Restore", color = TextPrimary, fontWeight = FontWeight.Medium) },
                    onClick = {
                        showProfileMenu = false
                        onOpenBackupRestore()
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(20.dp))
                    },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trash", color = TextPrimary, fontWeight = FontWeight.Medium)
                            if (trashCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = StatusDanger.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "$trashCount",
                                        color = StatusDanger,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        showProfileMenu = false
                        onOpenTrash()
                    }
                )

                HorizontalDivider(color = ObsidianBorder, modifier = Modifier.padding(vertical = 4.dp))

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(Icons.Default.Brightness4, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    },
                    text = { Text("Toggle Theme", color = TextPrimary) },
                    onClick = {
                        showProfileMenu = false
                        onCycleTheme()
                    }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            if (isPinConfigured) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isPinConfigured) CyberEmerald else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    text = {
                        Text(
                            if (isPinConfigured) "Lock Vault Now" else "Set Master PIN",
                            color = TextPrimary
                        )
                    },
                    onClick = {
                        showProfileMenu = false
                        onToggleLockOrPinSettings()
                    }
                )
            }
        }
    }
}

