package com.hzy.gitdemo.ui.screens.create_issue

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hzy.gitdemo.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateIssueActivity : AppCompatActivity() {
    private val viewModel: CreateIssueViewModel by viewModel()
    
    private lateinit var toolbar: MaterialToolbar
    private lateinit var repoInfoCard: MaterialCardView
    private lateinit var repoNameTextView: TextView
    private lateinit var titleInputLayout: TextInputLayout
    private lateinit var titleEditText: TextInputEditText
    private lateinit var bodyInputLayout: TextInputLayout
    private lateinit var bodyEditText: TextInputEditText
    private lateinit var createButton: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    
    companion object {
        const val EXTRA_OWNER = "extra_owner"
        const val EXTRA_REPO = "extra_repo"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_issue)
        
        val owner = intent.getStringExtra(EXTRA_OWNER) ?: ""
        val repo = intent.getStringExtra(EXTRA_REPO) ?: ""
        
        setupViews(owner, repo)
        observeViewModel()
    }
    
    private fun setupViews(owner: String, repo: String) {
        toolbar = findViewById(R.id.toolbar)
        repoInfoCard = findViewById(R.id.repoInfoCard)
        repoNameTextView = findViewById(R.id.repoNameTextView)
        titleInputLayout = findViewById(R.id.titleInputLayout)
        titleEditText = findViewById(R.id.titleEditText)
        bodyInputLayout = findViewById(R.id.bodyInputLayout)
        bodyEditText = findViewById(R.id.bodyEditText)
        createButton = findViewById(R.id.createButton)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        
        // 设置Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "创建Issue"
        
        // 设置仓库信息
        repoNameTextView.text = "$owner/$repo"
        
        // 设置创建按钮点击事件
        createButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val body = bodyEditText.text.toString().trim()
            
            if (title.isNotEmpty()) {
                viewModel.createIssue(owner, repo, title, body)
            } else {
                titleInputLayout.error = "请输入Issue标题"
            }
        }
        
        // 清除错误信息
        titleEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                titleInputLayout.error = null
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when {
                    uiState.isLoading -> {
                        createButton.isEnabled = false
                        progressBar.visibility = View.VISIBLE
                        errorTextView.visibility = View.GONE
                    }
                    uiState.isSuccess -> {
                        finish()
                    }
                    uiState.error != null -> {
                        createButton.isEnabled = true
                        progressBar.visibility = View.GONE
                        errorTextView.visibility = View.VISIBLE
                        errorTextView.text = uiState.error
                    }
                    else -> {
                        createButton.isEnabled = true
                        progressBar.visibility = View.GONE
                        errorTextView.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}