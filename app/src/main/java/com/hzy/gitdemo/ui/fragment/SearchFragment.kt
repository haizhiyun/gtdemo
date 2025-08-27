package com.hzy.gitdemo.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.hzy.gitdemo.R
import com.hzy.gitdemo.ui.activity.RepoDetailsActivity
import com.hzy.gitdemo.ui.adapter.RepositoryAdapter
import com.hzy.gitdemo.ui.adapter.SearchHistoryAdapter
import com.hzy.gitdemo.ui.screens.search.SearchViewModel
import com.hzy.gitdemo.ui.screens.search.SearchUiState
import com.hzy.gitdemo.ui.screens.search.SortBy
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by viewModel()
    
    // Views
    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageButton
    private lateinit var filterButton: ImageButton
    private lateinit var historyContainer: View
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var clearHistoryButton: MaterialButton
    private lateinit var recyclerView: RecyclerView
    
    // Adapters
    private lateinit var repositoryAdapter: RepositoryAdapter
    private lateinit var historyAdapter: SearchHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupAdapters()
        setupListeners()
        observeViewModel()
    }

    private fun setupViews(view: View) {
        searchEditText = view.findViewById(R.id.searchEditText)
        clearButton = view.findViewById(R.id.clearButton)
        filterButton = view.findViewById(R.id.filterButton)
        historyContainer = view.findViewById(R.id.historyContainer)
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView)
        clearHistoryButton = view.findViewById(R.id.clearHistoryButton)
        recyclerView = view.findViewById(R.id.recyclerView)
        
        // 设置 RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupAdapters() {
        // 仓库搜索结果适配器
        repositoryAdapter = RepositoryAdapter(
            onItemClick = { repository ->
                val intent = Intent(requireContext(), RepoDetailsActivity::class.java).apply {
                    putExtra("owner", repository.owner.login)
                    putExtra("repo", repository.name)
                }
                startActivity(intent)
            }
        )
        recyclerView.adapter = repositoryAdapter
        
        // 搜索历史适配器
        historyAdapter = SearchHistoryAdapter(
            onItemClick = { query ->
                searchEditText.setText(query)
                searchEditText.setSelection(query.length)
                viewModel.searchRepositories(query)
                hideHistory()
            },
            onDeleteClick = { query ->
                viewModel.removeSearchHistory(query)
            }
        )
        historyRecyclerView.adapter = historyAdapter
    }

    private fun setupListeners() {
        // 搜索框输入监听
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                clearButton.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                
                // 显示/隐藏历史记录和搜索结果
                if (query.isEmpty()) {
                    showHistory()
                } else {
                    hideHistory()
                    viewModel.searchRepositories(query)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        // 搜索框回车监听
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchRepositoriesWithHistory(query)
                    searchEditText.clearFocus()
                }
                true
            } else {
                false
            }
        }
        
        // 清除按钮
        clearButton.setOnClickListener {
            searchEditText.setText("")
            searchEditText.clearFocus()
        }
        
        // 过滤按钮
        filterButton.setOnClickListener {
            showFilterDialog()
        }
        
        // 清空历史按钮
        clearHistoryButton.setOnClickListener {
            viewModel.clearSearchHistory()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // 观察搜索结果
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is SearchUiState.Loading -> {
                        // TODO: 显示加载状态
                    }
                    is SearchUiState.Success -> {
                        repositoryAdapter.submitRepositories(uiState.repositories)
                    }
                    is SearchUiState.Error -> {
                        // TODO: 显示错误信息
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            // 观察搜索历史
            viewModel.searchHistory.collect { history ->
                historyAdapter.submitList(history)
                clearHistoryButton.visibility = if (history.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
    
    private fun showHistory() {
        historyContainer.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }
    
    private fun hideHistory() {
        historyContainer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
    
    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_search_filter, null)
        
        // 初始化视图
        val sortRadioGroup = view.findViewById<RadioGroup>(R.id.sortRadioGroup)
        val languageAutoComplete = view.findViewById<MaterialAutoCompleteTextView>(R.id.languageAutoComplete)
        val resetButton = view.findViewById<MaterialButton>(R.id.resetButton)
        val applyButton = view.findViewById<MaterialButton>(R.id.applyButton)
        
        // 设置当前选中状态
        when (viewModel.sortBy) {
            SortBy.STARS -> view.findViewById<View>(R.id.sortByStarsRadio).performClick()
            SortBy.UPDATED -> view.findViewById<View>(R.id.sortByUpdatedRadio).performClick()
        }
        
        // 设置语言选项
        val languages = listOf(getString(R.string.all_languages)) + viewModel.availableLanguages
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)
        languageAutoComplete.setAdapter(languageAdapter)
        languageAutoComplete.setText(viewModel.selectedLanguage ?: getString(R.string.all_languages), false)
        
        // 按钮监听
        resetButton.setOnClickListener {
            view.findViewById<View>(R.id.sortByStarsRadio).performClick()
            languageAutoComplete.setText(getString(R.string.all_languages), false)
        }
        
        applyButton.setOnClickListener {
            // 应用过滤条件
            val selectedSortBy = when (sortRadioGroup.checkedRadioButtonId) {
                R.id.sortByUpdatedRadio -> SortBy.UPDATED
                else -> SortBy.STARS
            }
            
            val selectedLanguage = languageAutoComplete.text.toString()
            val language = if (selectedLanguage == getString(R.string.all_languages)) null else selectedLanguage
            
            viewModel.setSortBy(selectedSortBy)
            viewModel.setLanguage(language)
            
            dialog.dismiss()
        }
        
        dialog.setContentView(view)
        dialog.show()
    }
}