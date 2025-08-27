package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.data.model.User
import com.hzy.gitdemo.domain.repository.GitHubRepository

class LoginUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(token: String): User = repository.login(token)
} 