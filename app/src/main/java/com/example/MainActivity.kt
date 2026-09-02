package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.feature.settings.PinLockScreen
import com.example.feature.search.SearchScreen
import com.example.feature.vault.VaultHomeScreen
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.KeyNestTheme
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.TextPrimary
import com.example.feature.vault.CopyFeedback
import com.example.feature.vault.ThemeMode
import com.example.feature.vault.VaultViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(vm: VaultViewModel) {
    val navController = rememberNavController()

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "vault",
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            composable("vault") {
                VaultHomeScreen(viewModel = vm, onNavigateToSearch = { navController.navigate("search") })
            }
            composable("search") {
                SearchScreen(viewModel = vm, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

class MainActivity : ComponentActivity() {

    private var vaultViewModel: VaultViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: VaultViewModel = viewModel()
            vaultViewModel = vm

            val themeMode by vm.themeMode.collectAsStateWithLifecycle()
            val systemInDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemInDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            KeyNestTheme(darkTheme = darkTheme) {
                val isLocked by vm.isVaultLocked.collectAsStateWithLifecycle()
                var currentToast by remember { mutableStateOf<CopyFeedback?>(null) }

                LaunchedEffect(Unit) {
                    vm.copyFeedbackEvent.collectLatest { feedback ->
                        currentToast = feedback
                        delay(2000)
                        currentToast = null
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLocked) {
                        PinLockScreen(
                            onUnlockSuccess = { /* State updates handled within ViewModel */ },
                            onVerifyPin = { pin -> vm.unlockVault(pin) }
                        )
                    } else {
                        MainScreen(vm = vm)
                    }

                    // Floating Copy Success Toast
                    AnimatedVisibility(
                        visible = currentToast != null,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 80.dp)
                    ) {
                        currentToast?.let { toast ->
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = ObsidianSurfaceElevated,
                                border = BorderStroke(1.5.dp, CyberEmerald),
                                shadowElevation = 10.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = CyberEmerald,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${toast.title} copied!",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vaultViewModel?.startClipboardMonitoring()
    }

    override fun onPause() {
        super.onPause()
        vaultViewModel?.stopClipboardMonitoring()
    }
}
