package com.hzy.gitdemo.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.data.model.SearchResponse
import com.hzy.gitdemo.data.preference.SearchHistoryPreference
import com.hzy.gitdemo.domain.usecase.SearchRepositoriesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortBy {
    STARS,
    UPDATED
}

class SearchViewModel constructor(
    private val searchRepositoriesUseCase: SearchRepositoriesUseCase,
    private val searchHistoryPreference: SearchHistoryPreference
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Success(emptyList()))
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    // 搜索历史记录
    val searchHistory: StateFlow<List<String>> = searchHistoryPreference.getSearchHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var searchJob: Job? = null
    private var currentQuery: String = ""
    private var _selectedLanguage: String? = null
    val selectedLanguage: String? get() = _selectedLanguage
    private var _sortBy: SortBy = SortBy.STARS
    val sortBy: SortBy get() = _sortBy

    val availableLanguages = listOf(
        "Java",
        "Kotlin",
        "Python",
        "JavaScript",
        "TypeScript",
        "Go",
        "Rust",
        "C++",
        "C#",
        "Swift"
    )

    fun searchRepositories(query: String) {
        currentQuery = query
        performSearch()
    }
    
    /**
     * 搜索仓库并添加到历史记录
     */
    fun searchRepositoriesWithHistory(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                searchHistoryPreference.addSearchHistory(query)
            }
        }
        searchRepositories(query)
    }
    
    /**
     * 从历史记录中搜索
     */
    fun searchFromHistory(query: String) {
        currentQuery = query
        performSearch()
    }
    
    /**
     * 清除搜索历史记录
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryPreference.clearSearchHistory()
        }
    }
    
    /**
     * 删除指定的搜索历史记录
     */
    fun removeSearchHistory(query: String) {
        viewModelScope.launch {
            searchHistoryPreference.removeSearchHistory(query)
        }
    }

    fun setLanguage(language: String?) {
        _selectedLanguage = language
        performSearch()
    }

    fun setSortBy(sortBy: SortBy) {
        _sortBy = sortBy
        performSearch()
    }

    private fun performSearch() {
        searchJob?.cancel()
        if (currentQuery.isBlank()) {
            _uiState.value = SearchUiState.Success(emptyList())
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                delay(300) // Debounce search
                val languageQuery = _selectedLanguage?.let { "language:$it" } ?: ""
                val sortQuery = when (_sortBy) {
                    SortBy.STARS -> "stars"
                    SortBy.UPDATED -> "updated"
                }
                val fullQuery = "$currentQuery $languageQuery sort:$sortQuery"
                val response = searchRepositoriesUseCase(fullQuery)
                _uiState.value = SearchUiState.Success(response.items)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Search failed")
            }
        }
    }
}

sealed class SearchUiState {
    object Loading : SearchUiState()
    data class Success(val repositories: List<Repository>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
} 