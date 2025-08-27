package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.domain.repository.GitHubRepository

class IsAuthenticatedUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(): Boolean = repository.isAuthenticated()
} 