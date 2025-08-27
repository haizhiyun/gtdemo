package com.hzy.gitdemo.ui.screens.popular

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.domain.usecase.SearchRepositoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PopularRepositoriesViewModel constructor(
    private val searchRepositoriesUseCase: SearchRepositoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PopularRepositoriesUiState>(PopularRepositoriesUiState.Loading)
    val uiState: StateFlow<PopularRepositoriesUiState> = _uiState.asStateFlow()
    
    private var currentPage = 1
    private var isLoading = false
    private var hasMoreData = true
    private val allRepositories = mutableListOf<Repository>()

    fun loadPopularRepositories() {
        if (isLoading) return
        isLoading = true
        
        viewModelScope.launch {
            try {
                if (currentPage == 1) {
                    _uiState.value = PopularRepositoriesUiState.Loading
                    allRepositories.clear()
                }
                
                // 搜索星标数超过 1000 的仓库，按星标数降序排序
                val response = searchRepositoriesUseCase(
                    query = "stars:>1000",
                    sort = "stars",
                    order = "desc",
                    page = currentPage,
                    perPage = 20
                )
                
                allRepositories.addAll(response.items)
                hasMoreData = response.items.isNotEmpty() && response.items.size == 20
                currentPage++
                
                _uiState.value = PopularRepositoriesUiState.Success(
                    repositories = allRepositories.toList(),
                    isLoadingMore = false,
                    hasMoreData = hasMoreData
                )
            } catch (e: Exception) {
                if (currentPage == 1) {
                    _uiState.value = PopularRepositoriesUiState.Error(e.message ?: "Failed to load repositories")
                } else {
                    // 如果是加载更多失败，保持当前数据并显示错误
                    _uiState.value = PopularRepositoriesUiState.Success(
                        repositories = allRepositories.toList(),
                        isLoadingMore = false,
                        hasMoreData = hasMoreData,
                        error = e.message ?: "Failed to load more repositories"
                    )
                }
                currentPage-- // 回退页码
            } finally {
                isLoading = false
            }
        }
    }
    
    fun loadMoreRepositories() {
        if (!hasMoreData || isLoading) return
        
        // 设置加载更多状态
        val currentState = _uiState.value
        if (currentState is PopularRepositoriesUiState.Success) {
            _uiState.value = currentState.copy(isLoadingMore = true, error = null)
        }
        
        loadPopularRepositories()
    }
    
    fun refresh() {
        currentPage = 1
        hasMoreData = true
        loadPopularRepositories()
    }
}

sealed class PopularRepositoriesUiState {
    object Loading : PopularRepositoriesUiState()
    data class Success(
        val repositories: List<Repository>,
        val isLoadingMore: Boolean = false,
        val hasMoreData: Boolean = true,
        val error: String? = null
    ) : PopularRepositoriesUiState()
    data class Error(val message: String) : PopularRepositoriesUiState()
} 