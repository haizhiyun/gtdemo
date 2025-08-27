package com.hzy.gitdemo.data.auth

import android.util.Log
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

private const val TAG = "GitHubOAuthService"

interface GitHubOAuthService {
    @FormUrlEncoded
    @POST("access_token")
    suspend fun getAccessToken(
        @Header("Accept") accept: String = "application/json",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): AccessTokenResponse
}

data class AccessTokenResponse(
    val access_token: String,
    val token_type: String,
    val scope: String,
    val expires_in: Int,
    val refresh_token: String,
    val refresh_token_expires_in: Int
) 