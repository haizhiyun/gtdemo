package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.domain.repository.GitHubRepository

class LogoutUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke() = repository.logout()
} 