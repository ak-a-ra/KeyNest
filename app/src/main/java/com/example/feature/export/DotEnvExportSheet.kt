package com.example.feature.export

import androidx.core.graphics.toColorInt
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.core.model.ApiKeyItem
import com.example.feature.vault.VaultViewModel
import com.example.core.security.VaultSecurity
import com.example.feature.vault.TactileCopyButton
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianBorderLight
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.StatusWarning
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DotEnvExportSheet(
    viewModel: VaultViewModel,
    keys: List<ApiKeyItem>,
    isImportMode: Boolean = false,
    onDismiss: () -> Unit,
    onCopyAll: (String) -> Unit,
    onImportKeys: (List<ApiKeyItem>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var activeTab by remember { mutableIntStateOf(if (isImportMode) 1 else 0) }

    val filteredKeys = remember(keys) {
        keys
    }
    val generatedDotEnv = remember(filteredKeys) {
        VaultSecurity.exportToDotEnv(filteredKeys)
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = viewModel.exportTextFile(uri, generatedDotEnv)
                if (result.isSuccess) {
                    Toast.makeText(context, "Saved .env file successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error saving file: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var rawImportText by remember { mutableStateOf("") }

    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val result = viewModel.importTextFile(uri)
                if (result.isSuccess) {
                    rawImportText = result.getOrNull() ?: ""
                    Toast.makeText(context, "Loaded .env file successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error reading file: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { saveFileLauncher.launch(".env") },
                        enabled = generatedDotEnv.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("save_dotenv_button")
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save .env File", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        TactileCopyButton(
                            onCopy = { onCopyAll(generatedDotEnv) },
                            modifier = Modifier.fillMaxWidth(),
                            label = "Copy .env"
                        )
                    }
                }
            } else {
                // Import View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto-Correct .env & Variables:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            onClick = {
                                rawImportText = """
                                    # Production AI & Cloud Configuration
                                    export NEXT_PUBLIC_OPENAI_API_KEY="sample_openai_key_prod_1234" # Primary model key
                                    OPENAI_BASE_URL="https://api.openai.com/v1"
                                    export ANTHROPIC_KEY='sample_anthropic_key_claude_1234';
                                    STRIPE_TEST_KEY: "sample_stripe_test_key_1234"
                                    AWS_ACCESS_KEY_ID=sample_aws_access_key_id_1234
                                    AWS_SECRET_ACCESS_KEY=sample_aws_secret_access_key_1234
                                """.trimIndent()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = ObsidianSurfaceElevated,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Text("Sample", fontSize = 11.sp, color = CyberCyan, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                        }

                        Surface(
                            onClick = { openFileLauncher.launch(arrayOf("text/plain", "*/*")) },
                            shape = RoundedCornerShape(8.dp),
                            color = ObsidianSurfaceElevated,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = CyberGold, modifier = Modifier.size(13.dp))
                                Text("Pick File", fontSize = 11.sp, color = CyberGold)
                            }
                        }

                        Surface(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrEmpty()) rawImportText = clipText
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = ObsidianSurfaceElevated,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(13.dp))
                                Text("Paste", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = rawImportText,
                    onValueChange = { rawImportText = it },
                    placeholder = {
                        Text(
                            text = "Paste .env, bash exports, or docker vars:\nexport OPENAI_API_KEY=\"sample_openai_key_...\"\nANTHROPIC_DEV_KEY='sample_anthropic_key_...';\nSTRIPE_KEY: \"sample_stripe_key_...\"\nAWS_ACCESS_KEY_ID=sample_aws_key_id_...\nAWS_SECRET_ACCESS_KEY=...",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGold,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MonospaceCodeStyle.copy(fontSize = 11.sp)
                )

                AnimatedVisibility(visible = parsedCandidateKeys.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = CyberEmerald.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = CyberEmerald, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        text = "Auto-Corrected ${parsedCandidateKeys.size} Key(s) Successfully",
                                        color = CyberEmerald,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Cleaned quotes, stripped 'export' keywords, paired companion secrets, and resolved provider categories.",
                                        color = TextSecondary,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }

                        // Preview Cards
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            parsedCandidateKeys.forEach { item ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = ObsidianSurfaceElevated,
                                    border = BorderStroke(1.dp, ObsidianBorderLight)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(try { Color(item.colorHex.toColorInt()) } catch (_: Exception) { CyberGold })
                                            )
                                            Column {
                                                Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                Text(
                                                    text = VaultSecurity.maskKey(item.apiKey),
                                                    style = MonospaceCodeStyle.copy(fontSize = 10.sp, color = CyberCyan)
                                                )
                                            }
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (item.secretKey.isNotBlank()) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = CyberEmerald.copy(alpha = 0.15f),
                                                    border = BorderStroke(0.5.dp, CyberEmerald)
                                                ) {
                                                    Text("+Secret", fontSize = 9.sp, color = CyberEmerald, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = ObsidianBorder,
                                                border = BorderStroke(0.5.dp, ObsidianBorderLight)
                                            ) {
                                                Text(item.provider, fontSize = 9.sp, color = TextSecondary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("import_dotenv_submit_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald, contentColor = Color(0xFF002211))
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = Color(0xFF002211), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (parsedCandidateKeys.isEmpty()) "Enter or paste .env above" else "Import ${parsedCandidateKeys.size} Key(s) to Vault",
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
