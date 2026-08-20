package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf(ExportFormat.ENVE) }

    val rawExportText = remember(selectedFormat, keys) {
        when (selectedFormat) {
            ExportFormat.ENVE -> VaultSecurity.exportToDotEnv(keys)
            ExportFormat.JSON -> ApiKeyFormatting.toJson(keys)
            ExportFormat.CSV -> ApiKeyFormatting.toCsv(keys)
            ExportFormat.PLAIN -> ApiKeyFormatting.toPlainText(keys)
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(rawExportText.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "File saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val defaultFileName = when (selectedFormat) {
        ExportFormat.ENVE -> ".env"
        ExportFormat.JSON -> "keynest_vault.json"
        ExportFormat.CSV -> "keynest_vault.csv"
        ExportFormat.PLAIN -> "keynest_vault.txt"
    }

    val selectedTabIndex = when (selectedFormat) {
        ExportFormat.ENVE -> 0
        ExportFormat.JSON -> 1
        ExportFormat.CSV -> 2
        ExportFormat.PLAIN -> 3
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
                selectedTabIndex = selectedTabIndex,
                containerColor = ObsidianSurfaceElevated,
                contentColor = CyberGold,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = CyberGold
                        )
                    }
                },
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedFormat == ExportFormat.ENVE,
                    onClick = { selectedFormat = ExportFormat.ENVE },
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
                    selected = selectedFormat == ExportFormat.JSON,
                    onClick = { selectedFormat = ExportFormat.JSON },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("JSON", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedFormat == ExportFormat.CSV,
                    onClick = { selectedFormat = ExportFormat.CSV },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("CSV", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedFormat == ExportFormat.PLAIN,
                    onClick = { selectedFormat = ExportFormat.PLAIN },
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

            // Export Preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (selectedFormat == ExportFormat.ENVE) 200.dp else 260.dp),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { saveFileLauncher.launch(defaultFileName) },
                    enabled = rawExportText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_exported_file_button")
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to File", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                }

                Box(modifier = Modifier.weight(1f)) {
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
