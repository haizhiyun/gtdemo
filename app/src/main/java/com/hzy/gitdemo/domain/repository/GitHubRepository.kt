package com.hzy.gitdemo.domain.repository

import com.hzy.gitdemo.data.model.IssueRequest
import com.hzy.gitdemo.data.model.IssueResponse
import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.data.model.SearchResponse
import com.hzy.gitdemo.data.model.User
import kotlinx.coroutines.flow.Flow

interface GitHubRepository {
    suspend fun searchRepositories(
        query: String,
        language: String? = null,
        sort: String = "stars",
        order: String = "desc",
        page: Int = 1,
        perPage: Int = 30
    ): SearchResponse

    suspend fun getRepository(owner: String, repo: String): Repository

    suspend fun getUser(): User

    suspend fun login(token: String): User

    suspend fun isAuthenticated(): Boolean

    fun getAuthToken(): String?

    suspend fun getUserRepositories(
        page: Int = 1,
        perPage: Int = 30
    ): List<Repository>

    suspend fun logout()

    suspend fun createIssue(owner: String, repo: String, issue: IssueRequest): IssueResponse
} 