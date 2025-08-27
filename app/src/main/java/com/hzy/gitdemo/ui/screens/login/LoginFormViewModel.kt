package com.hzy.gitdemo.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.gitdemo.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Base64

data class LoginFormUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class LoginFormViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LoginFormViewModel"
    }

    private val _uiState = MutableStateFlow(LoginFormUiState())
    val uiState: StateFlow<LoginFormUiState> = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        Log.i(TAG, "Attempting login for user: $username")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // GitHub API支持Personal Access Token
                // 如果password看起来像Personal Access Token，直接使用
                // 否则提示用户使用Personal Access Token
                
                if (isValidPersonalAccessToken(password)) {
                    Log.i(TAG, "Using Personal Access Token for authentication")
                    loginUseCase(password)
                    Log.i(TAG, "Login successful with Personal Access Token")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            error = null
                        )
                    }
                } else {
                    // 对于普通密码，我们提示用户使用Personal Access Token
                    Log.w(TAG, "Regular password detected, suggesting Personal Access Token")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            error = "GitHub API不支持密码登录，请使用Personal Access Token。\\n" +
                                    "1. 访问GitHub → Settings → Developer settings → Personal access tokens\\n" +
                                    "2. 生成新的token，选择必要的权限\\n" +
                                    "3. 将token作为密码输入"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login failed: ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("401") == true -> "认证失败，请检查用户名和Personal Access Token"
                    e.message?.contains("403") == true -> "访问被禁止，请检查token权限"
                    e.message?.contains("network") == true -> "网络连接失败，请检查网络"
                    else -> "登录失败: ${e.message}"
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isLoggedIn = false,
                        error = errorMessage
                    )
                }
            }
        }
    }
    
    private fun isValidPersonalAccessToken(token: String): Boolean {
        // GitHub Personal Access Token通常以ghp_开头，长度为40个字符
        // 或者是classic token格式（40个字符的十六进制）
        return when {
            token.startsWith("ghp_") && token.length >= 36 -> true
            token.startsWith("github_pat_") -> true
            token.length == 40 && token.all { it.isLetterOrDigit() } -> true
            else -> false
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}