package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.security.VaultSecurity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.MonospaceCodeStyle
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VibrantPillBg
import com.example.ui.viewmodel.ClipboardCopyState

@Composable
fun ClipboardDetectionBanner(
    detectedKey: String,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detectedProvider = remember(detectedKey) { VaultSecurity.detectProviderFromKey(detectedKey) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSurface,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, CyberEmerald)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = CyberEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Detected $detectedProvider Key in Clipboard!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberEmerald
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Clear, contentDescription = "Dismiss", tint = TextTertiary, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = VaultSecurity.maskKey(detectedKey, 6),
                style = MonospaceCodeStyle.copy(fontSize = 12.sp, color = TextPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Ignore", color = TextTertiary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald, contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to Vault", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ClipboardAutoClearBanner(
    copyState: ClipboardCopyState,
    onClearNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Visual Progress Ring Container
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(34.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { copyState.secondsRemaining.toFloat() / copyState.totalSeconds.toFloat() },
                            modifier = Modifier.size(34.dp),
                            color = CyberCyan,
                            trackColor = VibrantPillBg,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "${copyState.secondsRemaining}s",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Secret Copied to Clipboard",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "PROTECTED",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberEmerald,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = copyState.label,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = onClearNow,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan.copy(alpha = 0.18f),
                        contentColor = CyberCyan
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Clear Now", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            LinearProgressIndicator(
                progress = { copyState.secondsRemaining.toFloat() / copyState.totalSeconds.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape),
                color = CyberCyan,
                trackColor = VibrantPillBg
            )
        }
    }
}
