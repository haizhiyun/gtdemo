package com.hzy.gitdemo.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.hzy.gitdemo.R
import com.hzy.gitdemo.ui.activity.LoginActivity
import com.hzy.gitdemo.ui.activity.RepoDetailsActivity
import com.hzy.gitdemo.ui.activity.SettingsActivity
import com.hzy.gitdemo.ui.adapter.RepositoryAdapter
import com.hzy.gitdemo.ui.screens.user_profile.UserProfileViewModel
import com.hzy.gitdemo.ui.screens.user_profile.UserProfileUiState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserProfileFragment : Fragment() {
    private val viewModel: UserProfileViewModel by viewModel()
    private lateinit var toolbar: MaterialToolbar
    private lateinit var loginPromptContainer: View
    private lateinit var profileScrollView: View
    private lateinit var loginButton: MaterialButton
    private lateinit var avatarImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var loginTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var followersTextView: TextView
    private lateinit var followingTextView: TextView
    private lateinit var reposTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var repositoryAdapter: RepositoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupToolbar()
        setupAdapter()
        observeViewModel()
    }

    private fun setupViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        loginPromptContainer = view.findViewById(R.id.loginPromptContainer)
        profileScrollView = view.findViewById(R.id.profileScrollView)
        loginButton = view.findViewById(R.id.loginButton)
        avatarImageView = view.findViewById(R.id.avatarImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        loginTextView = view.findViewById(R.id.loginTextView)
        bioTextView = view.findViewById(R.id.bioTextView)
        followersTextView = view.findViewById(R.id.followersTextView)
        followingTextView = view.findViewById(R.id.followingTextView)
        reposTextView = view.findViewById(R.id.reposTextView)
        recyclerView = view.findViewById(R.id.recyclerView)
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        // 设置登录按钮点击事件
        loginButton.setOnClickListener {
            navigateToLogin()
        }
    }
    
    private fun setupToolbar() {
        // 设置标题
        toolbar.title = getString(R.string.profile_title)
        
        // 直接在Fragment的toolbar上设置菜单
        toolbar.inflateMenu(R.menu.profile_menu)
        
        // 设置菜单点击监听器
        toolbar.setOnMenuItemClickListener { menuItem ->
            Log.d("UserProfileFragment", "Toolbar menu item clicked: ${menuItem.itemId}")
            
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    Log.d("UserProfileFragment", "Settings menu item clicked")
                    try {
                        val intent = Intent(requireContext(), SettingsActivity::class.java)
                        Log.d("UserProfileFragment", "Starting SettingsActivity: ${intent.component}")
                        startActivity(intent)
                        Log.d("UserProfileFragment", "Settings activity started successfully")
                    } catch (e: Exception) {
                        Log.e("UserProfileFragment", "Error starting settings activity", e)
                        // 也尝试创建 Toast 显示错误
                        try {
                            android.widget.Toast.makeText(requireContext(), "设置页面启动失败: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        } catch (ignored: Exception) {}
                    }
                    true
                }
                R.id.action_logout -> {
                    Log.d("UserProfileFragment", "Logout menu item clicked")
                    viewModel.logout()
                    navigateToLogin()
                    true
                }
                else -> {
                    Log.d("UserProfileFragment", "Unknown menu item: ${menuItem.itemId}")
                    false
                }
            }
        }
        
        Log.d("UserProfileFragment", "Toolbar setup completed with menu")
    }

    private fun setupAdapter() {
        repositoryAdapter = RepositoryAdapter(
            onItemClick = { repository ->
                val intent = Intent(requireContext(), RepoDetailsActivity::class.java).apply {
                    putExtra("owner", repository.owner.login)
                    putExtra("repo", repository.name)
                }
                startActivity(intent)
            },
            onRetryClick = {
                viewModel.retryLoadMore()
            }
        )
        recyclerView.adapter = repositoryAdapter
        
        // 添加滚动监听器，在到达底部前3项时触发加载更多
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                
                // 当滚动到底部前3项时触发加载更多
                if (totalItemCount > 0 && lastVisibleItem >= totalItemCount - 3) {
                    val currentState = viewModel.uiState.value
                    if (currentState is UserProfileUiState.Success && 
                        currentState.hasMoreData && 
                        !currentState.isLoadingMore && 
                        currentState.loadMoreError == null) {
                        viewModel.loadMoreRepositories()
                    }
                }
            }
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is UserProfileUiState.Loading -> {
                        showLoading()
                    }
                    is UserProfileUiState.NotLoggedIn -> {
                        showLoginPrompt()
                    }
                    is UserProfileUiState.Success -> {
                        showProfile(uiState)
                    }
                    is UserProfileUiState.Error -> {
                        showProfile(null, uiState.message)
                    }
                    is UserProfileUiState.LoggedOut -> {
                        navigateToLogin()
                    }
                }
            }
        }
    }
    
    private fun showLoading() {
        loginPromptContainer.visibility = View.GONE
        profileScrollView.visibility = View.VISIBLE
        // TODO: 显示加载状态
    }
    
    private fun showLoginPrompt() {
        loginPromptContainer.visibility = View.VISIBLE
        profileScrollView.visibility = View.GONE
    }
    
    private fun showProfile(uiState: UserProfileUiState.Success? = null, errorMessage: String? = null) {
        loginPromptContainer.visibility = View.GONE
        profileScrollView.visibility = View.VISIBLE
        
        if (uiState != null) {
            val user = uiState.user
            // 更新用户信息
            Glide.with(this@UserProfileFragment)
                .load(user.avatarUrl)
                .placeholder(R.drawable.ic_person_24)
                .into(avatarImageView)
            
            nameTextView.text = user.name ?: user.login
            loginTextView.text = "@${user.login}"
            bioTextView.text = user.bio ?: "暂无简介"
            followersTextView.text = user.followers.toString()
            followingTextView.text = user.following.toString()
            reposTextView.text = user.publicRepos.toString()
            
            // 更新仓库列表，包括加载状态
            repositoryAdapter.submitRepositories(
                repositories = uiState.repositories,
                isLoadingMore = uiState.isLoadingMore,
                loadMoreError = uiState.loadMoreError
            )
        } else if (errorMessage != null) {
            // TODO: 显示错误信息
        }
    }
    
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
    }
}