package com.hzy.gitdemo

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.hzy.gitdemo.data.local.LanguagePreference
import com.hzy.gitdemo.di.appModule
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.*

class GitHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始Koin
        startKoin {
            androidContext(this@GitHubApplication)
            modules(appModule)
        }
        
        // 设置语言
        setupLanguage()
    }
    
    private fun setupLanguage() {
        try {
            val languagePreference = LanguagePreference(this)
            runBlocking {
                val selectedLanguage = languagePreference.getCurrentLanguage()
                val locale = languagePreference.getLocaleFromLanguage(selectedLanguage)
                applyLanguage(locale)
            }
        } catch (e: Exception) {
            // 如果读取语言设置失败，使用默认中文
            applyLanguage(Locale("zh", "CN"))
        }
    }
    
    private fun applyLanguage(locale: Locale) {
        Locale.setDefault(locale)
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
        
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        createConfigurationContext(configuration)
    }
} 