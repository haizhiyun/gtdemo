package com.hzy.gitdemo.data.model

data class IssueRequest(
    val title: String,
    val body: String
)

data class IssueResponse(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String,
    val state: String,
    val created_at: String,
    val updated_at: String
) 