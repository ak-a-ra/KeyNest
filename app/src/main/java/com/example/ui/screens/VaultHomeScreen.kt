package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ApiKeyCard
import com.example.ui.components.ClipboardAutoClearBanner
import com.example.ui.components.ClipboardDetectionBanner
import com.example.ui.components.EmptyKeysState
import com.example.ui.components.GoogleKeepTopSearchBar
import com.example.ui.components.KeyCardActions
import com.example.ui.components.VaultDrawerSheetContent
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.VaultDialogState
import com.example.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.launch

@Composable
fun VaultHomeScreen(
    viewModel: VaultViewModel,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allKeys by viewModel.allKeys.collectAsStateWithLifecycle()
    val filteredKeys by viewModel.filteredKeys.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedEnvironment by viewModel.selectedEnvironment.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val clipboardDetectedKey by viewModel.clipboardDetectedKey.collectAsStateWithLifecycle()
    val isPinConfigured by viewModel.isPinConfigured.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val clipboardCopyState by viewModel.clipboardCopyState.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkClipboardForApiKey()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val cardActions = remember(viewModel) {
        KeyCardActions(
            onClick = { item -> viewModel.openDialog(VaultDialogState.KeyDetail(item)) },
            onCopy = { item ->
                viewModel.copyToClipboard(item.apiKey, "${item.title} API Key", isSecret = true, itemId = item.id)
            },
            onTogglePin = { item -> viewModel.togglePin(item) }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            VaultDrawerSheetContent(
                totalKeysCount = allKeys.size,
                selectedCategory = selectedCategory,
                selectedEnvironment = selectedEnvironment,
                themeMode = themeMode,
                isPinConfigured = isPinConfigured,
                onSelectAllSecrets = {
                    viewModel.setSelectedCategory("All")
                    viewModel.setSelectedEnvironment("All")
                    coroutineScope.launch { drawerState.close() }
                },
                onSelectCategory = { category ->
                    viewModel.setSelectedCategory(category)
                    coroutineScope.launch { drawerState.close() }
                },
                onSelectEnvironment = { env ->
                    viewModel.setSelectedEnvironment(env)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenSecurityAudit = {
                    viewModel.openDialog(VaultDialogState.SecurityAudit)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenGenerator = {
                    viewModel.openDialog(VaultDialogState.Generator)
                    coroutineScope.launch { drawerState.close() }
                },
                onOpenDotEnvExport = {
                    viewModel.openDialog(VaultDialogState.DotEnvExport)
                    coroutineScope.launch { drawerState.close() }
                },
                onCycleTheme = { viewModel.cycleThemeMode() },
                onToggleLockOrPinSettings = {
                    if (isPinConfigured) viewModel.lockVault() else viewModel.openDialog(VaultDialogState.PinSettings)
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openDialog(VaultDialogState.AddKey()) },
                    containerColor = ObsidianSurfaceElevated,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    ),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(end = 6.dp, bottom = 6.dp)
                        .testTag("fab_add_key")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Key", tint = TextPrimary, modifier = Modifier.size(22.dp))
                        Text("Add Secret", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .statusBarsPadding()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    GoogleKeepTopSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSearchClick = onNavigateToSearch,
                        sortOption = sortOption,
                        onSortOptionChange = { viewModel.setSortOption(it) },
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onOpenAudit = { viewModel.openDialog(VaultDialogState.SecurityAudit) }
                    )

                    AnimatedVisibility(
                        visible = clipboardCopyState != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        clipboardCopyState?.let { state ->
                            ClipboardAutoClearBanner(
                                copyState = state,
                                onClearNow = { viewModel.clearClipboard() }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = clipboardDetectedKey != null,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        clipboardDetectedKey?.let { rawKey ->
                            ClipboardDetectionBanner(
                                detectedKey = rawKey,
                                onSave = { viewModel.openDialog(VaultDialogState.AddKey(initialKey = rawKey)) },
                                onDismiss = { viewModel.dismissClipboardBanner() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (filteredKeys.isEmpty()) {
                        EmptyKeysState(
                            hasQuery = searchQuery.isNotEmpty() || selectedCategory != "All" || selectedEnvironment != "All",
                            onImportFromNotes = { viewModel.openDialog(VaultDialogState.DotEnvImport) }
                        )
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 84.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            staggeredItems(filteredKeys, key = { it.id }) { item ->
                                ApiKeyCard(item = item, actions = cardActions)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    when (val state = dialogState) {
        is VaultDialogState.AddKey -> {
            AddEditKeySheet(
                existingItem = null,
                initialPreset = state.preset,
                initialKeyText = state.initialKey,
                existingTitles = allKeys.map { it.title },
                onDismiss = { viewModel.closeDialog() },
                onSave = { viewModel.saveKey(it) }
            )
        }
        is VaultDialogState.EditKey -> {
            AddEditKeySheet(
                existingItem = state.item,
                existingTitles = allKeys.filter { it.id != state.item.id }.map { it.title },
                onDismiss = { viewModel.closeDialog() },
                onSave = { viewModel.saveKey(it) }
            )
        }
        is VaultDialogState.KeyDetail -> {
            KeyDetailSheet(
                item = state.item,
                onDismiss = { viewModel.closeDialog() },
                onEdit = { viewModel.openDialog(VaultDialogState.EditKey(it)) },
                onDelete = { viewModel.deleteKey(it) },
                onTogglePin = { viewModel.togglePin(it) },
                onCopyKey = { text, label, id ->
                    viewModel.copyToClipboard(text, label, isSecret = true, itemId = id)
                }
            )
        }
        is VaultDialogState.Generator -> {
            KeyGeneratorSheet(
                onDismiss = { viewModel.closeDialog() },
                onSaveToVault = { generatedKey ->
                    viewModel.openDialog(VaultDialogState.AddKey(initialKey = generatedKey))
                },
                onCopy = { key ->
                    viewModel.copyToClipboard(key, "Generated Secret", isSecret = true)
                }
            )
        }
        is VaultDialogState.DotEnvExport -> {
            DotEnvExportSheet(
                keys = allKeys,
                isImportMode = false,
                onDismiss = { viewModel.closeDialog() },
                onCopyAll = { content ->
                    viewModel.copyToClipboard(content, ".env Export", isSecret = true)
                },
                onImportKeys = { items -> viewModel.importKeys(items) }
            )
        }
        is VaultDialogState.DotEnvImport -> {
            DotEnvExportSheet(
                keys = allKeys,
                isImportMode = true,
                onDismiss = { viewModel.closeDialog() },
                onCopyAll = { content ->
                    viewModel.copyToClipboard(content, ".env Export", isSecret = true)
                },
                onImportKeys = { items -> viewModel.importKeys(items) }
            )
        }
        is VaultDialogState.SecurityAudit -> {
            SecurityAuditSheet(
                keys = allKeys,
                isPinEnabled = isPinConfigured,
                onDismiss = { viewModel.closeDialog() },
                onSelectKey = { keyItem -> viewModel.openDialog(VaultDialogState.KeyDetail(keyItem)) }
            )
        }
        is VaultDialogState.PinSettings -> {
            PinSettingsSheet(
                isPinCurrentlyEnabled = isPinConfigured,
                onDismiss = { viewModel.closeDialog() },
                onSetPin = { pin -> viewModel.setMasterPin(pin) },
                onRemovePin = { viewModel.removeMasterPin() }
            )
        }
        VaultDialogState.None -> Unit
    }
}
