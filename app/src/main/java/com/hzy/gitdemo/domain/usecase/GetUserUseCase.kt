package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.data.model.User
import com.hzy.gitdemo.domain.repository.GitHubRepository

class GetUserUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(): User = repository.getUser()
} 