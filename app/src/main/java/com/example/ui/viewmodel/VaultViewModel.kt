package com.example.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableStateFlow
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApiKeyItem
import com.example.data.repository.ApiKeyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import com.example.util.AutoLockTimeout

data class DisplayMode(
    val isGrid: Boolean,
    val label: String
)

object DisplayMode {
    val Grid = DisplayMode(isGrid = true, label = "Grid")
    val List = DisplayMode(isGrid = false, label = "List")
}

data class AutoLockTimeout(
    val minutes: Int,
    val label: String
)

object AutoLockTimeout {
    val Minutes1 = AutoLockTimeout(minutes = 1, label = "1 min")
    val Minutes5 = AutoLockTimeout(minutes = 5, label = "5 min")
    val Minutes15 = AutoLockTimeout(minutes = 15, label = "15 min")
    val Minutes30 = AutoLockTimeout(minutes = 30, label = "30 min")
    val Hours1 = AutoLockTimeout(minutes = 60, label = "1 hour")
    val Custom = AutoLockTimeout(minutes = 0, label = "Custom...")

    val presets = listOf(Minutes1, Minutes5, Minutes15, Minutes30, Hours1)
}

class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val _displayMode = MutableStateFlow(DisplayMode.Grid)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    private val _displayModePreferenceKey = "display_mode_preference"

    private val _autoLockTimeout = MutableStateFlow(AutoLockTimeout.Minutes15)
    val autoLockTimeout: StateFlow<AutoLockTimeout> = _autoLockTimeout.asStateFlow()

    private val _autoLockTimeoutPreferenceKey = "auto_lock_timeout_preference"

    init {
        // Restore saved preferences
        val prefs = application.getApplicationContext().getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        val savedMode = prefs.getString(_displayModePreferenceKey, "grid")
        if (savedMode == "list") {
            _displayMode.value = DisplayMode.List
        } else {
            _displayMode.value = DisplayMode.Grid
        }
        // Restore auto-lock timeout
        val savedTimeout = prefs.getString(_autoLockTimeoutPreferenceKey, "15min")
        when (savedTimeout) {
            "1min" -> _autoLockTimeout.value = AutoLockTimeout.Minutes1
            "5min" -> _autoLockTimeout.value = AutoLockTimeout.Minutes5
            "15min" -> _autoLockTimeout.value = AutoLockTimeout.Minutes15
            "30min" -> _autoLockTimeout.value = AutoLockTimeout.Minutes30
            "60min" -> _autoLockTimeout.value = AutoLockTimeout.Hours1
            else -> _autoLockTimeout.value = AutoLockTimeout.Minutes15
        }
    }

    fun setDisplayMode(mode: DisplayMode) {
        _displayMode.value = mode
        // Persist preference
        val prefs = application.getApplicationContext().getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(_displayModePreferenceKey, mode.label).apply()
    }

    fun setAutoLockTimeout(timeout: AutoLockTimeout) {
        _autoLockTimeout.value = timeout
        // Persist preference
        val prefs = application.getApplicationContext().getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(_autoLockTimeoutPreferenceKey, timeout.label).apply()
    }
}