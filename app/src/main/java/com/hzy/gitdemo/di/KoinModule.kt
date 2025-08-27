package com.hzy.gitdemo.di

import com.hzy.gitdemo.data.api.GitHubService
import com.hzy.gitdemo.data.auth.GitHubOAuthService
import com.hzy.gitdemo.data.local.LanguagePreference
import com.hzy.gitdemo.data.local.SecureStorage
import com.hzy.gitdemo.data.preference.SearchHistoryPreference
import com.hzy.gitdemo.data.repository.GitHubRepositoryImpl
import com.hzy.gitdemo.domain.repository.GitHubRepository
import com.hzy.gitdemo.domain.usecase.*
import com.hzy.gitdemo.ui.screens.login.LoginFormViewModel
import com.hzy.gitdemo.ui.screens.login.LoginViewModel
import com.hzy.gitdemo.ui.screens.main.MainViewModel
import com.hzy.gitdemo.ui.screens.popular.PopularRepositoriesViewModel
import com.hzy.gitdemo.ui.screens.repo_details.RepoDetailsViewModel
import com.hzy.gitdemo.ui.screens.create_issue.CreateIssueViewModel
import com.hzy.gitdemo.ui.screens.search.SearchViewModel
import com.hzy.gitdemo.ui.screens.settings.SettingsViewModel
import com.hzy.gitdemo.ui.screens.user_profile.UserProfileViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    // Network dependencies
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    single<GitHubService> {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubService::class.java)
    }

    single<GitHubOAuthService> {
        Retrofit.Builder()
            .baseUrl("https://github.com/login/oauth/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubOAuthService::class.java)
    }

    // Repository dependencies
    single<SecureStorage> {
        SecureStorage(androidContext())
    }
    
    single<LanguagePreference> {
        LanguagePreference(androidContext())
    }
    
    single<SearchHistoryPreference> {
        SearchHistoryPreference(androidContext())
    }

    single<GitHubRepository> {
        GitHubRepositoryImpl(
            api = get(),
            oauthService = get(),
            context = androidContext(),
            secureStorage = get()
        )
    }

    // Use cases
    single { LoginUseCase(get()) }
    single { LogoutUseCase(get()) }
    single { IsAuthenticatedUseCase(get()) }
    single { GetUserUseCase(get()) }
    single { GetUserRepositoriesUseCase(get()) }
    single { GetUserProfileUseCase(get()) }
    single { GetRepositoryDetailsUseCase(get()) }
    single { CreateIssueUseCase(get()) }
    single { SearchRepositoriesUseCase(get()) }

    // ViewModels
    viewModel { LoginViewModel(get(), get()) }
    viewModel { LoginFormViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { PopularRepositoriesViewModel(get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { UserProfileViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), androidContext()) }
    viewModel { CreateIssueViewModel(get()) }
    viewModel { parameters -> 
        RepoDetailsViewModel(get(), parameters.get()) 
    }
}