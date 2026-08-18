package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed as staggeredItemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ApiKeyItem
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
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.VaultViewModel
import com.example.ui.viewmodel.VaultDialogState
import com.example.ui.viewmodel.DisplayMode
import kotlinx.coroutines.launch

import com.example.ui.components.VaultTrashView
import com.example.ui.viewmodel.VaultViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    viewModel: VaultViewModel,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allKeys by viewModel.allKeys.collectAsStateWithLifecycle()
    val trashedKeys by viewModel.trashedKeys.collectAsStateWithLifecycle()
    val trashCount by viewModel.trashCount.collectAsStateWithLifecycle()
    val currentViewMode by viewModel.currentViewMode.collectAsStateWithLifecycle()
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
    val displayMode by viewModel.displayMode.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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
                trashCount = trashCount,
                currentViewMode = currentViewMode,
                selectedCategory = selectedCategory,
                selectedEnvironment = selectedEnvironment,
                themeMode = themeMode,
                isPinConfigured = isPinConfigured,
                onSelectAllSecrets = {
                    viewModel.setViewMode(VaultViewMode.ALL_SECRETS)
                    viewModel.setSelectedCategory("All")
                    viewModel.setSelectedEnvironment("All")
                    coroutineScope.launch { drawerState.close() }
                },
                onSelectTrash = {
                    viewModel.setViewMode(VaultViewMode.TRASH)
                    coroutineScope.launch { drawerState.close() }
                },
                onSelectCategory = { category ->
                    viewModel.setViewMode(VaultViewMode.ALL_SECRETS)
                    viewModel.setSelectedCategory(category)
                    coroutineScope.launch { drawerState.close() }
                },
                onSelectEnvironment = { env ->
                    viewModel.setViewMode(VaultViewMode.ALL_SECRETS)
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
                if (currentViewMode == VaultViewMode.ALL_SECRETS) {
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
                    if (currentViewMode == VaultViewMode.TRASH) {
                        VaultTrashView(
                            trashedKeys = trashedKeys,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onBackToSecrets = { viewModel.setViewMode(VaultViewMode.ALL_SECRETS) },
                            onRestoreKey = { viewModel.restoreKey(it) },
                            onPermanentDeleteKey = { viewModel.permanentDeleteKey(it) },
                            onEmptyTrash = { viewModel.emptyTrash() }
                        )
                    } else {
                        GoogleKeepTopSearchBar(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onSearchClick = onNavigateToSearch,
                            sortOption = sortOption,
                            onSortOptionChange = { viewModel.setSortOption(it) },
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                            onOpenAudit = { viewModel.openDialog(VaultDialogState.SecurityAudit) },
                            isGridView = displayMode.isGrid,
                            onToggleGridView = {
                                viewModel.setDisplayMode(if (displayMode.isGrid) DisplayMode.List else DisplayMode.Grid)
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (filteredKeys.isEmpty()) {
                            EmptyKeysState(
                                hasQuery = searchQuery.isNotEmpty() || selectedCategory != "All" || selectedEnvironment != "All",
                                onImportFromNotes = { viewModel.openDialog(VaultDialogState.DotEnvImport) }
                            )
                        } else {
                            if (displayMode.isGrid) {
                                LazyVerticalStaggeredGrid(
                                    columns = StaggeredGridCells.Fixed(2),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 84.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalItemSpacing = 12.dp,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    staggeredItemsIndexed(filteredKeys, key = { _, item -> item.id }) { index, item ->
                                        ApiKeyCard(
                                            item = item,
                                            actions = cardActions,
                                            modifier = Modifier.testTag("card_${index}_${item.id}")
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 84.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    itemsIndexed(filteredKeys, key = { _, item -> item.id }) { index, item ->
                                        ApiKeyCard(
                                            item = item,
                                            actions = cardActions,
                                            modifier = Modifier.testTag("card_list_${index}_${item.id}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Clipboard banners positioned over top bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 56.dp, start = 16.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
        is VaultDialogState.Generator, is VaultDialogState.SecurityAudit -> Unit
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
