package com.hzy.gitdemo.ui.screens.create_issue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.gitdemo.data.model.IssueRequest
import com.hzy.gitdemo.domain.usecase.CreateIssueUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateIssueUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val hasAttemptedSubmit: Boolean = false
)

class CreateIssueViewModel constructor(
    private val createIssueUseCase: CreateIssueUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateIssueUiState())
    val uiState: StateFlow<CreateIssueUiState> = _uiState.asStateFlow()

    fun createIssue(owner: String, repo: String, title: String, body: String) {
        _uiState.update { it.copy(hasAttemptedSubmit = true) }
        
        if (title.isBlank()) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    error = null,
                    isSuccess = false
                ) 
            }
            
            try {
                createIssueUseCase(owner, repo, IssueRequest(title, body))
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "创建Issue失败，请重试"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}