package com.hzy.gitdemo.data.auth

object GitHubOAuthConfig {
    const val CLIENT_ID = "Iv23liMr3TkHUhly3nkG"
    const val CLIENT_SECRET = "9777efca9e94782cca9f241f434f34c76cca2d9b"
    const val REDIRECT_URI = "mygithubos://oauth/callback"
    const val AUTH_URL = "https://github.com/login/oauth/authorize"
    const val TOKEN_URL = "https://github.com/login/oauth/access_token"
    const val SCOPE = "repo,user"
    
    fun getAuthorizationUrl(): String {
        return "$AUTH_URL?client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&scope=$SCOPE"
    }
} 