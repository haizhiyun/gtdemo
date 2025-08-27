package com.hzy.gitdemo.data.repository

import android.content.Context
import android.util.Log
import com.hzy.gitdemo.data.api.GitHubService
import com.hzy.gitdemo.data.auth.GitHubOAuthService
import com.hzy.gitdemo.data.local.SecureStorage
import com.hzy.gitdemo.data.model.IssueRequest
import com.hzy.gitdemo.data.model.IssueResponse
import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.data.model.SearchResponse
import com.hzy.gitdemo.data.model.User
import com.hzy.gitdemo.domain.repository.GitHubRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class GitHubRepositoryImpl constructor(
    private val api: GitHubService,
    private val oauthService: GitHubOAuthService,
    private val context: Context,
    private val secureStorage: SecureStorage
) : GitHubRepository {

    companion object {
        private const val TAG = "GitHubRepositoryImpl"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }

    private val _isAuthenticated = MutableStateFlow(false)
    private var authToken: String? = secureStorage.getString(KEY_AUTH_TOKEN)

    init {
        Log.d(TAG, "Initializing GitHubRepositoryImpl")
        // 检查是否有保存的 token
        authToken?.let {
            Log.d(TAG, "Found saved token, setting authenticated state to true")
            _isAuthenticated.value = true
        } ?: run {
            Log.d(TAG, "No saved token found, setting authenticated state to false")
            _isAuthenticated.value = false
        }
    }

    override suspend fun searchRepositories(
        query: String,
        language: String?,
        sort: String,
        order: String,
        page: Int,
        perPage: Int
    ): SearchResponse {
        if (query.isBlank()) {
            Log.i(TAG, "Empty query, returning empty search result")
            return SearchResponse(
                totalCount = 0,
                incompleteResults = false,
                items = emptyList()
            )
        }
        
        Log.i(TAG, "Searching repositories with query: $query")
        return api.searchRepositories(query, language, sort, order, page, perPage)
    }

    override suspend fun getRepository(owner: String, repo: String): Repository {
        Log.i(TAG, "Getting repository: $owner/$repo")
        return api.getRepository(owner, repo)
    }

    override suspend fun getUser(): User {
        val token = authToken ?: throw IllegalStateException("Not authenticated")
        return try {
            val response = api.getCurrentUser("Bearer $token")
            Log.i(TAG, "Got current user: ${response.login}")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user: ${e.message}", e)
            _isAuthenticated.value = false
            authToken = null
            secureStorage.remove(KEY_AUTH_TOKEN)
            throw e
        }
    }

    override suspend fun login(token: String): User {
        Log.i(TAG, "Logging in with token: ${token.take(10)}...")
        try {
            authToken = token
            secureStorage.saveString(KEY_AUTH_TOKEN, token)
            
            // Get current user to verify token
            Log.i(TAG, "Getting current user with token: ${token.take(10)}...")
            val user = getUser()
            Log.i(TAG, "Login successful for user: ${user.login}")
            _isAuthenticated.value = true
            return user
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}", e)
            _isAuthenticated.value = false
            authToken = null
            secureStorage.remove(KEY_AUTH_TOKEN)
            throw e
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        val token = authToken ?: return false
        
        return try {
            api.getCurrentUser("Bearer $token")
            _isAuthenticated.value = true
            true
        } catch (e: Exception) {
            // Token可能已过期或无效，清除认证状态
            authToken = null
            secureStorage.remove(KEY_AUTH_TOKEN)
            _isAuthenticated.value = false
            false
        }
    }

    override fun getAuthToken(): String? = authToken

    override suspend fun getUserRepositories(
        page: Int,
        perPage: Int
    ): List<Repository> {
        val token = authToken ?: throw IllegalStateException("Not authenticated")
        Log.i(TAG, "Getting user repositories with token: ${token.take(10)}...")
        return try {
            api.getUserRepositories("Bearer $token", page, perPage)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user repositories: ${e.message}", e)
            throw e
        }
    }

    override suspend fun logout() {
        Log.i(TAG, "Logging out user")
        _isAuthenticated.value = false
        authToken = null
        secureStorage.remove(KEY_AUTH_TOKEN)
    }

    override suspend fun createIssue(owner: String, repo: String, issue: IssueRequest): IssueResponse {
        val token = authToken ?: throw IllegalStateException("Not authenticated")
        Log.i(TAG, "Creating issue in $owner/$repo")
        return try {
            api.createIssue("Bearer $token", owner, repo, issue)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating issue: ${e.message}", e)
            throw e
        }
    }
} 