package com.hzy.gitdemo.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hzy.gitdemo.R
import com.hzy.gitdemo.data.auth.GitHubOAuthConfig
import com.hzy.gitdemo.ui.screens.login.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModel()
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        setupWebView()
        observeViewModel()
    }

    private fun setupWebView() {
        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url?.startsWith(GitHubOAuthConfig.REDIRECT_URI) == true) {
                    val code = Uri.parse(url).getQueryParameter("code")
                    if (code != null) {
                        viewModel.handleOAuthCallback(code)
                        return true
                    }
                }
                return false
            }
        }
        
        // 加载GitHub OAuth URL
        webView.loadUrl(GitHubOAuthConfig.getAuthorizationUrl())
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                if (uiState.isLoggedIn) {
                    val intent = Intent(this@LoginActivity, MainActivityXml::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}