package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.domain.repository.GitHubRepository

class GetRepositoryDetailsUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(owner: String, repo: String): Repository {
        return repository.getRepository(owner, repo)
    }
} 