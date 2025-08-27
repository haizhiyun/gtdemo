package com.hzy.gitdemo.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.hzy.gitdemo.R
import com.hzy.gitdemo.ui.screens.create_issue.CreateIssueActivity
import com.hzy.gitdemo.ui.screens.repo_details.RepoDetailsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RepoDetailsActivity : AppCompatActivity() {
    private val viewModel: RepoDetailsViewModel by viewModel()
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var repoNameTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var languageTextView: TextView
    private lateinit var starsTextView: TextView
    private lateinit var watchersTextView: TextView
    private lateinit var forksTextView: TextView
    private lateinit var topicsChipGroup: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repo_details)
        
        val owner = intent.getStringExtra("owner") ?: ""
        val repo = intent.getStringExtra("repo") ?: ""
        
        setupViews()
        observeViewModel()
        
        // 加载数据
        viewModel.loadRepositoryDetails(owner, repo)
    }

    private fun setupViews() {
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        repoNameTextView = findViewById(R.id.repoNameTextView)
        descriptionTextView = findViewById(R.id.descriptionTextView)
        languageTextView = findViewById(R.id.languageTextView)
        starsTextView = findViewById(R.id.starsTextView)
        watchersTextView = findViewById(R.id.watchersTextView)
        forksTextView = findViewById(R.id.forksTextView)
        topicsChipGroup = findViewById(R.id.topicsChipGroup)
        
        // 设置Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.project_details)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when {
                    uiState.isLoading -> {
                        progressBar.visibility = View.VISIBLE
                        errorTextView.visibility = View.GONE
                        hideRepositoryDetails()
                    }
                    uiState.error != null -> {
                        progressBar.visibility = View.GONE
                        errorTextView.visibility = View.VISIBLE
                        errorTextView.text = uiState.error
                        hideRepositoryDetails()
                    }
                    uiState.repository != null -> {
                        progressBar.visibility = View.GONE
                        errorTextView.visibility = View.GONE
                        showRepositoryDetails(uiState.repository)
                    }
                }
            }
        }
    }

    private fun showRepositoryDetails(repository: com.hzy.gitdemo.data.model.Repository) {
        repoNameTextView.visibility = View.VISIBLE
        repoNameTextView.text = repository.fullName
        
        if (!repository.description.isNullOrBlank()) {
            descriptionTextView.visibility = View.VISIBLE
            descriptionTextView.text = repository.description
        } else {
            descriptionTextView.visibility = View.GONE
        }
        
        if (!repository.language.isNullOrEmpty()) {
            languageTextView.visibility = View.VISIBLE
            languageTextView.text = "Language: ${repository.language}"
        } else {
            languageTextView.visibility = View.GONE
        }
        
        starsTextView.text = repository.stargazersCount.toString()
        watchersTextView.text = repository.watchersCount.toString()
        forksTextView.text = repository.forksCount.toString()
        
        // 显示Topics
        if (repository.topics.isNotEmpty()) {
            topicsChipGroup.visibility = View.VISIBLE
            topicsChipGroup.removeAllViews()
            repository.topics.forEach { topic ->
                val chip = Chip(this)
                chip.text = topic
                chip.isClickable = false
                topicsChipGroup.addView(chip)
            }
        } else {
            topicsChipGroup.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_repo_details, menu)
        return true
    }

    private fun hideRepositoryDetails() {
        repoNameTextView.visibility = View.GONE
        descriptionTextView.visibility = View.GONE
        languageTextView.visibility = View.GONE
        topicsChipGroup.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_create_issue -> {
                val owner = intent.getStringExtra("owner") ?: ""
                val repo = intent.getStringExtra("repo") ?: ""
                val intent = Intent(this, CreateIssueActivity::class.java).apply {
                    putExtra(CreateIssueActivity.EXTRA_OWNER, owner)
                    putExtra(CreateIssueActivity.EXTRA_REPO, repo)
                }
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}