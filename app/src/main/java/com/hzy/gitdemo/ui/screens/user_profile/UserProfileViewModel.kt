package com.hzy.gitdemo.ui.screens.user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.data.model.User
import com.hzy.gitdemo.domain.repository.GitHubRepository
import com.hzy.gitdemo.domain.usecase.GetUserUseCase
import com.hzy.gitdemo.domain.usecase.GetUserRepositoriesUseCase
import com.hzy.gitdemo.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getUserRepositoriesUseCase: GetUserRepositoriesUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val gitHubRepository: GitHubRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()
    
    // 分页相关状态
    private var currentPage = 1
    private val pageSize = 20
    private var isLoading = false
    private var hasMoreData = true
    private val allRepositories = mutableListOf<Repository>()
    private var currentUser: User? = null

    init {
        checkAuthenticationStatus()
    }
    
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            val isAuthenticated = gitHubRepository.isAuthenticated()
            if (isAuthenticated) {
                loadUserProfile()
            } else {
                _uiState.value = UserProfileUiState.NotLoggedIn
            }
        }
    }

    fun loadUserProfile() {
        if (isLoading) return
        
        viewModelScope.launch {
            try {
                isLoading = true
                _uiState.value = UserProfileUiState.Loading
                
                // 重置分页状态
                currentPage = 1
                hasMoreData = true
                allRepositories.clear()
                
                // 获取用户信息
                currentUser = getUserUseCase()
                
                // 获取第一页仓库数据
                val repos = getUserRepositoriesUseCase(currentPage, pageSize)
                allRepositories.addAll(repos)
                
                // 检查是否还有更多数据
                hasMoreData = repos.size == pageSize
                
                _uiState.value = UserProfileUiState.Success(
                    user = currentUser!!,
                    repositories = allRepositories.toList(),
                    isLoadingMore = false,
                    hasMoreData = hasMoreData
                )
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error(e.message ?: "Unknown error")
            } finally {
                isLoading = false
            }
        }
    }
    
    fun loadMoreRepositories() {
        if (isLoading || !hasMoreData) return
        
        val currentState = _uiState.value
        if (currentState !is UserProfileUiState.Success) return
        
        viewModelScope.launch {
            try {
                isLoading = true
                
                // 更新UI状态显示加载中
                _uiState.value = currentState.copy(isLoadingMore = true)
                
                // 加载下一页数据
                currentPage++
                val newRepos = getUserRepositoriesUseCase(currentPage, pageSize)
                
                // 添加到现有列表
                allRepositories.addAll(newRepos)
                
                // 检查是否还有更多数据
                hasMoreData = newRepos.size == pageSize
                
                _uiState.value = UserProfileUiState.Success(
                    user = currentState.user,
                    repositories = allRepositories.toList(),
                    isLoadingMore = false,
                    hasMoreData = hasMoreData
                )
            } catch (e: Exception) {
                // 发生错误时回退页数
                currentPage--
                _uiState.value = currentState.copy(
                    isLoadingMore = false,
                    loadMoreError = e.message
                )
            } finally {
                isLoading = false
            }
        }
    }
    
    fun retryLoadMore() {
        val currentState = _uiState.value
        if (currentState is UserProfileUiState.Success && currentState.loadMoreError != null) {
            _uiState.value = currentState.copy(loadMoreError = null)
            loadMoreRepositories()
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                _uiState.value = UserProfileUiState.LoggedOut
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error(e.message ?: "Logout failed")
            }
        }
    }
}

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    object NotLoggedIn : UserProfileUiState()
    data class Success(
        val user: User,
        val repositories: List<Repository>,
        val isLoadingMore: Boolean = false,
        val hasMoreData: Boolean = true,
        val loadMoreError: String? = null
    ) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
    object LoggedOut : UserProfileUiState()
} 