package com.hzy.gitdemo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.*

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_settings")

class LanguagePreference(private val context: Context) {
    
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        const val LANGUAGE_CHINESE = "zh"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_SYSTEM = "system"
    }
    
    val selectedLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: LANGUAGE_CHINESE // 默认中文
    }
    
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language
        }
    }
    
    suspend fun getCurrentLanguage(): String {
        return selectedLanguage.first()
    }
    
    fun getLanguageDisplayName(language: String, context: Context): String {
        return when (language) {
            LANGUAGE_CHINESE -> "简体中文"
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_SYSTEM -> {
                val systemLang = Locale.getDefault().language
                if (systemLang == "zh") {
                    "简体中文"
                } else {
                    "English"
                }
            }
            else -> "简体中文"
        }
    }
    
    fun getLocaleFromLanguage(language: String): Locale {
        return when (language) {
            LANGUAGE_CHINESE -> Locale("zh", "CN")
            LANGUAGE_ENGLISH -> Locale("en", "US")
            LANGUAGE_SYSTEM -> Locale.getDefault()
            else -> Locale("zh", "CN")
        }
    }
}