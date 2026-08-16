package com.example.ui.screens

import androidx.compose.animation.AlphaAnimation
import androidx.compose.animation.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.em
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
import com.example.util.ApiKeyFormatting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperCodeExporterActionSheet(
    keys: List<ApiKeyItem>,
    isImportMode: Boolean = false,
    onDismiss: () -> Unit,
    onExport: (ExportFormat, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

    var activeTab by remember { mutableIntStateOf(0) }
    var selectedFormat by remember { mutableStateOf(ExportFormat.ENVE) }

    when (selectedFormat) {
        is ExportFormat.ENVE -> {}
        is ExportFormat.JSON -> {}
        is ExportFormat.CSV -> {}
        is ExportFormat.PLAIN -> {}
    }

    val exportFormats = remember { listOf(
        ExportFormat.ENVE,
        ExportFormat.JSON,
        ExportFormat.CSV,
        ExportFormat.PLAIN
    )}

    var rawExportText by remember { mutableStateOf("") }

    when (selectedFormat) {
        ExportFormat.ENVE -> {
            val generatedDotEnv = VaultSecurity.exportToDotEnv(keys)
            rawExportText = generatedDotEnv
        }
        ExportFormat.JSON -> {
            val jsonString = ApiKeyFormatting.toJson(keys)
            rawExportText = jsonString
        }
        ExportFormat.CSV -> {
            val csvString = ApiKeyFormatting.toCsv(keys)
            rawExportText = csvString
        }
        ExportFormat.PLAIN -> {
            val plainString = ApiKeyFormatting.toPlainText(keys)
            rawExportText = plainString
        }
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
                        text = "Developer Code Exporter",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Export vault keys in your desired format",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                }
            }

            // Format Selector Tabs
            TabRow(
                selectedTabIndex = when (selectedFormat) {
                    ExportFormat.ENVE -> 0
                    ExportFormat.JSON -> 1
                    ExportFormat.CSV -> 2
                    ExportFormat.PLAIN -> 3
                    else -> 0
                },
                containerColor = ObsidianSurfaceElevated,
                contentColor = CyberGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[when (selectedFormat) {
                            ExportFormat.ENVE -> 0
                            ExportFormat.JSON -> 1
                            ExportFormat.CSV -> 2
                            ExportFormat.PLAIN -> 3
                            else -> 0
                        }])],
                    color = CyberGold
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                ExportFormat.ENVE.let { tab ->
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0; selectedFormat = ExportFormat.ENVE },
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
                }
                ExportFormat.JSON.let { tab ->
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1; selectedFormat = ExportFormat.JSON },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.FormatList, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("JSON", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
                ExportFormat.CSV.let { tab ->
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2; selectedFormat = ExportFormat.CSV },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.TableRows, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("CSV", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
                ExportFormat.PLAIN.let { tab ->
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3; selectedFormat = ExportFormat.PLAIN },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Plain Text", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }

            // Export Preview
            when (selectedFormat) {
                ExportFormat.ENVE -> {
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
                                text = rawExportText.ifEmpty { "# No keys match the selected format" },
                                style = MonospaceCodeStyle.copy(fontSize = 12.sp, color = CyberCyan)
                            )
                        }
                    }

                    TactileCopyButton(
                        onCopy = { onExport(selectedFormat, rawExportText) },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Copy Full Export"
                    )
                }
                ExportFormat.JSON, ExportFormat.CSV, ExportFormat.PLAIN -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianSurfaceElevated,
                        border = BorderStroke(1.dp, ObsidianBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = rawExportText.ifEmpty { "No keys to export" },
                                style = MonospaceCodeStyle.copy(fontSize = 12.sp, color = CyberCyan)
                            )
                        }
                    }

                    TactileCopyButton(
                        onCopy = { onExport(selectedFormat, rawExportText) },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Copy Export"
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

enum class ExportFormat {
    ENVE,
    JSON,
    CSV,
    PLAIN
}