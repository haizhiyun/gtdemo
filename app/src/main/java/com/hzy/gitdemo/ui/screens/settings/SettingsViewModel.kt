package com.hzy.gitdemo.ui.screens.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.hzy.gitdemo.MainActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.gitdemo.data.local.LanguagePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val currentLanguage: String = LanguagePreference.LANGUAGE_CHINESE,
    val isLoading: Boolean = false
)

class SettingsViewModel(
    private val languagePreference: LanguagePreference,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            languagePreference.selectedLanguage.collect { language ->
                _uiState.value = _uiState.value.copy(currentLanguage = language)
            }
        }
    }

    fun selectLanguage(language: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                languagePreference.setLanguage(language)
                _uiState.value = _uiState.value.copy(
                    currentLanguage = language,
                    isLoading = false
                )
                
                // 重启应用以应用语言更改
                restartApplication()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun restartApplication() {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
        Runtime.getRuntime().exit(0)
    }

    fun getLanguageDisplayName(language: String): String {
        return languagePreference.getLanguageDisplayName(language, context)
    }
}