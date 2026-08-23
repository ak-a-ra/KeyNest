package com.example.feature.vault

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.example.core.database.AppDatabase
import com.example.core.files.VaultFileManager
import com.example.core.model.ApiKeyItem
import com.example.core.model.ProviderPreset
import com.example.core.model.ProviderPresets
import com.example.core.repository.ApiKeyRepository
import com.example.core.security.SecretCipherException
import com.example.core.security.VaultBackupCrypto
import com.example.core.security.VaultSecurity
import com.example.core.util.ApiKeyFormatting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val clipboardManager: ClipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var autoClearJob: Job? = null
    private var lastSelfCopiedKey: String? = null
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkClipboardForApiKey()
    }

    val allKeys: StateFlow<List<ApiKeyItem>>
    val trashedKeys: StateFlow<List<ApiKeyItem>>
    val trashCount: StateFlow<Int>

    private val _cipherError = MutableStateFlow(false)

    /** True when a row could not be decrypted (e.g. invalidated Keystore key); the vault stays usable. */
    val cipherError: StateFlow<Boolean> = _cipherError.asStateFlow()

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

    /** One undecryptable row must not crash collectors or hide all healthy entries — flag [cipherError] instead. */
    private fun Flow<List<ApiKeyItem>>.recoverFromCipherFailure(): Flow<List<ApiKeyItem>> =
        catch { e ->
            if (e is SecretCipherException) {
                _cipherError.value = true
                emit(emptyList())
            } else {
                throw e
            }
        }

    val filteredKeys: StateFlow<List<ApiKeyItem>>
    val availableTags: StateFlow<List<String>>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ApiKeyRepository(database.apiKeyDao())
        try {
            clipboardManager.addPrimaryClipChangedListener(clipListener)
        } catch (_: Exception) { }

        allKeys = repository.allKeys
            .recoverFromCipherFailure()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        availableTags = allKeys
            .map { keys ->
                keys.flatMap { ApiKeyFormatting.parseTags(it.tags) }
                    .distinct()
                    .sorted()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        trashedKeys = repository.trashedKeys
            .recoverFromCipherFailure()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        trashCount = repository.trashCount
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        favoritesCount = allKeys
            .map { list -> list.count { it.isPinned } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = 0
            )

        // Restore saved preferences
        val prefs = application.getApplicationContext().getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        val savedMode = prefs.getString(_displayModePreferenceKey, "Grid")
        _displayMode.value = if (savedMode == "List") DisplayMode.List else DisplayMode.Grid

        // Restore auto-lock timeout
        val savedTimeout = prefs.getString(_autoLockTimeoutPreferenceKey, "15 min")
        _autoLockTimeout.value = AutoLockTimeout.presets.firstOrNull { it.label == savedTimeout } ?: AutoLockTimeout.Minutes15

        val searchFilterFlow = combine(_searchQuery, _selectedCategory, _selectedTag, _onlyFavorites) { q, cat, tag, fav ->
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
                        val q = query.trim().lowercase()
                        val masked = VaultSecurity.maskKey(item.apiKey).lowercase()
                        item.title.lowercase().contains(q) ||
                                item.provider.lowercase().contains(q) ||
                                item.category.lowercase().contains(q) ||
                                item.tags.lowercase().contains(q) ||
                                item.endpointUrl.lowercase().contains(q) ||
                                item.modelOrProject.lowercase().contains(q) ||
                                item.organizationId.lowercase().contains(q) ||
                                item.notes.lowercase().contains(q) ||
                                masked.contains(q) ||
                                (q.startsWith("#") && item.tags.lowercase().contains(q.removePrefix("#"))) ||
                                (q.startsWith("tag:") && item.tags.lowercase().contains(q.removePrefix("tag:")))
                    }
                }
                .filter { category == "All" || it.category.equals(category, ignoreCase = true) }
                .filter { tagFilter == null || ApiKeyFormatting.parseTags(it.tags).any { tag -> tag.equals(tagFilter, ignoreCase = true) } }
                .filter { !onlyFavs || it.isPinned }
                .toList()
                .sortedWithOption(sort)
        }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    override fun onCleared() {
        super.onCleared()
        try {
            clipboardManager.removePrimaryClipChangedListener(clipListener)
        } catch (_: Exception) { }
    }

    fun setDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
        val prefs = getApplication<Application>().getApplicationContext().getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(_displayModePreferenceKey, mode.label).apply()
    }

    fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        _autoLockTimeout.value = timeout
        val prefs = getApplication<Application>().getApplicationContext().getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(_autoLockTimeoutPreferenceKey, timeout.label).apply()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun toggleTagFilter(tag: String) {
        _selectedTag.value = if (_selectedTag.value.equals(tag, ignoreCase = true)) null else tag
    }

    fun setOnlyFavorites(only: Boolean) {
        _onlyFavorites.value = only
    }

    fun toggleOnlyFavorites() {
        _onlyFavorites.value = !_onlyFavorites.value
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun openDialog(dialog: VaultDialogState) {
        _dialogState.value = dialog
    }

    fun closeDialog() {
        _dialogState.value = VaultDialogState.None
    }

    fun copyToClipboard(text: String, label: String, isSecret: Boolean = true, itemId: Long? = null) {
        lastSelfCopiedKey = text
        VaultSecurity.setLastSelfCopiedKey(getApplication(), text)
        val clip = ClipData.newPlainText(label, text)
        if (isSecret && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboardManager.setPrimaryClip(clip)
        if (itemId != null) {
            viewModelScope.launch { repository.recordCopy(itemId) }
        }
        viewModelScope.launch {
            _copyFeedbackEvent.emit(
                CopyFeedback(title = label, isSecret = isSecret, message = "Copied to clipboard")
            )
        }
        if (isSecret) {
            startClipboardAutoClearCountdown(label)
        }
    }

    private fun startClipboardAutoClearCountdown(label: String) {
        autoClearJob?.cancel()
        autoClearJob = viewModelScope.launch {
            val totalSeconds = 30
            for (sec in totalSeconds downTo 1) {
                _clipboardCopyState.value = ClipboardCopyState(
                    label = label,
                    totalSeconds = totalSeconds,
                    secondsRemaining = sec
                )
                delay(1000)
            }
            clearClipboard()
        }
    }

    fun clearClipboard() {
        autoClearJob?.cancel()
        autoClearJob = null
        _clipboardCopyState.value = null
        lastSelfCopiedKey = null
        VaultSecurity.setLastSelfCopiedKey(getApplication(), null)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboardManager.clearPrimaryClip()
            } else {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (_: Exception) { }
    }

    fun checkClipboardForApiKey() {
        try {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()?.trim()
                if (!text.isNullOrEmpty() && text.length in 16..512 && !text.contains("\n") && !text.contains(" ")) {
                    val persistedSelfCopied = VaultSecurity.getLastSelfCopiedKey(getApplication())
                    if (text == lastSelfCopiedKey || text == persistedSelfCopied) return
                    val detectedProvider = VaultSecurity.detectProviderFromKey(text)
                    val isPresetMatch = ProviderPresets.list.any { preset ->
                        preset.defaultPrefix.isNotEmpty() && text.startsWith(preset.defaultPrefix)
                    }
                    val isRecognizedKey = (detectedProvider != "Custom / Other") || isPresetMatch || (text.length >= 24)
                    val exists = allKeys.value.any { it.apiKey == text || it.secretKey == text }
                    if (!exists && isRecognizedKey) {
                        _clipboardDetectedKey.value = text
                        return
                    }
                }
            }
        } catch (_: Exception) { }
        _clipboardDetectedKey.value = null
    }

    fun dismissClipboardBanner() {
        _clipboardDetectedKey.value = null
    }

    fun saveKey(item: ApiKeyItem) {
        viewModelScope.launch {
            if (item.id == 0L) repository.insertKey(item) else repository.updateKey(item)
            closeDialog()
            _clipboardDetectedKey.value = null
        }
    }

    fun setViewMode(mode: VaultViewMode) {
        _currentViewMode.value = mode
    }

    fun moveToTrash(item: ApiKeyItem) {
        viewModelScope.launch {
            repository.softDeleteKey(item.id)
            if (_dialogState.value is VaultDialogState.KeyDetail || _dialogState.value is VaultDialogState.EditKey) {
                closeDialog()
            }
        }
    }

    fun restoreKey(item: ApiKeyItem) {
        viewModelScope.launch {
            repository.restoreKey(item.id)
        }
    }

    fun permanentDeleteKey(item: ApiKeyItem) {
        viewModelScope.launch {
            repository.permanentDeleteKey(item.id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
        }
    }

    fun deleteKey(item: ApiKeyItem) {
        moveToTrash(item)
    }

    fun togglePin(item: ApiKeyItem) {
        val newPinnedState = !item.isPinned
        viewModelScope.launch {
            repository.togglePin(item.id, newPinnedState)
            val currentDialog = _dialogState.value
            if (currentDialog is VaultDialogState.KeyDetail && currentDialog.item.id == item.id) {
                _dialogState.value = VaultDialogState.KeyDetail(item.copy(isPinned = newPinnedState))
            }
        }
    }

    fun importKeys(items: List<ApiKeyItem>) {
        viewModelScope.launch {
            repository.insertAll(items)
            closeDialog()
        }
    }

    fun loadStarterTemplates() {
        val now = System.currentTimeMillis()
        val sampleKeys = listOf(
            ApiKeyItem(
                title = "OpenAI Production GPT-4o",
                provider = "OpenAI",
                category = "AI & LLMs",
                environment = "Production",
                apiKey = "sample-openai-key-demo-placeholder-000000000",
                tags = "prod, ai, gpt4",
                notes = "Main backend inference key for production workloads",
                isPinned = true,
                colorHex = "#10A37F",
                createdAt = now
            ),
            ApiKeyItem(
                title = "Google Gemini 2.0 Flash API",
                provider = "Google Gemini",
                category = "AI & LLMs",
                environment = "Production",
                apiKey = "sample-google-gemini-key-demo-placeholder-000000",
                tags = "ai, gemini, multimodal",
                notes = "Multimodal reasoning & image processing pipeline",
                isPinned = true,
                colorHex = "#4285F4",
                createdAt = now - 86400000
            ),
            ApiKeyItem(
                title = "GitHub CI/CD Automation",
                provider = "GitHub",
                category = "Developer Tools",
                environment = "Staging",
                apiKey = "sample-github-token-demo-placeholder-000000",
                tags = "ci-cd, github-actions, deploy",
                notes = "Deployment token with repo and workflow scopes",
                isPinned = false,
                colorHex = "#24292E",
                createdAt = now - 172800000
            ),
            ApiKeyItem(
                title = "Stripe Billing Webhook Secret",
                provider = "Stripe",
                category = "Payments",
                environment = "Development",
                apiKey = "sample-stripe-webhook-secret-demo-placeholder-000",
                tags = "billing, payments, webhook",
                notes = "Local Stripe CLI test webhook signing secret",
                isPinned = false,
                colorHex = "#635BFF",
                createdAt = now - 259200000
            )
        )
        viewModelScope.launch {
            repository.insertAll(sampleKeys)
        }
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
    SortOption.ALPHABETICAL -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenBy { it.title.lowercase() })
    SortOption.EXPIRING_SOON -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenBy { it.expiresAt ?: Long.MAX_VALUE })
    SortOption.PINNED_FIRST -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenByDescending { it.createdAt })
}
