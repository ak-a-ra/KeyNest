package com.example.feature.vault

import androidx.core.content.edit
import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.AppDatabase
import com.example.core.files.VaultFileManager
import com.example.core.model.ApiKeyItem
import com.example.core.model.ProviderKeyItem
import com.example.core.model.ProviderPreset
import com.example.core.model.ProviderPresets
import com.example.core.model.ProviderProfile
import com.example.core.network.ConnectionResult
import com.example.core.network.ProviderConnectionTester
import com.example.core.repository.ApiKeyRepository
import com.example.core.repository.ProviderRepository
import com.example.core.repository.UNDECRYPTABLE_PLACEHOLDER
import com.example.core.security.VaultBackupCrypto
import com.example.core.security.VaultSecurity
import com.example.core.util.ApiKeyFormatting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class FilterCriteria(
    val query: String,
    val category: String,
    val tag: String?,
    val onlyFavorites: Boolean
)

enum class SortOption(val label: String) {
    RECENT("Recently Added"),
    MOST_USED("Most Copied"),
    ALPHABETICAL("A to Z"),
    EXPIRING_SOON("Rotation Due"),
    PINNED_FIRST("Pinned / Favorites First")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class VaultViewMode {
    ALL_SECRETS,
    TRASH
}

sealed interface VaultDialogState {
    object None : VaultDialogState
    data class AddKey(val preset: ProviderPreset? = null, val initialKey: String = "") : VaultDialogState
    data class EditKey(val item: ApiKeyItem) : VaultDialogState
    data class KeyDetail(val item: ApiKeyItem) : VaultDialogState
    data class ConfigureProvider(val profile: ProviderProfile) : VaultDialogState
    data class AddCustomProvider(val preset: ProviderPreset? = null) : VaultDialogState
    object Generator : VaultDialogState
    object DotEnvExport : VaultDialogState
    object DotEnvImport : VaultDialogState
    object SecurityAudit : VaultDialogState
    object PinSettings : VaultDialogState
    data class BackupRestore(val initialTab: Int = 0) : VaultDialogState
}

data class CopyFeedback(
    val title: String,
    val isSecret: Boolean = false,
    val message: String = "Copied to clipboard"
)

data class ClipboardCopyState(
    val label: String,
    val totalSeconds: Int = 30,
    val secondsRemaining: Int = 30
)

data class DisplayMode(
    val isGrid: Boolean,
    val label: String
) {
    companion object {
        val Grid = DisplayMode(isGrid = true, label = "Grid")
        val List = DisplayMode(isGrid = false, label = "List")
    }
}

data class AutoLockTimeout(
    val minutes: Int,
    val label: String
) {
    companion object {
        val Minutes1 = AutoLockTimeout(minutes = 1, label = "1 min")
        val Minutes5 = AutoLockTimeout(minutes = 5, label = "5 min")
        val Minutes15 = AutoLockTimeout(minutes = 15, label = "15 min")
        val Minutes30 = AutoLockTimeout(minutes = 30, label = "30 min")
        val Hours1 = AutoLockTimeout(minutes = 60, label = "1 hour")
        val Custom = AutoLockTimeout(minutes = 0, label = "Custom...")

        val presets = listOf(Minutes1, Minutes5, Minutes15, Minutes30, Hours1)
    }
}

class VaultViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ApiKeyRepository
    private val providerRepository: ProviderRepository
    private val clipboardManager: ClipboardManager? =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private var autoClearJob: Job? = null
    private var lastSelfCopiedKey: String? = null
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkClipboardForApiKey()
    }

    private val envVarSanitizeRegex = Regex("[^A-Z0-9]")

    val allKeys: StateFlow<List<ApiKeyItem>>
    val trashedKeys: StateFlow<List<ApiKeyItem>>
    val trashCount: StateFlow<Int>

    // Provider Profiles (Agora Architecture)
    val allProviders: StateFlow<List<ProviderProfile>>
    val trashedProviders: StateFlow<List<ProviderProfile>>
    val providerTrashCount: StateFlow<Int>
    val filteredProviders: StateFlow<List<ProviderProfile>>
    val configuredProvidersCount: StateFlow<Int>
    val activeProvidersCount: StateFlow<Int>

    private val _connectionResults = MutableStateFlow<Map<String, ConnectionResult>>(emptyMap())
    val connectionResults: StateFlow<Map<String, ConnectionResult>> = _connectionResults.asStateFlow()

    private val _testingProviders = MutableStateFlow<Set<String>>(emptySet())
    val testingProviders: StateFlow<Set<String>> = _testingProviders.asStateFlow()

    val cipherError: StateFlow<Boolean>

    private val _currentViewMode = MutableStateFlow(VaultViewMode.ALL_SECRETS)
    val currentViewMode: StateFlow<VaultViewMode> = _currentViewMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _onlyFavorites = MutableStateFlow(false)
    val onlyFavorites: StateFlow<Boolean> = _onlyFavorites.asStateFlow()

    val favoritesCount: StateFlow<Int>

    private val _sortOption = MutableStateFlow(SortOption.RECENT)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _dialogState = MutableStateFlow<VaultDialogState>(VaultDialogState.None)
    val dialogState: StateFlow<VaultDialogState> = _dialogState.asStateFlow()

    private val _clipboardDetectedKey = MutableStateFlow<String?>(null)
    val clipboardDetectedKey: StateFlow<String?> = _clipboardDetectedKey.asStateFlow()

    private val _isVaultLocked = MutableStateFlow(VaultSecurity.isPinSet(application))
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    private val _isPinConfigured = MutableStateFlow(VaultSecurity.isPinSet(application))
    val isPinConfigured: StateFlow<Boolean> = _isPinConfigured.asStateFlow()

    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(VaultSecurity.getThemeMode(application))
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _copyFeedbackEvent = MutableSharedFlow<CopyFeedback>()
    val copyFeedbackEvent = _copyFeedbackEvent.asSharedFlow()

    private val _clipboardCopyState = MutableStateFlow<ClipboardCopyState?>(null)
    val clipboardCopyState: StateFlow<ClipboardCopyState?> = _clipboardCopyState.asStateFlow()

    private val _displayMode = MutableStateFlow(DisplayMode.Grid)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _displayModePreferenceKey = "display_mode_preference"

    private val _autoLockTimeout = MutableStateFlow(AutoLockTimeout.Minutes15)
    val autoLockTimeout: StateFlow<AutoLockTimeout> = _autoLockTimeout.asStateFlow()

    private val _autoLockTimeoutPreferenceKey = "auto_lock_timeout_preference"

    private val fileManager = VaultFileManager(application)

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _debouncedSearchQuery = MutableStateFlow("")

    val filteredKeys: StateFlow<List<ApiKeyItem>>
    val availableTags: StateFlow<List<String>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ApiKeyRepository(database.apiKeyDao())
        providerRepository = ProviderRepository(database.providerDao())

        try {
            clipboardManager?.addPrimaryClipChangedListener(clipListener)
        } catch (_: Exception) { }

        allKeys = repository.allKeys
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        allProviders = providerRepository.allProviders
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        trashedProviders = providerRepository.trashedProviders
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        providerTrashCount = providerRepository.trashCount
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        configuredProvidersCount = allProviders
            .map { list -> list.count { it.isConfigured } }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        activeProvidersCount = allProviders
            .map { list -> list.count { it.isActive && it.isConfigured } }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        availableTags = allKeys
            .map { keys ->
                val set = mutableSetOf<String>()
                for (k in keys) {
                    set.addAll(ApiKeyFormatting.parseTags(k.tags))
                }
                set.sorted()
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        trashedKeys = repository.trashedKeys
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        cipherError = combine(repository.allKeys, repository.trashedKeys) { keys, trashed ->
            (keys + trashed).any {
                it.apiKey == UNDECRYPTABLE_PLACEHOLDER || it.secretKey == UNDECRYPTABLE_PLACEHOLDER
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

        trashCount = repository.trashCount
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        favoritesCount = allProviders
            .map { list -> list.count { it.isPinned } }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        // Restore saved preferences
        val prefs = application.applicationContext.getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        val savedMode = prefs.getString(_displayModePreferenceKey, "Grid")
        _displayMode.value = if (savedMode == "List") DisplayMode.List else DisplayMode.Grid

        val savedTimeout = prefs.getString(_autoLockTimeoutPreferenceKey, "15 min")
        _autoLockTimeout.value = AutoLockTimeout.presets.firstOrNull { it.label == savedTimeout } ?: AutoLockTimeout.Minutes15

        @OptIn(kotlinx.coroutines.FlowPreview::class)
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    _debouncedSearchQuery.value = query
                    _isSearching.value = false
                }
        }

        val searchFilterFlow = combine(_debouncedSearchQuery, _selectedCategory, _selectedTag, _onlyFavorites) { q, cat, tag, fav ->
            FilterCriteria(q, cat, tag, fav)
        }

        filteredKeys = combine(
            allKeys,
            searchFilterFlow,
            _sortOption
        ) { keys, criteria, sort ->
            val query = criteria.query
            val category = criteria.category
            val tagFilter = criteria.tag
            val onlyFavs = criteria.onlyFavorites
            keys.asSequence()
                .filter { item ->
                    if (query.isBlank()) true else {
                        val q = query.trim()
                        val masked = VaultSecurity.maskKey(item.apiKey)
                        item.title.contains(q, ignoreCase = true) ||
                                item.provider.contains(q, ignoreCase = true) ||
                                item.category.contains(q, ignoreCase = true) ||
                                item.tags.contains(q, ignoreCase = true) ||
                                item.endpointUrl.contains(q, ignoreCase = true) ||
                                item.modelOrProject.contains(q, ignoreCase = true) ||
                                item.organizationId.contains(q, ignoreCase = true) ||
                                item.notes.contains(q, ignoreCase = true) ||
                                masked.contains(q, ignoreCase = true) ||
                                (q.startsWith("#") && item.tags.contains(q.removePrefix("#"), ignoreCase = true)) ||
                                (q.startsWith("tag:") && item.tags.contains(q.removePrefix("tag:"), ignoreCase = true))
                    }
                }
                .filter { category == "All" || it.category.equals(category, ignoreCase = true) }
                .filter { tagFilter == null || ApiKeyFormatting.hasTag(it.tags, tagFilter) }
                .filter { !onlyFavs || it.isPinned }
                .toList()
                .sortedWithOption(sort)
        }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredProviders = combine(
            allProviders,
            searchFilterFlow
        ) { providers, criteria ->
            val query = criteria.query
            val category = criteria.category
            val tagFilter = criteria.tag
            val onlyFavs = criteria.onlyFavorites
            providers.asSequence()
                .filter { p ->
                    if (query.isBlank()) true else {
                        val q = query.trim()
                        p.displayName.contains(q, ignoreCase = true) ||
                                p.category.contains(q, ignoreCase = true) ||
                                p.baseUrl.contains(q, ignoreCase = true) ||
                                p.notes.contains(q, ignoreCase = true) ||
                                p.tags.contains(q, ignoreCase = true) ||
                                p.keys.any { k -> k.label.contains(q, ignoreCase = true) }
                    }
                }
                .filter { category == "All" || it.category.equals(category, ignoreCase = true) }
                .filter { tagFilter == null || it.tags.contains(tagFilter, ignoreCase = true) || it.keys.any { k -> k.label.contains(tagFilter, ignoreCase = true) } }
                .filter { !onlyFavs || it.isPinned }
                .toList()
        }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Ensure default presets exist on first run
        seedDefaultProvidersIfEmpty()
    }

    private fun seedDefaultProvidersIfEmpty() {
        viewModelScope.launch {
            val existing = providerRepository.allProviders.first()
            if (existing.isEmpty()) {
                val defaultProfiles = ProviderPresets.list.map { preset ->
                    ProviderProfile(
                        id = preset.id.ifBlank { preset.name.lowercase().replace(" ", "_") },
                        category = preset.category,
                        displayName = preset.name,
                        baseUrl = preset.defaultEndpoint,
                        colorHex = preset.defaultColorHex,
                        isActive = true,
                        keys = emptyList()
                    )
                }
                providerRepository.insertAll(defaultProfiles)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipListener)
        } catch (_: Exception) { }
    }

    fun setDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
        val prefs = getApplication<Application>().applicationContext.getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        prefs.edit { putString(_displayModePreferenceKey, mode.label) }
    }

    fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        _autoLockTimeout.value = timeout
        val prefs = getApplication<Application>().applicationContext.getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        prefs.edit { putString(_autoLockTimeoutPreferenceKey, timeout.label) }
    }

    fun setSearchQuery(query: String) {
        _isSearching.value = true
        _searchQuery.value = query
    }

    fun clearSearch() {
        _isSearching.value = false
        _searchQuery.value = ""
        _debouncedSearchQuery.value = ""
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setViewMode(mode: VaultViewMode) {
        _currentViewMode.value = mode
    }

    fun setOnlyFavorites(only: Boolean) {
        _onlyFavorites.value = only
    }

    fun toggleTagFilter(tag: String) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun openDialog(state: VaultDialogState) {
        _dialogState.value = state
    }

    fun closeDialog() {
        _dialogState.value = VaultDialogState.None
    }

    fun deleteKey(item: ApiKeyItem) {
        softDeleteKey(item)
    }

    fun importKeys(items: List<ApiKeyItem>) {
        batchSaveKeys(items)
    }

    fun clearClipboard() {
        clearClipboardNow()
    }

    fun copyToClipboard(text: String, label: String, isSecret: Boolean = true, itemId: Long? = null) {
        copySecretValue(text, label, isSecret)
        if (itemId != null) {
            viewModelScope.launch {
                repository.recordCopy(itemId)
            }
        }
    }

    fun loadStarterTemplates() {
        seedDefaultProvidersIfEmpty()
    }

    fun setDialogState(state: VaultDialogState) {
        _dialogState.value = state
    }

    fun dismissDialog() {
        _dialogState.value = VaultDialogState.None
    }

    // Provider Profile Operations (Agora Architecture)
    fun testProviderConnection(profile: ProviderProfile, overrideKey: String? = null) {
        viewModelScope.launch {
            _testingProviders.value = _testingProviders.value + profile.id
            val result = ProviderConnectionTester.testConnection(profile, overrideKey)
            _connectionResults.value = _connectionResults.value + (profile.id to result)
            _testingProviders.value = _testingProviders.value - profile.id
        }
    }

    fun saveProvider(profile: ProviderProfile) {
        viewModelScope.launch {
            providerRepository.saveProvider(profile)
        }
    }

    fun deleteProvider(profile: ProviderProfile) {
        viewModelScope.launch {
            providerRepository.softDeleteProvider(profile.id)
        }
    }

    fun restoreProvider(id: String) {
        viewModelScope.launch {
            providerRepository.restoreProvider(id)
        }
    }

    fun permanentDeleteProvider(id: String) {
        viewModelScope.launch {
            providerRepository.permanentDeleteProvider(id)
        }
    }

    fun toggleProviderActive(profile: ProviderProfile, isActive: Boolean) {
        viewModelScope.launch {
            providerRepository.toggleActive(profile.id, isActive)
        }
    }

    fun toggleProviderPin(profile: ProviderProfile) {
        viewModelScope.launch {
            providerRepository.togglePin(profile.id, !profile.isPinned)
        }
    }

    fun setActiveKey(providerId: String, keyId: String) {
        viewModelScope.launch {
            providerRepository.setActiveKey(providerId, keyId)
        }
    }

    fun addKeyToProvider(providerId: String, keyItem: ProviderKeyItem) {
        viewModelScope.launch {
            providerRepository.addOrUpdateKey(providerId, keyItem)
        }
    }

    fun removeKeyFromProvider(providerId: String, keyId: String) {
        viewModelScope.launch {
            providerRepository.removeKey(providerId, keyId)
        }
    }

    fun openConfigureProvider(profile: ProviderProfile) {
        _dialogState.value = VaultDialogState.ConfigureProvider(profile)
    }

    fun openAddCustomProvider(preset: ProviderPreset? = null) {
        _dialogState.value = VaultDialogState.AddCustomProvider(preset)
    }

    fun getAggregatedDotEnv(): String {
        val providers = allProviders.value.filter { it.isActive && it.isConfigured }
        val sb = StringBuilder()
        sb.append("# KeyNest Aggregated Active Environment Keys\n")
        sb.append("# Generated at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}\n\n")

        for (p in providers) {
            val preset = ProviderPresets.findById(p.id)
            val varName = preset.envVarNameSuggestion.ifBlank { "${p.displayName.uppercase().replace(envVarSanitizeRegex, "_")}_API_KEY" }
            val key = p.activeApiKey
            if (key.isNotBlank()) {
                sb.append("$varName=$key\n")
            }
        }
        return sb.toString()
    }

    // Legacy Key Operations (Preserved)
    fun saveKey(item: ApiKeyItem) {
        viewModelScope.launch {
            repository.saveKey(item)
        }
    }

    fun batchSaveKeys(items: List<ApiKeyItem>) {
        viewModelScope.launch {
            repository.insertAll(items)
        }
    }

    fun softDeleteKey(item: ApiKeyItem) {
        viewModelScope.launch {
            repository.softDeleteKey(item.id)
        }
    }

    fun restoreKey(id: Long) {
        viewModelScope.launch {
            repository.restoreKey(id)
        }
    }

    fun permanentDeleteKey(id: Long) {
        viewModelScope.launch {
            repository.permanentDeleteKey(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            providerRepository.emptyTrash()
        }
    }

    fun togglePin(item: ApiKeyItem) {
        viewModelScope.launch {
            repository.togglePin(item.id, !item.isPinned)
        }
    }

    fun copySecretValue(secretText: String, label: String, isSecret: Boolean = true) {
        val mgr = clipboardManager ?: return
        lastSelfCopiedKey = secretText
        val clip = ClipData.newPlainText(label, secretText).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isSecret) {
                description.extras = PersistableBundle().apply {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                }
            }
        }
        mgr.setPrimaryClip(clip)

        viewModelScope.launch {
            _copyFeedbackEvent.emit(
                CopyFeedback(
                    title = label,
                    isSecret = isSecret,
                    message = if (isSecret) "$label copied (Auto-clears in 30s)" else "$label copied"
                )
            )
        }

        if (isSecret) {
            startClipboardAutoClear(label, secretText)
        }
    }

    fun copyActiveKeyForProvider(profile: ProviderProfile) {
        val activeKey = profile.activeKey
        if (activeKey != null && activeKey.apiKey.isNotBlank()) {
            copySecretValue(activeKey.apiKey, "${profile.displayName} (${activeKey.label})", isSecret = true)
            viewModelScope.launch {
                providerRepository.recordCopy(profile.id)
            }
        }
    }

    fun copyApiKey(item: ApiKeyItem) {
        copySecretValue(item.apiKey, item.title, isSecret = true)
        viewModelScope.launch {
            repository.recordCopy(item.id)
        }
    }

    fun copySecretKey(item: ApiKeyItem) {
        if (item.secretKey.isNotBlank()) {
            copySecretValue(item.secretKey, "${item.title} Secret", isSecret = true)
            viewModelScope.launch {
                repository.recordCopy(item.id)
            }
        }
    }

    private fun startClipboardAutoClear(label: String, expectedContent: String) {
        autoClearJob?.cancel()
        autoClearJob = viewModelScope.launch {
            for (remaining in 30 downTo 1) {
                _clipboardCopyState.value = ClipboardCopyState(label = label, totalSeconds = 30, secondsRemaining = remaining)
                delay(1000)
            }
            clearClipboardIfMatches(expectedContent)
            _clipboardCopyState.value = null
        }
    }

    fun clearClipboardNow() {
        autoClearJob?.cancel()
        clearClipboardIfMatches(null)
        _clipboardCopyState.value = null
    }

    private fun clearClipboardIfMatches(expected: String?) {
        val mgr = clipboardManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mgr.clearPrimaryClip()
            } else {
                mgr.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (_: Exception) { }
    }

    fun checkClipboardForApiKey() {
        val mgr = clipboardManager ?: return
        val item = mgr.primaryClip?.getItemAt(0)?.text?.toString()?.trim() ?: return
        if (item == lastSelfCopiedKey || item.isBlank()) return

        if (VaultSecurity.isLikelyApiKey(item)) {
            _clipboardDetectedKey.value = item
        }
    }

    fun dismissClipboardBanner() {
        _clipboardDetectedKey.value = null
    }

    fun unlockVault(pin: String): Boolean {
        val success = VaultSecurity.verifyPin(getApplication(), pin)
        if (success) {
            _isVaultLocked.value = false
        }
        return success
    }

    fun lockVault() {
        if (VaultSecurity.isPinSet(getApplication())) {
            _isVaultLocked.value = true
        }
    }

    fun setMasterPin(pin: String) {
        VaultSecurity.setMasterPin(getApplication(), pin)
        _isPinConfigured.value = true
        _isVaultLocked.value = false
    }

    fun removeMasterPin() {
        VaultSecurity.removeMasterPin(getApplication())
        _isPinConfigured.value = false
        _isVaultLocked.value = false
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        VaultSecurity.setThemeMode(getApplication(), mode.name)
    }

    fun cycleThemeMode() {
        val next = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
        }
        setThemeMode(next)
    }

    suspend fun exportTextFile(uri: Uri, content: String): Result<Unit> {
        return fileManager.exportTextFile(uri, content)
    }

    suspend fun importTextFile(uri: Uri): Result<String> {
        return fileManager.importTextFile(uri)
    }

    fun openBackupRestoreDialog(initialTab: Int = 0) {
        _dialogState.value = VaultDialogState.BackupRestore(initialTab)
    }

    suspend fun createAndExportBackup(uri: Uri, passphrase: CharArray, keys: List<ApiKeyItem>): Result<Int> {
        return withContext(Dispatchers.IO) {
            val backupJsonResult = VaultBackupCrypto.createEncryptedBackup(keys, passphrase)
            if (backupJsonResult.isFailure) {
                return@withContext Result.failure(backupJsonResult.exceptionOrNull() ?: Exception("Encryption failed"))
            }
            val jsonContent = backupJsonResult.getOrThrow()
            val writeResult = fileManager.exportTextFile(uri, jsonContent)
            if (writeResult.isFailure) {
                return@withContext Result.failure(writeResult.exceptionOrNull() ?: Exception("Write failed"))
            }
            Result.success(keys.size)
        }
    }

    suspend fun inspectBackupMetadata(uri: Uri): Result<VaultBackupCrypto.BackupMetadata> {
        return withContext(Dispatchers.IO) {
            val readResult = fileManager.importTextFile(uri)
            if (readResult.isFailure) {
                return@withContext Result.failure(readResult.exceptionOrNull() ?: Exception("Read failed"))
            }
            VaultBackupCrypto.peekBackupMetadata(readResult.getOrThrow())
        }
    }

    suspend fun restoreEncryptedBackup(uri: Uri, passphrase: CharArray, replaceExisting: Boolean): Result<Int> {
        return withContext(Dispatchers.IO) {
            val readResult = fileManager.importTextFile(uri)
            if (readResult.isFailure) {
                return@withContext Result.failure(readResult.exceptionOrNull() ?: Exception("Read failed"))
            }
            val content = readResult.getOrThrow()
            val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(content, passphrase)
            if (restoreResult.isFailure) {
                return@withContext Result.failure(restoreResult.exceptionOrNull() ?: Exception("Decryption failed"))
            }
            val restoredKeys = restoreResult.getOrThrow()
            if (replaceExisting) {
                repository.replaceAll(restoredKeys)
            } else {
                repository.insertAll(restoredKeys)
            }
            Result.success(restoredKeys.size)
        }
    }
}

private fun List<ApiKeyItem>.sortedWithOption(sort: SortOption): List<ApiKeyItem> = when (sort) {
    SortOption.RECENT -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenByDescending { it.createdAt })
    SortOption.MOST_USED -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenByDescending { it.copyCount })
    SortOption.ALPHABETICAL -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    SortOption.EXPIRING_SOON -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenBy { it.expiresAt ?: Long.MAX_VALUE })
    SortOption.PINNED_FIRST -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenByDescending { it.createdAt })
}
