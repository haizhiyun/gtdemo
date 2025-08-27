package com.hzy.gitdemo.ui.screens.repo_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.domain.usecase.GetRepositoryDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RepoDetailsUiState(
    val repository: Repository? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class RepoDetailsViewModel constructor(
    private val getRepositoryDetailsUseCase: GetRepositoryDetailsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepoDetailsUiState())
    val uiState: StateFlow<RepoDetailsUiState> = _uiState.asStateFlow()



    init {
        val owner = savedStateHandle.get<String>("owner")
        val repo = savedStateHandle.get<String>("repo")
        if (owner != null && repo != null) {
            loadRepositoryDetails(owner, repo)
        }
    }

    fun loadRepositoryDetails(owner: String, repo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val repository = getRepositoryDetailsUseCase(owner, repo)
                _uiState.update { 
                    it.copy(
                        repository = repository,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = e.message ?: "An error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }


} 