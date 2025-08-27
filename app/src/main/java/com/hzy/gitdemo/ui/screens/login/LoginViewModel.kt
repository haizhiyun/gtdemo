package com.hzy.gitdemo.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.gitdemo.data.auth.GitHubOAuthConfig
import com.hzy.gitdemo.data.auth.GitHubOAuthService
import com.hzy.gitdemo.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val authUrl: String = "${GitHubOAuthConfig.AUTH_URL}?" +
            "client_id=${GitHubOAuthConfig.CLIENT_ID}&" +
            "redirect_uri=${GitHubOAuthConfig.REDIRECT_URI}&" +
            "scope=${GitHubOAuthConfig.SCOPE}",
    val isLoggedIn: Boolean = false
)

class LoginViewModel constructor(
    private val loginUseCase: LoginUseCase,
    private val oAuthService: GitHubOAuthService
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun handleOAuthCallback(code: String) {
        Log.i(TAG, "Handling OAuth callback with code: ${code.take(10)}...")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                Log.i(TAG, "Getting access token...")
                val response = oAuthService.getAccessToken(
                    clientId = GitHubOAuthConfig.CLIENT_ID,
                    clientSecret = GitHubOAuthConfig.CLIENT_SECRET,
                    code = code,
                    redirectUri = GitHubOAuthConfig.REDIRECT_URI
                )
                Log.i(TAG, "Received access token response: ${response.access_token.take(10)}...")
                
                if (response.access_token.isNotEmpty()) {
                    try {
                        Log.i(TAG, "Attempting to login with use case...")
                        loginUseCase(response.access_token)
                        Log.i(TAG, "Login successful")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                error = null
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Use case login failed: ${e.message}", e)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                isLoggedIn = false,
                                error = "Failed to login: ${e.message}"
                            )
                        }
                    }
                } else {
                    Log.e(TAG, "Access token is empty")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            error = "Failed to get access token"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "OAuth process failed: ${e.message}", e)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        error = e.message ?: "Login failed"
                    )
                }
            }
        }
    }
} 