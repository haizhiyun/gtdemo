package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.data.model.SearchResponse
import com.hzy.gitdemo.domain.repository.GitHubRepository

class SearchRepositoriesUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(
        query: String,
        language: String? = null,
        sort: String = "stars",
        order: String = "desc",
        page: Int = 1,
        perPage: Int = 30
    ): SearchResponse {
        // 构建搜索查询
        val searchQuery = buildString {
            append(query)
            if (!language.isNullOrBlank()) {
                append(" language:$language")
            }
        }
        
        return repository.searchRepositories(
            query = searchQuery,
            sort = sort,
            order = order,
            page = page,
            perPage = perPage
        )
    }
} 