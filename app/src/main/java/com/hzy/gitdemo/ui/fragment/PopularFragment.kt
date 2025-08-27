package com.hzy.gitdemo.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.hzy.gitdemo.R
import com.hzy.gitdemo.ui.activity.RepoDetailsActivity
import com.hzy.gitdemo.ui.adapter.RepositoryAdapter
import com.hzy.gitdemo.ui.screens.popular.PopularRepositoriesViewModel
import com.hzy.gitdemo.ui.screens.popular.PopularRepositoriesUiState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PopularFragment : Fragment() {
    private val viewModel: PopularRepositoriesViewModel by viewModel()
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var repositoryAdapter: RepositoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_popular, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupAdapter()
        observeViewModel()
        
        // 初始加载数据
        viewModel.loadPopularRepositories()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        
        // 设置网格布局
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        
        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadPopularRepositories()
        }
    }

    private fun setupAdapter() {
        repositoryAdapter = RepositoryAdapter(
            onItemClick = { repository ->
                // 点击仓库跳转到详情页
                val intent = Intent(requireContext(), RepoDetailsActivity::class.java).apply {
                    putExtra("owner", repository.owner.login)
                    putExtra("repo", repository.name)
                }
                startActivity(intent)
            }
        )
        recyclerView.adapter = repositoryAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is PopularRepositoriesUiState.Loading -> {
                        swipeRefreshLayout.isRefreshing = true
                    }
                    is PopularRepositoriesUiState.Success -> {
                        swipeRefreshLayout.isRefreshing = uiState.isLoadingMore
                        repositoryAdapter.submitRepositories(uiState.repositories)
                    }
                    is PopularRepositoriesUiState.Error -> {
                        swipeRefreshLayout.isRefreshing = false
                        // TODO: 显示错误信息
                    }
                }
            }
        }
    }
}