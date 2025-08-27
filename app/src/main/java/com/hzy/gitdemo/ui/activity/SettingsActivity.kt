package com.hzy.gitdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.hzy.gitdemo.R
import com.hzy.gitdemo.MainActivity
import com.hzy.gitdemo.ui.screens.settings.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsViewModel by viewModel()
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var languageCard: MaterialCardView
    private lateinit var languageRadioGroup: RadioGroup
    private lateinit var chineseRadio: RadioButton
    private lateinit var englishRadio: RadioButton
    
    // 标志位，防止初始化时触发重启
    private var isInitializing = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupViews()
        observeViewModel()
    }
    
    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        languageCard = findViewById(R.id.languageCard)
        languageRadioGroup = findViewById(R.id.languageRadioGroup)
        chineseRadio = findViewById(R.id.chineseRadio)
        englishRadio = findViewById(R.id.englishRadio)
        
        // 设置Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)
        
        // 设置语言选择监听
        languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (!isInitializing) {
                when (checkedId) {
                    R.id.chineseRadio -> {
                        viewModel.selectLanguage("zh")
                        recreateApp()
                    }
                    R.id.englishRadio -> {
                        viewModel.selectLanguage("en")
                        recreateApp()
                    }
                }
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState.currentLanguage) {
                    "zh" -> chineseRadio.isChecked = true
                    "en" -> englishRadio.isChecked = true
                }
                // 初始化完成后，允许监听器生效
                isInitializing = false
            }
        }
    }
    
    private fun recreateApp() {
        // 重启应用以应用语言更改
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}