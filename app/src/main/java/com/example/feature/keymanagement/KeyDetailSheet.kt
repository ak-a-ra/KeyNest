package com.example.feature.keymanagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ApiKeyItem
import com.example.core.model.ProviderPreset
import com.example.core.model.ProviderPresets
import com.example.feature.vault.EntropyStrengthBar
import com.example.feature.keymanagement.KeyCodeSnippetsCard
import com.example.feature.keymanagement.KeyDetailActivityMetricsCard
import com.example.feature.keymanagement.KeyDetailFingerprintCard
import com.example.feature.keymanagement.KeyDeveloperConsoleButton
import com.example.feature.keymanagement.KeyExpirationStatusCard
import com.example.feature.vault.MaskedKeyPreview
import com.example.feature.vault.ProviderIconBadge
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.LocalKeyNestColors
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.StatusDanger
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import com.example.core.designsystem.VaultCardColor
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyDetailSheet(
    item: ApiKeyItem,
    onDismiss: () -> Unit,
    onEdit: (ApiKeyItem) -> Unit,
    onDelete: (ApiKeyItem) -> Unit,
    onTogglePin: (ApiKeyItem) -> Unit,
    onCopyKey: (String, String, Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val preset = remember(item.provider) { ProviderPresets.findByName(item.provider) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val isDark = LocalKeyNestColors.current.isDark
    val sheetBgColor = remember(item.colorHex, isDark) {
        val matched = VaultCardColor.fromHex(item.colorHex)
        if (matched != VaultCardColor.DEFAULT) {
            Color(if (isDark) matched.darkBg else matched.lightBg)
        } else {
            if (isDark) Color(0xFF202124) else Color(0xFFFFFFFF)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBgColor,
        dragHandle = null
    ) {
        KeyDetailPane(
            item = item,
            preset = preset,
            onEdit = onEdit,
            onDelete = { showDeleteConfirm = true },
            onTogglePin = onTogglePin,
            onCopyKey = onCopyKey,
            onClose = onDismiss
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Move to Trash?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Move \"${item.title}\" to Trash? You can restore it anytime from the drawer menu.", color = TextSecondary) },
            containerColor = ObsidianSurface,
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(item)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDanger)
                ) {
                    Text("Move to Trash", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun KeyDetailPane(
    item: ApiKeyItem,
    preset: com.example.core.model.ProviderPreset,
    onEdit: (ApiKeyItem) -> Unit,
    onDelete: () -> Unit,
    onTogglePin: (ApiKeyItem) -> Unit,
    onCopyKey: (String, String, Long) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProviderIconBadge(
                    provider = item.provider,
                    colorHex = item.colorHex ?: preset.defaultColorHex,
                    size = 38
                )
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = item.provider,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onTogglePin(item) },
                    modifier = Modifier.testTag("detail_pin_key_button")
                ) {
                    Icon(
                        imageVector = if (item.isPinned) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (item.isPinned) "Unpin Key" else "Pin Key",
                        tint = if (item.isPinned) CyberGold else TextTertiary
                    )
                }
                IconButton(onClick = { onEdit(item) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyberCyan)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusDanger)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                }
            }
        }

        // Primary API Key Section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "API KEY / TOKEN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiary,
                letterSpacing = 0.8.sp
            )
            MaskedKeyPreview(
                apiKey = item.apiKey,
                onCopy = {
                    onCopyKey(item.apiKey, "${item.title} API Key", item.id)
                }
            )
        }

        // Secret Key Section (if present)
        if (item.secretKey.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "SECONDARY SECRET / KEY ID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.8.sp
                )
                MaskedKeyPreview(
                    apiKey = item.secretKey,
                    onCopy = {
                        onCopyKey(item.secretKey, "${item.title} Secret", item.id)
                    }
                )
            }
        }

        // Entropy strength analysis
        EntropyStrengthBar(apiKey = item.apiKey)

        // SHA-256 Key Fingerprint Badge
        KeyDetailFingerprintCard(apiKey = item.apiKey)

        // Key Expiration & Rotation Status Card
        KeyExpirationStatusCard(
            item = item,
            onRotateClick = { onEdit(item) }
        )

        // Usage & Timestamp Metrics Card
        KeyDetailActivityMetricsCard(item = item)

        // Quick Code Snippets (Ready to copy for .env, cURL, Python, Node.js)
        KeyCodeSnippetsCard(
            item = item,
            preset = preset,
            onCopySnippet = { snippet, label ->
                onCopyKey(snippet, label, item.id)
            }
        )

        // Developer console link (if available)
        if (preset.consoleUrl.isNotEmpty()) {
            KeyDeveloperConsoleButton(
                provider = item.provider,
                consoleUrl = preset.consoleUrl
            )
        }

        // Notes and Tags if any
        if (item.notes.isNotBlank() || item.tags.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (item.tags.isNotBlank()) {
                    Text(
                        text = "TAGS: ${item.tags}",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (item.notes.isNotBlank()) {
                    Text(
                        text = item.notes,
                        fontSize = 12.5.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
