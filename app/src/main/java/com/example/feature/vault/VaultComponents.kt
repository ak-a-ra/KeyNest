package com.example.feature.vault

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.security.VaultSecurity
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.CyberPurple
import com.example.core.designsystem.CyberRose
import com.example.core.designsystem.MonospaceCodeStyle
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceHighlight
import com.example.core.designsystem.StatusSuccess
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import com.example.core.designsystem.VibrantButtonBg
import com.example.core.designsystem.VibrantPillBg
import kotlinx.coroutines.delay

@Composable
fun ProviderIconBadge(
    provider: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val fallbackColor = CyberGold
    val brandColor = remember(colorHex, fallbackColor) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (_: Exception) {
            fallbackColor
        }
    }

    val initials = remember(provider) {
        when (provider.lowercase()) {
            "openai" -> "AI"
            "google gemini" -> "GM"
            "anthropic claude" -> "CL"
            "deepseek" -> "DS"
            "groq" -> "GQ"
            "mistral ai" -> "MS"
            "perplexity" -> "PX"
            "openrouter" -> "OR"
            "hugging face" -> "HF"
            "github" -> "GH"
            "stripe" -> "ST"
            "aws" -> "AW"
            "supabase" -> "SB"
            "firebase" -> "FB"
            "resend" -> "RS"
            "vercel" -> "VC"
            "elevenlabs" -> "EL"
            "pinecone" -> "PC"
            "discord bot" -> "DC"
            "telegram bot" -> "TG"
            else -> provider.take(2).uppercase()
        }
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size / 3).dp))
            .background(brandColor.copy(alpha = 0.14f))
            .border(1.2.dp, brandColor.copy(alpha = 0.35f), RoundedCornerShape((size / 3).dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = brandColor,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.38).sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = (-0.5).sp
        )
    }
}

@Composable
fun TactileCopyButton(
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Copy"
) {
    var isJustCopied by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isJustCopied) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "copy_scale"
    )

    LaunchedEffect(isJustCopied) {
        if (isJustCopied) {
            delay(1200)
            isJustCopied = false
        }
    }

    Surface(
        onClick = {
            isJustCopied = true
            onCopy()
        },
        modifier = modifier
            .scale(scale)
            .testTag("tactile_copy_button"),
        shape = RoundedCornerShape(14.dp),
        color = if (isJustCopied) CyberEmerald.copy(alpha = 0.18f) else VibrantButtonBg,
        border = BorderStroke(1.dp, if (isJustCopied) CyberEmerald else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AnimatedVisibility(
                visible = isJustCopied,
                enter = scaleIn(animationSpec = tween(150)) + fadeIn(animationSpec = tween(150)),
                exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Copied",
                    tint = CyberEmerald,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isJustCopied) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy key",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = if (isJustCopied) "COPIED" else label,
                color = if (isJustCopied) CyberEmerald else TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun MaskedKeyPreview(
    apiKey: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRevealed by remember { mutableStateOf(false) }
    val animatedTextColor by animateColorAsState(
        targetValue = if (isRevealed) CyberGold else TextPrimary,
        animationSpec = tween(durationMillis = 200),
        label = "masked_key_color"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurfaceHighlight,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = isRevealed,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.96f)) togetherWith
                                (fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.96f))
                    },
                    label = "key_unmask_transition"
                ) { revealed ->
                    Text(
                        text = if (revealed) apiKey else VaultSecurity.maskKey(apiKey, 4),
                        style = MonospaceCodeStyle.copy(
                            color = animatedTextColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isRevealed = !isRevealed },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_reveal_button")
                ) {
                    Icon(
                        imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isRevealed) "Hide Secret" else "Reveal Secret",
                        tint = if (isRevealed) CyberGold else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                TactileCopyButton(
                    onCopy = onCopy,
                    label = "Copy"
                )
            }
        }
    }
}

@Composable
fun EntropyStrengthBar(
    apiKey: String,
    modifier: Modifier = Modifier
) {
    val result = remember(apiKey) { VaultSecurity.calculateEntropy(apiKey) }
    val fallbackSuccessColor = StatusSuccess
    val barColor = remember(result.colorHex, fallbackSuccessColor) {
        try {
            Color(android.graphics.Color.parseColor(result.colorHex))
        } catch (_: Exception) {
            fallbackSuccessColor
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
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
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = barColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Entropy Quality: ${result.strength}",
                    color = barColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${result.entropyBits} bits",
                color = TextSecondary,
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { result.strengthPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = barColor,
            trackColor = VibrantPillBg
        )
    }
}
