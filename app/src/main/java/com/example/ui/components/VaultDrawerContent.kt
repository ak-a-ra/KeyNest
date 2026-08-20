package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberGoldLight
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantAvatarBg
import com.example.ui.theme.VibrantPillBg
import com.example.ui.viewmodel.ThemeMode

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import com.example.ui.viewmodel.VaultViewMode
import com.example.ui.theme.StatusDanger

@Composable
fun VaultDrawerSheetContent(
    totalKeysCount: Int,
    trashCount: Int = 0,
    currentViewMode: VaultViewMode = VaultViewMode.ALL_SECRETS,
    selectedCategory: String,
    selectedEnvironment: String,
    themeMode: ThemeMode,
    isPinConfigured: Boolean,
    onSelectAllSecrets: () -> Unit,
    onSelectTrash: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onSelectEnvironment: (String) -> Unit,
    onOpenSecurityAudit: () -> Unit,
    onOpenGenerator: () -> Unit,
    onOpenDotEnvExport: () -> Unit,
    onCycleTheme: () -> Unit,
    onToggleLockOrPinSettings: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = ObsidianSurface,
        drawerContentColor = TextPrimary,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.width(310.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Area
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo_red_cat),
                        contentDescription = "KeyNest Mascot",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, CyberCyan.copy(alpha = 0.6f), CircleShape)
                    )
                    Column {
                        Text("KeyNest Vault", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Text("Encrypted Secrets • $totalKeysCount items", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                HorizontalDivider(color = ObsidianBorder, modifier = Modifier.padding(vertical = 6.dp))
            }

            // All Secrets Navigation Item
            item {
                val isAllSelected = currentViewMode == VaultViewMode.ALL_SECRETS && selectedCategory == "All" && selectedEnvironment == "All"
                NavigationDrawerItem(
                    label = { Text("All Secrets", fontWeight = FontWeight.SemiBold) },
                    selected = isAllSelected,
                    onClick = onSelectAllSecrets,
                    icon = { Icon(Icons.Default.Key, contentDescription = null, tint = CyberGold) },
                    badge = {
                        Text(
                            text = "$totalKeysCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = CyberGoldLight,
                        selectedIconColor = CyberGold,
                        selectedTextColor = TextPrimary,
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // Trash Navigation Item
            item {
                val isTrashSelected = currentViewMode == VaultViewMode.TRASH
                NavigationDrawerItem(
                    label = { Text("Trash", fontWeight = FontWeight.SemiBold) },
                    selected = isTrashSelected,
                    onClick = onSelectTrash,
                    icon = { Icon(if (isTrashSelected) Icons.Default.Delete else Icons.Default.DeleteOutline, contentDescription = null, tint = if (isTrashSelected) StatusDanger else TextSecondary) },
                    badge = {
                        if (trashCount > 0) {
                            Text(
                                text = "$trashCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusDanger
                            )
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = VibrantPillBg,
                        selectedIconColor = StatusDanger,
                        selectedTextColor = TextPrimary,
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // CATEGORIES Section Label
            item {
                Text(
                    text = "CATEGORIES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
                )
            }

            val categories = listOf(
                "All" to Icons.Default.Apps,
                "AI & LLMs" to Icons.Default.AutoAwesome,
                "Cloud & Infra" to Icons.Default.Cloud,
                "Payments" to Icons.Default.CreditCard,
                "Auth & Security" to Icons.Default.Lock,
                "Database" to Icons.Default.Storage,
                "Dev Tools" to Icons.Default.Code
            )

            items(categories) { (cat, icon) ->
                val isSelected = selectedCategory == cat
                NavigationDrawerItem(
                    label = { Text(cat) },
                    selected = isSelected,
                    onClick = { onSelectCategory(cat) },
                    icon = { Icon(icon, contentDescription = null, tint = if (isSelected) CyberCyan else TextSecondary) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = VibrantPillBg,
                        selectedIconColor = CyberCyan,
                        selectedTextColor = TextPrimary,
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // ENVIRONMENTS Section Label
            item {
                Text(
                    text = "ENVIRONMENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp)
                )
            }

            val environments = listOf(
                "All" to Icons.Default.Layers,
                "Production" to Icons.Default.Shield,
                "Staging" to Icons.Default.Build,
                "Development" to Icons.Default.Terminal
            )

            items(environments) { (env, icon) ->
                val isSelected = selectedEnvironment == env
                NavigationDrawerItem(
                    label = { Text(env) },
                    selected = isSelected,
                    onClick = { onSelectEnvironment(env) },
                    icon = { Icon(icon, contentDescription = null, tint = if (isSelected) CyberGold else TextSecondary) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = VibrantPillBg,
                        selectedIconColor = CyberGold,
                        selectedTextColor = TextPrimary,
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // TOOLS & SETTINGS Section Label
            item {
                HorizontalDivider(color = ObsidianBorder, modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    text = "TOOLS & SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                )
            }

//             item {
//                 NavigationDrawerItem(
//                     label = { Text("Security Audit") },
//                     selected = false,
//                     onClick = onOpenSecurityAudit,
//                     icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = CyberGold) },
//                     shape = RoundedCornerShape(24.dp),
//                     colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedTextColor = TextSecondary)
//                 )
//             }
// 
//             item {
//                 NavigationDrawerItem(
//                     label = { Text("Key Generator") },
//                     selected = false,
//                     onClick = onOpenGenerator,
//                     icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberEmerald) },
//                     shape = RoundedCornerShape(24.dp),
//                     colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedTextColor = TextSecondary)
//                 )
//             }

            item {
                NavigationDrawerItem(
                    label = { Text("Export / Import .env") },
                    selected = false,
                    onClick = onOpenDotEnvExport,
                    icon = { Icon(Icons.Default.FileDownload, contentDescription = null, tint = CyberCyan) },
                    shape = RoundedCornerShape(24.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedTextColor = TextSecondary)
                )
            }

            item {
                NavigationDrawerItem(
                    label = { Text("Theme: ${themeMode.label}") },
                    selected = false,
                    onClick = onCycleTheme,
                    icon = { Icon(Icons.Default.Brightness4, contentDescription = null, tint = CyberGold) },
                    shape = RoundedCornerShape(24.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedTextColor = TextSecondary)
                )
            }

            item {
                NavigationDrawerItem(
                    label = { Text(if (isPinConfigured) "Lock Vault" else "Setup PIN") },
                    selected = false,
                    onClick = onToggleLockOrPinSettings,
                    icon = { Icon(if (isPinConfigured) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = null, tint = if (isPinConfigured) CyberGold else TextSecondary) },
                    shape = RoundedCornerShape(24.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedTextColor = TextSecondary)
                )
            }
        }
    }
}
