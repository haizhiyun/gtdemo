package com.hzy.gitdemo.data.api

import com.hzy.gitdemo.data.model.IssueRequest
import com.hzy.gitdemo.data.model.IssueResponse
import com.hzy.gitdemo.data.model.Repository
import com.hzy.gitdemo.data.model.SearchResponse
import com.hzy.gitdemo.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubService {
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("language") language: String?,
        @Query("sort") sort: String,
        @Query("order") order: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): SearchResponse

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Repository

    @GET("user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): User

    @GET("user/repos")
    suspend fun getUserRepositories(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int
    ): List<Repository>

    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body issue: IssueRequest
    ): IssueResponse
} 