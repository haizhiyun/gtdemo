package com.hzy.gitdemo.data.model

import com.google.gson.annotations.SerializedName

data class Repository(
    val id: Long,
    val name: String,
    @SerializedName("full_name")
    val fullName: String,
    val description: String?,
    val owner: User,
    @SerializedName("stargazers_count")
    val stargazersCount: Int,
    @SerializedName("watchers_count")
    val watchersCount: Int,
    @SerializedName("forks_count")
    val forksCount: Int,
    val language: String?,
    @SerializedName("html_url")
    val htmlUrl: String,
    @SerializedName("default_branch")
    val defaultBranch: String,
    val topics: List<String> = emptyList()
) 