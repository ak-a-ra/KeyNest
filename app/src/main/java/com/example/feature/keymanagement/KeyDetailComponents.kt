package com.example.feature.keymanagement
import com.example.feature.vault.TactileCopyButton

import androidx.core.net.toUri
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.ApiKeyItem
import com.example.core.model.ProviderPreset
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianBorderLight
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KeyDetailFingerprintCard(
    apiKey: String,
    modifier: Modifier = Modifier
) {
    val shaFingerprint = remember(apiKey) {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(apiKey.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }.take(24) + "..."
        } catch (_: Exception) {
            "N/A"
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSurfaceElevated,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SHA-256 CHECKSUM FINGERPRINT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = shaFingerprint,
                    style = MonospaceCodeStyle.copy(
                        fontSize = 11.5.sp,
                        color = CyberCyan,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberCyan.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "VERIFIED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun KeyDetailActivityMetricsCard(
    item: ApiKeyItem,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSurfaceElevated,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = CyberGold,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Usage Activity",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Copied Count", fontSize = 11.sp, color = TextTertiary)
                    Text("${item.copyCount} times", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column {
                    Text("Created Date", fontSize = 11.sp, color = TextTertiary)
                    Text(dateFormat.format(Date(item.createdAt)), fontSize = 12.sp, color = TextSecondary)
                }
                Column {
                    Text("Last Copied", fontSize = 11.sp, color = TextTertiary)
                    Text(
                        item.lastCopiedAt?.let { dateFormat.format(Date(it)) } ?: "Never",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun KeyCodeSnippetsCard(
    item: ApiKeyItem,
    preset: ProviderPreset,
    onCopySnippet: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSnippetTab by remember { mutableIntStateOf(0) }
    val varName = remember(item.provider, item.title) {
        preset.envVarNameSuggestion.ifEmpty {
            item.title.uppercase().replace("[^A-Z0-9_]".toRegex(), "_")
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianSurfaceElevated)
            .border(1.dp, ObsidianBorderLight, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                Text(
                    text = "Code Integration Snippets",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        val tabs = listOf(".env", "cURL", "Node.js", "Python")
        TabRow(
            selectedTabIndex = selectedSnippetTab,
            containerColor = ObsidianSurface,
            contentColor = CyberGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSnippetTab]),
                    color = CyberGold
                )
            }
        ) {
            tabs.forEachIndexed { index, tabName ->
                Tab(
                    selected = selectedSnippetTab == index,
                    onClick = { selectedSnippetTab = index },
                    text = {
                        Text(
                            text = tabName,
                            fontSize = 11.5.sp,
                            fontWeight = if (selectedSnippetTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSnippetTab == index) CyberGold else TextSecondary
                        )
                    }
                )
            }
        }

        val snippetText = when (selectedSnippetTab) {
            0 -> "${varName}=\"${item.apiKey}\"${if (item.endpointUrl.isNotBlank()) "\n${varName}_ENDPOINT=\"${item.endpointUrl}\"" else ""}"
            1 -> "curl ${item.endpointUrl.ifEmpty { "https://api.example.com" }} \\\n  -H \"Authorization: Bearer ${item.apiKey}\" \\\n  -H \"Content-Type: application/json\""
            2 -> "import { config } from 'dotenv';\nconfig();\nconst apiKey = process.env.${varName} || '${item.apiKey}';"
            else -> "import os\napi_key = os.getenv('${varName}', '${item.apiKey}')"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ObsidianSurface)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = snippetText,
                    style = MonospaceCodeStyle.copy(fontSize = 11.5.sp, color = CyberEmerald)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TactileCopyButton(
                        onCopy = {
                            onCopySnippet(snippetText, "$varName Snippet")
                        },
                        label = "Copy Snippet"
                    )
                }
            }
        }
    }
}

@Composable
fun KeyDeveloperConsoleButton(
    provider: String,
    consoleUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, consoleUrl.toUri())
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSurfaceElevated,
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Open $provider Console",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Manage quotas, billing & revoke keys",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
