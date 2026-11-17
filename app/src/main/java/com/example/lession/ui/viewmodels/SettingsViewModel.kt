package com.example.lession.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _thresholdSetting = MutableStateFlow(3200)
    val thresholdSetting: StateFlow<Int> = _thresholdSetting.asStateFlow()

    private val _autoReconnectSetting = MutableStateFlow(true)
    val autoReconnectSetting: StateFlow<Boolean> = _autoReconnectSetting.asStateFlow()

    fun setThreshold(value: Int) {
        _thresholdSetting.value = value.coerceIn(500, 4000)
    }

    fun setAutoReconnect(enabled: Boolean) {
        _autoReconnectSetting.value = enabled
    }
}
