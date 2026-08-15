package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeyItem
import com.example.data.security.VaultSecurity
import com.example.ui.components.TactileCopyButton
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianBorderLight
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DotEnvExportSheet(
    keys: List<ApiKeyItem>,
    isImportMode: Boolean = false,
    onDismiss: () -> Unit,
    onCopyAll: (String) -> Unit,
    onImportKeys: (List<ApiKeyItem>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

    var activeTab by remember { mutableIntStateOf(if (isImportMode) 1 else 0) }
    var selectedEnvFilter by remember { mutableStateOf("All") }

    val filteredKeys = remember(keys, selectedEnvFilter) {
        if (selectedEnvFilter == "All") keys else keys.filter { it.environment.equals(selectedEnvFilter, ignoreCase = true) }
    }
    val generatedDotEnv = remember(filteredKeys) {
        VaultSecurity.exportToDotEnv(filteredKeys)
    }

    var rawImportText by remember { mutableStateOf("") }
    val parsedCandidateKeys = remember(rawImportText) {
        if (rawImportText.isNotBlank()) VaultSecurity.parseDotEnv(rawImportText) else emptyList()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .align(Alignment.CenterHorizontally)
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
                Column {
                    Text(
                        text = ".env Exporter & Batch Importer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Migrate from Google Notes or export into project .env",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = ObsidianSurfaceElevated,
                contentColor = CyberGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = CyberGold
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Export .env", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Import from Notes", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (activeTab == 0) {
                // Export View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("All", "Production", "Staging", "Development").forEach { env ->
                        val isSelected = selectedEnvFilter == env
                        Surface(
                            onClick = { selectedEnvFilter = env },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CyberGold.copy(alpha = 0.15f) else ObsidianSurfaceElevated,
                            border = BorderStroke(1.dp, if (isSelected) CyberGold else ObsidianBorder)
                        ) {
                            Text(
                                text = env,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyberGold else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceElevated,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = generatedDotEnv.ifEmpty { "# No keys match the selected filter" },
                            style = MonospaceCodeStyle.copy(fontSize = 12.sp, color = CyberCyan)
                        )
                    }
                }

                TactileCopyButton(
                    onCopy = { onCopyAll(generatedDotEnv) },
                    modifier = Modifier.fillMaxWidth(),
                    label = "Copy Full .env Configuration"
                )
            } else {
                // Import View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paste raw .env or notes content:", fontSize = 12.sp, color = TextSecondary)
                    Button(
                        onClick = {
                            val clipText = clipboardManager.getText()?.text
                            if (!clipText.isNullOrEmpty()) rawImportText = clipText
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ObsidianSurfaceElevated, contentColor = CyberGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text("Paste Clipboard", fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }

                OutlinedTextField(
                    value = rawImportText,
                    onValueChange = { rawImportText = it },
                    placeholder = { Text("OPENAI_API_KEY=\"sk-proj-...\"\nANTHROPIC_KEY=\"sk-ant-...\"", color = TextTertiary, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGold,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MonospaceCodeStyle.copy(fontSize = 12.sp)
                )

                AnimatedVisibility(visible = parsedCandidateKeys.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = CyberEmerald.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, tint = CyberEmerald)
                            Text(
                                text = "Found ${parsedCandidateKeys.size} valid key(s) ready to import into encrypted vault",
                                color = CyberEmerald,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (parsedCandidateKeys.isNotEmpty()) {
                            onImportKeys(parsedCandidateKeys)
                            onDismiss()
                        }
                    },
                    enabled = parsedCandidateKeys.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald, contentColor = Color(0xFF002211))
                ) {
                    Text(
                        text = if (parsedCandidateKeys.isEmpty()) "Enter valid .env lines above" else "Import ${parsedCandidateKeys.size} Key(s) to Vault",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Security note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurfaceElevated)
                    .border(1.dp, ObsidianBorderLight, RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(16.dp))
                Text(
                    text = "Encrypted in-memory only. KeyNest never transmits keys across the network.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
