package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ApiKeyItem
import com.example.data.model.ProviderPreset
import com.example.data.model.ProviderPresets
import com.example.data.repository.ApiKeyRepository
import com.example.data.security.VaultSecurity
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    RECENT("Recently Added"),
    MOST_USED("Most Copied"),
    ALPHABETICAL("A to Z"),
    EXPIRING_SOON("Rotation Due")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
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

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ApiKeyRepository
    private val clipboardManager: ClipboardManager =
        application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var autoClearJob: Job? = null
    private var lastSelfCopiedKey: String? = null

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        checkClipboardForApiKey()
    }

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = ApiKeyRepository(database.apiKeyDao())
        try {
            clipboardManager.addPrimaryClipChangedListener(clipListener)
        } catch (_: Exception) { }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            clipboardManager.removePrimaryClipChangedListener(clipListener)
        } catch (_: Exception) { }
    }

    val allKeys: StateFlow<List<ApiKeyItem>> = repository.allKeys
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedEnvironment = MutableStateFlow("All")
    val selectedEnvironment: StateFlow<String> = _selectedEnvironment.asStateFlow()

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

    val filteredKeys: StateFlow<List<ApiKeyItem>> = combine(
        allKeys,
        _searchQuery,
        _selectedCategory,
        _selectedEnvironment,
        _sortOption
    ) { keys, query, category, environment, sort ->
        keys.asSequence()
            .filter { item ->
                if (query.isBlank()) true else {
                    val q = query.trim().lowercase()
                    item.title.lowercase().contains(q) ||
                            item.provider.lowercase().contains(q) ||
                            item.tags.lowercase().contains(q) ||
                            item.environment.lowercase().contains(q) ||
                            item.notes.lowercase().contains(q)
                }
            }
            .filter { category == "All" || it.category.equals(category, ignoreCase = true) }
            .filter { environment == "All" || it.environment.equals(environment, ignoreCase = true) }
            .toList()
            .sortedWithOption(sort)
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedEnvironment(env: String) {
        _selectedEnvironment.value = env
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

    fun deleteKey(item: ApiKeyItem) {
        viewModelScope.launch {
            repository.deleteKey(item)
            if (_dialogState.value is VaultDialogState.KeyDetail || _dialogState.value is VaultDialogState.EditKey) {
                closeDialog()
            }
        }
    }

    fun togglePin(item: ApiKeyItem) {
        viewModelScope.launch { repository.togglePin(item.id, !item.isPinned) }
    }

    fun importKeys(items: List<ApiKeyItem>) {
        viewModelScope.launch {
            repository.insertAll(items)
            closeDialog()
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
}

private fun List<ApiKeyItem>.sortedWithOption(sort: SortOption): List<ApiKeyItem> = when (sort) {
    SortOption.RECENT -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenByDescending { it.createdAt })
    SortOption.MOST_USED -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenByDescending { it.copyCount })
    SortOption.ALPHABETICAL -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenBy { it.title.lowercase() })
    SortOption.EXPIRING_SOON -> sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenBy { it.expiresAt ?: Long.MAX_VALUE })
}
