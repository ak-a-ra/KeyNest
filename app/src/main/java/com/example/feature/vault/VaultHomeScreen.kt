package com.example.feature.vault
import com.example.feature.settings.PinSettingsSheet
import com.example.feature.export.DotEnvExportSheet
import com.example.feature.keymanagement.KeyDetailSheet
import com.example.feature.keymanagement.AddEditKeySheet
import androidx.activity.compose.BackHandler
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import com.example.feature.keymanagement.KeyDetailPane
import com.example.core.model.ProviderPresets


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
import com.example.core.model.ApiKeyItem
import com.example.feature.vault.ApiKeyCard
import com.example.feature.vault.ClipboardAutoClearBanner
import com.example.feature.vault.ClipboardDetectionBanner
import com.example.feature.vault.EmptyKeysState
import com.example.feature.vault.GoogleKeepTopSearchBar
import com.example.feature.vault.KeyCardActions
import com.example.feature.vault.VaultDrawerSheetContent
import com.example.core.designsystem.ObsidianBg
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.feature.vault.VaultViewModel
import com.example.feature.vault.VaultDialogState
import com.example.feature.vault.DisplayMode
import kotlinx.coroutines.launch

import com.example.feature.vault.VaultTrashView
import com.example.feature.vault.VaultViewMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
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
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>()

    BackHandler(navigator.canNavigateBack()) {
        navigator.navigateBack()
    }


    val cardActions = remember(viewModel) {
        KeyCardActions(
            onClick = { item -> navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, item.id) },
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
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            modifier = modifier.fillMaxSize(),
            listPane = {
                AnimatedPane {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
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
            },
            detailPane = {
                AnimatedPane {
                    val currentId = navigator.currentDestination?.content
                    val item = allKeys.find { it.id == currentId }
                    if (item != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ObsidianBg)
                        ) {
                            KeyDetailPane(
                                item = item,
                                preset = ProviderPresets.findByName(item.provider),
                                onEdit = { viewModel.openDialog(VaultDialogState.EditKey(it)) },
                                onDelete = {
                                    viewModel.deleteKey(item)
                                    if (navigator.canNavigateBack()) {
                                        navigator.navigateBack()
                                    }
                                },
                                onTogglePin = { viewModel.togglePin(it) },
                                onCopyKey = { text, label, id ->
                                    viewModel.copyToClipboard(text, label, isSecret = true, itemId = id)
                                },
                                onClose = {
                                    if (navigator.canNavigateBack()) {
                                        navigator.navigateBack()
                                    }
                                }
                            )
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(ObsidianBg))
                    }
                }
            }
        )
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
            // Migrated to adaptive detail pane
            viewModel.closeDialog()
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, state.item.id)
        }
        is VaultDialogState.Generator, is VaultDialogState.SecurityAudit -> Unit
        is VaultDialogState.DotEnvExport -> {
            DotEnvExportSheet(
                viewModel = viewModel,
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
                viewModel = viewModel,
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
