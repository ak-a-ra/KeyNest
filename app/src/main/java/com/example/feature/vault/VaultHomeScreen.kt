package com.example.feature.vault

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.CyberCyan
import com.example.core.designsystem.CyberEmerald
import com.example.core.designsystem.CyberGold
import com.example.core.designsystem.ObsidianBg
import com.example.core.designsystem.ObsidianBorder
import com.example.core.designsystem.ObsidianBorderLight
import com.example.core.designsystem.ObsidianSurface
import com.example.core.designsystem.ObsidianSurfaceElevated
import com.example.core.designsystem.ObsidianSurfaceHighlight
import com.example.core.designsystem.StatusSuccess
import com.example.core.designsystem.TextPrimary
import com.example.core.designsystem.TextSecondary
import com.example.core.designsystem.TextTertiary
import com.example.core.model.ProviderPresets
import com.example.core.model.ProviderProfile
import com.example.feature.export.DotEnvExportSheet
import com.example.feature.export.VaultBackupSheet
import com.example.feature.keymanagement.AddEditKeySheet
import com.example.feature.keymanagement.KeyGeneratorSheet
import com.example.feature.keymanagement.ProviderConfigSheet
import com.example.feature.settings.PinSettingsSheet
import com.example.feature.settings.SecurityAuditSheet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    viewModel: VaultViewModel,
    onNavigateToSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allProviders by viewModel.allProviders.collectAsStateWithLifecycle()
    val trashedProviders by viewModel.trashedProviders.collectAsStateWithLifecycle()
    val filteredProviders by viewModel.filteredProviders.collectAsStateWithLifecycle()
    val filteredKeys by viewModel.filteredKeys.collectAsStateWithLifecycle()
    val connectionResults by viewModel.connectionResults.collectAsStateWithLifecycle()
    val testingProviders by viewModel.testingProviders.collectAsStateWithLifecycle()

    val allKeys by viewModel.allKeys.collectAsStateWithLifecycle()
    val trashedKeys by viewModel.trashedKeys.collectAsStateWithLifecycle()
    val trashCount by viewModel.trashCount.collectAsStateWithLifecycle()
    val currentViewMode by viewModel.currentViewMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val clipboardDetectedKey by viewModel.clipboardDetectedKey.collectAsStateWithLifecycle()
    val isPinConfigured by viewModel.isPinConfigured.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val clipboardCopyState by viewModel.clipboardCopyState.collectAsStateWithLifecycle()
    val displayMode by viewModel.displayMode.collectAsStateWithLifecycle()
    val availableTags by viewModel.availableTags.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    val onlyFavorites by viewModel.onlyFavorites.collectAsStateWithLifecycle()
    val favoritesCount by viewModel.favoritesCount.collectAsStateWithLifecycle()
    val configuredCount by viewModel.configuredProvidersCount.collectAsStateWithLifecycle()
    val activeCount by viewModel.activeProvidersCount.collectAsStateWithLifecycle()
    val categories = remember { ProviderPresets.categories.filterNot { it == "All" } }

    val cardActions = remember(viewModel) {
        KeyCardActions(
            onClick = { item -> viewModel.openDialog(VaultDialogState.KeyDetail(item)) },
            onCopy = { item ->
                viewModel.copyToClipboard(item.apiKey, "${item.title} API Key", isSecret = true, itemId = item.id)
            },
            onTogglePin = { item -> viewModel.togglePin(item) },
            onTagClick = { tag -> viewModel.toggleTagFilter(tag) }
        )
    }

    // Handle toast feedback events
    LaunchedEffect(Unit) {
        viewModel.copyFeedbackEvent.collectLatest { feedback ->
            Toast.makeText(context, feedback.message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (currentViewMode == VaultViewMode.ALL_SECRETS) {
                FloatingActionButton(
                    onClick = { viewModel.openAddCustomProvider() },
                    containerColor = CyberEmerald,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 3.dp,
                        pressedElevation = 6.dp
                    ),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(end = 6.dp, bottom = 6.dp)
                        .testTag("fab_add_provider")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Provider", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        Text("Add Provider", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
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
                        onBackToSecrets = { viewModel.setViewMode(VaultViewMode.ALL_SECRETS) },
                        onRestoreKey = { viewModel.restoreKey(it.id) },
                        onPermanentDeleteKey = { viewModel.permanentDeleteKey(it.id) },
                        onEmptyTrash = { viewModel.emptyTrash() }
                    )
                } else {
                    // Top Search & Navigation Bar
                    GoogleKeepTopSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSearchClick = onNavigateToSearch,
                        sortOption = sortOption,
                        onSortOptionChange = { viewModel.setSortOption(it) },
                        onOpenAudit = { viewModel.setDialogState(VaultDialogState.SecurityAudit) },
                        onOpenGenerator = { viewModel.setDialogState(VaultDialogState.Generator) },
                        onOpenDotEnvExport = { viewModel.setDialogState(VaultDialogState.DotEnvExport) },
                        onOpenBackupRestore = { viewModel.openBackupRestoreDialog(0) },
                        onOpenTrash = { viewModel.setViewMode(VaultViewMode.TRASH) },
                        onCycleTheme = { viewModel.cycleThemeMode() },
                        onToggleLockOrPinSettings = {
                            if (isPinConfigured) viewModel.lockVault() else viewModel.setDialogState(VaultDialogState.PinSettings)
                        },
                        trashCount = trashCount + trashedProviders.size,
                        isPinConfigured = isPinConfigured,
                        isGridView = displayMode.isGrid,
                        onToggleGridView = {
                            viewModel.setDisplayMode(if (displayMode.isGrid) DisplayMode.List else DisplayMode.Grid)
                        },
                        isSearching = isSearching
                    )

                        // Categories Carousel
                        VaultTagFilterCarousel(
                            tags = categories,
                            selectedTag = if (selectedCategory == "All") null else selectedCategory,
                            onlyFavorites = onlyFavorites,
                            favoritesCount = favoritesCount,
                            onTagSelected = { category ->
                                viewModel.setSelectedCategory(if (selectedCategory == category) "All" else category)
                            },
                            onClearTagFilter = { viewModel.setSelectedCategory("All") },
                            onToggleFavorites = { viewModel.toggleOnlyFavorites() }
                        )
                        
                        // Tags Carousel
                        if (availableTags.isNotEmpty()) {
                            VaultTagFilterCarousel(
                                tags = availableTags,
                                selectedTag = selectedTag,
                                onTagSelected = { tag -> viewModel.toggleTagFilter(tag) },
                                onClearTagFilter = { viewModel.setSelectedTag(null) }
                            )
                        }

                        // Agora Metrics & Quick Action Strip
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = ObsidianSurfaceHighlight,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = CyberEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "$activeCount Active",
                                            color = CyberEmerald,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "$configuredCount / ${allProviders.size} Configured",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Quick Ping All Configured
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ObsidianSurfaceElevated,
                                        border = BorderStroke(1.dp, ObsidianBorderLight),
                                        modifier = Modifier
                                            .clickable {
                                                allProviders.filter { it.isConfigured }.forEach { p ->
                                                    viewModel.testProviderConnection(p)
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                                            Text("Ping All", fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Quick Export .env
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ObsidianSurfaceElevated,
                                        border = BorderStroke(1.dp, ObsidianBorderLight),
                                        modifier = Modifier
                                            .clickable { viewModel.setDialogState(VaultDialogState.DotEnvExport) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = CyberGold, modifier = Modifier.size(13.dp))
                                            Text(".env", fontSize = 11.sp, color = CyberGold, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (filteredProviders.isEmpty() && filteredKeys.isEmpty()) {
                            EmptyKeysState(
                                hasQuery = searchQuery.isNotEmpty() || selectedCategory != "All" || selectedTag != null || onlyFavorites,
                                onImportFromNotes = { viewModel.setDialogState(VaultDialogState.DotEnvImport) },
                                onLoadSampleTemplates = { viewModel.setDialogState(VaultDialogState.AddCustomProvider()) }
                            )
                        } else {
                            if (displayMode.isGrid) {
                                LazyVerticalStaggeredGrid(
                                    columns = StaggeredGridCells.Fixed(1),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 84.dp),
                                    verticalItemSpacing = 12.dp,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredProviders, key = { it.id }) { provider ->
                                        val preset = remember(provider.id) { ProviderPresets.findById(provider.id) }
                                        ProviderCard(
                                            profile = provider,
                                            preset = preset,
                                            connectionState = connectionResults[provider.id],
                                            isTesting = testingProviders.contains(provider.id),
                                            onCardClick = { viewModel.openConfigureProvider(provider) },
                                            onTestConnection = { viewModel.testProviderConnection(provider) },
                                            onCopyActiveKey = { viewModel.copyActiveKeyForProvider(provider) },
                                            onToggleActive = { active -> viewModel.toggleProviderActive(provider, active) },
                                            onConfigure = { viewModel.openConfigureProvider(provider) }
                                        )
                                    }
                                    items(filteredKeys, key = { "key_${it.id}" }) { item ->
                                        ApiKeyCard(
                                            item = item,
                                            actions = cardActions,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 84.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(filteredProviders, key = { it.id }) { provider ->
                                        val preset = remember(provider.id) { ProviderPresets.findById(provider.id) }
                                        ProviderCard(
                                            profile = provider,
                                            preset = preset,
                                            connectionState = connectionResults[provider.id],
                                            isTesting = testingProviders.contains(provider.id),
                                            onCardClick = { viewModel.openConfigureProvider(provider) },
                                            onTestConnection = { viewModel.testProviderConnection(provider) },
                                            onCopyActiveKey = { viewModel.copyActiveKeyForProvider(provider) },
                                            onToggleActive = { active -> viewModel.toggleProviderActive(provider, active) },
                                            onConfigure = { viewModel.openConfigureProvider(provider) }
                                        )
                                    }
                                    items(filteredKeys, key = { "key_${it.id}" }) { item ->
                                        ApiKeyCard(
                                            item = item,
                                            actions = cardActions,
                                            modifier = Modifier.fillMaxWidth()
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
                                onClearNow = { viewModel.clearClipboardNow() }
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
                                onSave = {
                                    val detectedProviderName = ProviderPresets.detectProvider(rawKey)
                                    val matched = allProviders.find { it.displayName.equals(detectedProviderName, ignoreCase = true) }
                                    if (matched != null) {
                                        viewModel.openConfigureProvider(matched)
                                    } else {
                                        val preset = ProviderPresets.findByName(detectedProviderName)
                                        viewModel.openAddCustomProvider(preset)
                                    }
                                },
                                onDismiss = { viewModel.dismissClipboardBanner() }
                            )
                        }
                    }
                }
            }
        }

    // Sheets & Dialogs
    when (val state = dialogState) {
        is VaultDialogState.ConfigureProvider -> {
            val preset = remember(state.profile.id) { ProviderPresets.findById(state.profile.id) }
            ProviderConfigSheet(
                profile = state.profile,
                preset = preset,
                onDismiss = { viewModel.dismissDialog() },
                onSaveProfile = { viewModel.saveProvider(it) },
                onDeleteProfile = { viewModel.deleteProvider(state.profile) },
                onCopyKey = { viewModel.copySecretValue(it, "${state.profile.displayName} Key") }
            )
        }
        is VaultDialogState.AddCustomProvider -> {
            val preset = state.preset ?: ProviderPresets.findById("custom")
            val newProfile = remember {
                ProviderProfile(
                    id = UUID.randomUUID().toString(),
                    category = preset.category,
                    displayName = preset.name,
                    baseUrl = preset.defaultEndpoint,
                    colorHex = preset.defaultColorHex,
                    isActive = true,
                    keys = emptyList()
                )
            }
            ProviderConfigSheet(
                profile = newProfile,
                preset = preset,
                onDismiss = { viewModel.dismissDialog() },
                onSaveProfile = { viewModel.saveProvider(it) },
                onCopyKey = { viewModel.copySecretValue(it, "API Key") }
            )
        }
        is VaultDialogState.DotEnvExport -> {
            DotEnvExportSheet(
                viewModel = viewModel,
                keys = allKeys,
                isImportMode = false,
                onDismiss = { viewModel.dismissDialog() },
                onCopyAll = { content ->
                    viewModel.copySecretValue(content, ".env Export", isSecret = true)
                },
                onImportKeys = { items -> viewModel.batchSaveKeys(items) }
            )
        }
        is VaultDialogState.DotEnvImport -> {
            DotEnvExportSheet(
                viewModel = viewModel,
                keys = allKeys,
                isImportMode = true,
                onDismiss = { viewModel.dismissDialog() },
                onCopyAll = { content ->
                    viewModel.copySecretValue(content, ".env Export", isSecret = true)
                },
                onImportKeys = { items -> viewModel.batchSaveKeys(items) }
            )
        }
        is VaultDialogState.AddKey -> {
            AddEditKeySheet(
                existingItem = null,
                initialPreset = state.preset,
                initialKeyText = state.initialKey,
                existingTitles = allKeys.map { it.title },
                availableTags = availableTags,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { viewModel.saveKey(it) },
                onBatchSave = { items -> viewModel.batchSaveKeys(items) }
            )
        }
        is VaultDialogState.EditKey -> {
            AddEditKeySheet(
                existingItem = state.item,
                existingTitles = allKeys.filter { it.id != state.item.id }.map { it.title },
                availableTags = availableTags,
                onDismiss = { viewModel.dismissDialog() },
                onSave = { viewModel.saveKey(it) }
            )
        }
        is VaultDialogState.KeyDetail -> {
            viewModel.dismissDialog()
        }
        is VaultDialogState.Generator -> {
            KeyGeneratorSheet(
                onDismiss = { viewModel.dismissDialog() },
                onSaveToVault = { generatedKey ->
                    viewModel.setDialogState(VaultDialogState.AddKey(initialKey = generatedKey))
                },
                onCopy = { generatedKey ->
                    viewModel.copySecretValue(generatedKey, "Generated Secret", isSecret = true)
                }
            )
        }
        is VaultDialogState.SecurityAudit -> {
            SecurityAuditSheet(
                keys = allKeys,
                isPinEnabled = isPinConfigured,
                onDismiss = { viewModel.dismissDialog() },
                onSelectKey = { item ->
                    viewModel.dismissDialog()
                }
            )
        }
        is VaultDialogState.PinSettings -> {
            PinSettingsSheet(
                isPinCurrentlyEnabled = isPinConfigured,
                onDismiss = { viewModel.dismissDialog() },
                onSetPin = { pin -> viewModel.setMasterPin(pin) },
                onRemovePin = { viewModel.removeMasterPin() }
            )
        }
        is VaultDialogState.BackupRestore -> {
            VaultBackupSheet(
                viewModel = viewModel,
                keys = allKeys,
                initialTab = state.initialTab,
                onDismiss = { viewModel.dismissDialog() }
            )
        }
        VaultDialogState.None -> Unit
    }
}
