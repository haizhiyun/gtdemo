package com.hzy.gitdemo.data.auth

object GitHubOAuthConfig {
    const val CLIENT_ID = "Ov23li4b9hQWEPtwnwtE"
    const val CLIENT_SECRET = "3977ce25d68d91b644dc3d21878d51d2378d9142"
    const val REDIRECT_URI = "gtdemo://oauth/callback"
    const val AUTH_URL = "https://github.com/login/oauth/authorize"
    const val TOKEN_URL = "https://github.com/login/oauth/access_token"
    const val SCOPE = "repo,user"
    
    fun getAuthorizationUrl(): String {
        return "$AUTH_URL?client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&scope=$SCOPE"
    }
} 