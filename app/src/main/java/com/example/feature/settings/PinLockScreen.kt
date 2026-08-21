package com.example.feature.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.ObsidianBg
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.StatusDanger
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PinLockScreen(
    onUnlockSuccess: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun handleDigit(d: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + d
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (onVerifyPin(newPin)) {
                    onUnlockSuccess()
                } else {
                    errorMessage = "Incorrect Master PIN. Try again."
                    scope.launch {
                        shakeOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 400
                                -20f at 50
                                20f at 100
                                -15f at 150
                                15f at 200
                                -10f at 250
                                10f at 300
                                0f at 400
                            }
                        )
                        enteredPin = ""
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier
                .widthIn(max = 420.dp)
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
        ) {
            // Lock Badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(CyberGold.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                    .border(1.5.dp, CyberGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CyberGold,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "KeyNest Vault Locked",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter your 4-digit Master PIN to unlock secrets",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // 4 Pin Indicator Dots
            PinDotsRow(length = enteredPin.length)

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = StatusDanger,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Full Numeric Keypad
            NumericKeypad(
                buttonSize = 68,
                fontSize = 22,
                spacing = 20,
                showClear = true,
                onDigit = ::handleDigit,
                onBackspace = { if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1) },
                onClear = { enteredPin = ""; errorMessage = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinSettingsSheet(
    isPinCurrentlyEnabled: Boolean,
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var step by remember { mutableStateOf(if (isPinCurrentlyEnabled) "MANAGE" else "SET_NEW") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Master PIN Vault Security",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                }
            }

            when (step) {
                "MANAGE" -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianSurfaceElevated,
                        border = BorderStroke(1.dp, CyberEmerald)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = CyberEmerald)
                            Column {
                                Text("Vault Protection is Active", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Your keys require a 4-digit PIN to view.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }

                    Button(
                        onClick = { step = "SET_NEW"; newPin = ""; confirmPin = "" },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGold, contentColor = Color(0xFF1E1400))
                    ) {
                        Text("Change Master PIN", fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            onRemovePin()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Disable PIN Lock (Not Recommended)", color = StatusDanger, fontWeight = FontWeight.SemiBold)
                    }
                }
                "SET_NEW" -> {
                    Text("Enter a new 4-digit Master PIN", color = TextPrimary, fontWeight = FontWeight.Bold)
                    PinDotsRow(length = newPin.length)

                    NumericKeypad(
                        buttonSize = 54,
                        fontSize = 18,
                        spacing = 14,
                        showClear = false,
                        onDigit = { digit ->
                            if (newPin.length < 4) {
                                newPin += digit
                                if (newPin.length == 4) step = "CONFIRM_NEW"
                            }
                        },
                        onBackspace = { if (newPin.isNotEmpty()) newPin = newPin.dropLast(1) }
                    )
                }
                "CONFIRM_NEW" -> {
                    Text("Confirm your 4-digit Master PIN", color = TextPrimary, fontWeight = FontWeight.Bold)
                    PinDotsRow(length = confirmPin.length)

                    if (errorMessage != null) {
                        Text(errorMessage!!, color = StatusDanger, fontSize = 12.sp)
                    }

                    NumericKeypad(
                        buttonSize = 54,
                        fontSize = 18,
                        spacing = 14,
                        showClear = false,
                        onDigit = { digit ->
                            if (confirmPin.length < 4) {
                                confirmPin += digit
                                if (confirmPin.length == 4) {
                                    if (confirmPin == newPin) {
                                        onSetPin(newPin)
                                        onDismiss()
                                    } else {
                                        errorMessage = "PINs do not match! Please try again."
                                        confirmPin = ""
                                    }
                                }
                            }
                        },
                        onBackspace = { if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinDotsRow(length: Int, total: Int = 4) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        for (i in 0 until total) {
            val isFilled = i < length
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) CyberGold else ObsidianSurfaceElevated)
                    .border(1.dp, if (isFilled) CyberGold else ObsidianBorder, CircleShape)
            )
        }
    }
}

@Composable
private fun NumericKeypad(
    buttonSize: Int,
    fontSize: Int,
    spacing: Int,
    showClear: Boolean = false,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit = {}
) {
    val keypad = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (showClear) "C" else "", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy((spacing * 0.7).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keypad.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    if (key.isEmpty()) {
                        Spacer(modifier = Modifier.size(buttonSize.dp))
                    } else {
                        Surface(
                            onClick = {
                                when (key) {
                                    "DEL" -> onBackspace()
                                    "C" -> onClear()
                                    else -> onDigit(key)
                                }
                            },
                            modifier = Modifier
                                .size(buttonSize.dp)
                                .testTag("pin_key_$key"),
                            shape = CircleShape,
                            color = ObsidianSurfaceElevated,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (key == "DEL") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = TextSecondary,
                                        modifier = Modifier.size((buttonSize * 0.32).dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        fontSize = fontSize.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (key == "C") TextTertiary else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
