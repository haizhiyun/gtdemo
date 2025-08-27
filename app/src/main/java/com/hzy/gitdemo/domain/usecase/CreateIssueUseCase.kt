package com.hzy.gitdemo.domain.usecase

import com.hzy.gitdemo.data.model.IssueRequest
import com.hzy.gitdemo.data.model.IssueResponse
import com.hzy.gitdemo.domain.repository.GitHubRepository

class CreateIssueUseCase constructor(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(
        owner: String,
        repo: String,
        issue: IssueRequest
    ): IssueResponse = repository.createIssue(owner, repo, issue)
} 