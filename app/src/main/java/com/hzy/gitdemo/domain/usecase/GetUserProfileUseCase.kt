package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.data.model.User
import com.hzy.gitdemo.domain.repository.GitHubRepository

class GetUserProfileUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(): User {
        return repository.getUser()
    }
} 