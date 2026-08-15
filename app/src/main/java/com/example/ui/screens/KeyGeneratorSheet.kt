package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.VaultSecurity
import com.example.ui.components.EntropyStrengthBar
import com.example.ui.components.TactileCopyButton
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianBorderLight
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyGeneratorSheet(
    onDismiss: () -> Unit,
    onSaveToVault: (String) -> Unit,
    onCopy: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var length by remember { mutableFloatStateOf(32f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(false) }
    var prefix by remember { mutableStateOf("sk_") }
    var generatorType by remember { mutableStateOf("CUSTOM") } // CUSTOM, UUID, HEX

    var generatedKey by remember {
        mutableStateOf(
            VaultSecurity.generateCustomKey(
                length = 32,
                useUpper = true,
                useLower = true,
                useNumbers = true,
                useSymbols = false,
                prefix = "sk_"
            )
        )
    }

    fun regenerate() {
        generatedKey = when (generatorType) {
            "UUID" -> VaultSecurity.generateUuid()
            "HEX" -> VaultSecurity.generateHex(length.toInt() / 2)
            else -> VaultSecurity.generateCustomKey(
                length = length.toInt(),
                useUpper = useUpper,
                useLower = useLower,
                useNumbers = useNumbers,
                useSymbols = useSymbols,
                prefix = prefix
            )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberEmerald.copy(alpha = 0.2f))
                            .border(1.dp, CyberEmerald, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyberEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Key & Secret Generator",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Cryptographically secure, high-entropy tokens",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                }
            }

            // Generated Key Preview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GENERATED SECRET",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            letterSpacing = 0.8.sp
                        )
                        IconButton(
                            onClick = ::regenerate,
                            modifier = Modifier.size(32.dp).testTag("button_regenerate")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate",
                                tint = CyberEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = generatedKey,
                        style = MonospaceCodeStyle.copy(
                            color = CyberGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TactileCopyButton(
                            onCopy = { onCopy(generatedKey) },
                            modifier = Modifier.weight(1f),
                            label = "Copy Secret"
                        )
                        Button(
                            onClick = {
                                onDismiss()
                                onSaveToVault(generatedKey)
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberEmerald,
                                contentColor = Color(0xFF002211)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Save to Vault", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }

            // Real-time Entropy Quality Meter
            EntropyStrengthBar(apiKey = generatedKey)

            // Generator Preset Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "CUSTOM" to "API Token",
                    "UUID" to "UUID v4",
                    "HEX" to "HEX Hash"
                ).forEach { (type, label) ->
                    val isSelected = generatorType == type
                    Surface(
                        onClick = {
                            generatorType = type
                            regenerate()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CyberEmerald.copy(alpha = 0.15f) else ObsidianSurfaceElevated,
                        border = BorderStroke(1.dp, if (isSelected) CyberEmerald else ObsidianBorder)
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CyberEmerald else TextSecondary
                        )
                    }
                }
            }

            // Custom Token Controls
            if (generatorType == "CUSTOM" || generatorType == "HEX") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianSurfaceElevated)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Length", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text(
                            "${length.toInt()} characters",
                            color = CyberGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }

                    Slider(
                        value = length,
                        onValueChange = {
                            length = it
                            regenerate()
                        },
                        valueRange = 8f..128f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberEmerald,
                            activeTrackColor = CyberEmerald,
                            inactiveTrackColor = ObsidianBorder
                        )
                    )

                    if (generatorType == "CUSTOM") {
                        OutlinedTextField(
                            value = prefix,
                            onValueChange = {
                                prefix = it
                                regenerate()
                            },
                            label = { Text("Prefix (e.g. sk_, gsk_, api_)", color = TextSecondary, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        // Charset toggles
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            CharsetCheckbox("Uppercase Letters (A-Z)", useUpper) { useUpper = it; regenerate() }
                            CharsetCheckbox("Lowercase Letters (a-z)", useLower) { useLower = it; regenerate() }
                            CharsetCheckbox("Numbers (0-9)", useNumbers) { useNumbers = it; regenerate() }
                            CharsetCheckbox("Special Symbols (!@#$)", useSymbols) { useSymbols = it; regenerate() }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharsetCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = CyberEmerald,
                uncheckedColor = TextTertiary
            )
        )
        Text(text = label, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
    }
}
