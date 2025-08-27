package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.domain.repository.GitHubRepository

class GetUserRepositoriesUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(
        page: Int = 1,
        perPage: Int = 30
    ): List<Repository> = repository.getUserRepositories(page, perPage)
} 