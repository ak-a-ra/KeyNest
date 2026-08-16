package com.example.ui.screens

import androidx.compose.animation.AlphaAnimation
import androidx.compose.animation.AnimationSpec
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.snap {
    LastSnappingItemIndex(0, Edge.Start)
}
import androidx.compose.foundation.reminderLocalization
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ViewJustify
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Chip
import androidx.compose.material3.ChipGroup
import androidx.compose.material3.ChipGroup互State
import androidx.compose.material3.ChipGroup互SelectionMode
import androidx.compose.material3.ChipGroup互State
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.ModalFocusManager
import androidx.compose.ui.focus.findFocusManager
import androidx.compose.ui.focus.requestFocus
import androidx.compose.ui.focus.sense.isFocused
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.targetPixmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTags
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeyItem
import com.example.data.security.VaultSecurity
import com.example.ui.components.ApiKeyCard
import com.example.ui.components.ClipboardAutoClearBanner
import com.example.ui.components.ClipboardDetectionBanner
import com.example.ui.components.EmptyKeysState
import com.example.ui.components.GoogleKeepTopSearchBar
import com.example.ui.components.KeyCardActions
import com.example.ui.viewmodel.VaultViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val displayMode by viewModel.displayMode.collectAsStateWithLifecycle()

    val cardActions = remember(viewModel) {
        KeyCardActions(
            onClick = { item -> viewModel.openDialog(VaultDialogState.KeyDetail(item)) },
            onCopy = { item ->
                viewModel.copyToClipboard(item.apiKey, "${item.title} API Key", isSecret = true, itemId = item.id)
            },
            onTogglePin = { item -> viewModel.togglePin(item) }
        )
    }

    // Grid/List toggle chips
    var selectedChip by remember { mutableIntStateOf(0) }

    val gridListChips = remember {
        listOf(DisplayMode.Grid, DisplayMode.List).map { mode ->
            Chip(
                selected = selectedChip == when (mode) {
                    DisplayMode.Grid -> 0
                    DisplayMode.List -> 1
                    else -> 0
                },
                onSelected = {
                    selectedChip = when (mode) {
                        DisplayMode.Grid -> 1
                        DisplayMode.List -> 0
                        else -> 1
                    }
                    viewModel.setDisplayMode(mode)
                },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spedBy(4.dp)
                    ) {
                        Icon(
                            if (mode.isGrid) Icons.Default.GridOn else Icons.Default.List,
                            contentDescription = null,
                            tint = if (selectedChip == when (mode) {
                                DisplayMode.Grid -> 0
                                DisplayMode.List -> 1
                                else -> 0
                            }) CyberGold else CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = mode.label,
                            fontSize = 11.sp,
                            fontWeight = if (selectedChip == when (mode) {
                                DisplayMode.Grid -> 0
                                DisplayMode.List -> 1
                                else -> 0
                            }) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedChip == when (mode) {
                                DisplayMode.Grid -> 0
                                DisplayMode.List -> 1
                                else -> 0
                            }) CyberGold else CyberCyan
                        )
                    }
                },
                modifier = Modifier.padding(4.dp)
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
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
                },
                onSelectCategory = { category ->
                    viewModel.setSelectedCategory(category)
                },
                onSelectEnvironment = { env ->
                    viewModel.setSelectedEnvironment(env)
                },
                onOpenSecurityAudit = {
                    viewModel.openDialog(VaultDialogState.SecurityAudit)
                },
                onOpenGenerator = {
                    viewModel.openDialog(VaultDialogState.Generator)
                },
                onOpenDotEnvExport = {
                    viewModel.openDialog(VaultDialogState.DotEnvExport)
                },
                onCycleTheme = { viewModel.cycleThemeMode() },
                onToggleLockOrPinSettings = {
                    if (isPinConfigured) viewModel.lockVault() else viewModel.openDialog(VaultDialogState.PinSettings)
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
                        onOpenDrawer = { coroutineScope.launch { rememberDrawerState.open() } },
                        onOpenAudit = { viewModel.openDialog(VaultDialogState.SecurityAudit) }
                    )
                }

                // Grid/List Toggle Section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Display Mode:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    ChipGroup(
                        mutuallyExclusive = true,
                        value = selectedChip,
                        onValueSelected = { selectedChip ->
                            when (selectedChip) {
                                0 -> { selectedChip = 1; viewModel.setDisplayMode(DisplayMode.List) }
                                1 -> { selectedChip = 0; viewModel.setDisplayMode(DisplayMode.Grid) }
                            }
                        }
                    ) {
                        gridListChips.forEach { chip ->
                            it.let { addIt(it) }
                        }
                    }
                }

                // Clipboard banners
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
                    when (displayMode.isGrid) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 84.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalItemSpacing = 12.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(filteredKeys, key = { _, it -> it.id }) { index, item ->
                                ApiKeyCard(item = item, actions = cardActions, testTag = "card_${index}_${it.id}")
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 84.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(filteredKeys, key = { _, it -> it.id }) { index, item ->
                                ApiKeyCard(
                                    item = item,
                                    actions = cardActions,
                                    // List mode: single column, different layout
                                    testTag = "card_list_${index}_${it.id}"
                                )
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
        // Temporarily paused KeyGenerator & SecurityAudit
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