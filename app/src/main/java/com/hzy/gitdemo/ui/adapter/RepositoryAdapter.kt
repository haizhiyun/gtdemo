package com.hzy.gitdemo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.hzy.gitdemo.R
import com.hzy.gitdemo.data.model.Repository

class RepositoryAdapter(
    private val onItemClick: (Repository) -> Unit,
    private val onRetryClick: () -> Unit = {}
) : ListAdapter<RepositoryAdapter.ListItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_REPOSITORY = 0
        private const val VIEW_TYPE_LOADING = 1
        private const val VIEW_TYPE_ERROR = 2
        
        private val DiffCallback = object : DiffUtil.ItemCallback<ListItem>() {
            override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
                return when {
                    oldItem is ListItem.RepositoryItem && newItem is ListItem.RepositoryItem -> {
                        oldItem.repository.id == newItem.repository.id
                    }
                    oldItem is ListItem.LoadingItem && newItem is ListItem.LoadingItem -> true
                    oldItem is ListItem.ErrorItem && newItem is ListItem.ErrorItem -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
                return oldItem == newItem
            }
        }
    }
    
    sealed class ListItem {
        data class RepositoryItem(val repository: Repository) : ListItem()
        object LoadingItem : ListItem()
        data class ErrorItem(val message: String) : ListItem()
    }
    
    fun submitRepositories(repositories: List<Repository>, isLoadingMore: Boolean = false, loadMoreError: String? = null) {
        val items = mutableListOf<ListItem>()
        
        // 添加仓库数据
        items.addAll(repositories.map { ListItem.RepositoryItem(it) })
        
        // 添加加载指示器或错误状态
        when {
            loadMoreError != null -> items.add(ListItem.ErrorItem(loadMoreError))
            isLoadingMore -> items.add(ListItem.LoadingItem)
        }
        
        submitList(items)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.RepositoryItem -> VIEW_TYPE_REPOSITORY
            is ListItem.LoadingItem -> VIEW_TYPE_LOADING
            is ListItem.ErrorItem -> VIEW_TYPE_ERROR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_REPOSITORY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_repository, parent, false)
                RepositoryViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            VIEW_TYPE_ERROR -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_load_error, parent, false)
                ErrorViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.RepositoryItem -> (holder as RepositoryViewHolder).bind(item.repository)
            is ListItem.LoadingItem -> (holder as LoadingViewHolder).bind()
            is ListItem.ErrorItem -> (holder as ErrorViewHolder).bind(item.message)
        }
    }

    inner class RepositoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val languageTextView: TextView = itemView.findViewById(R.id.languageTextView)
        private val starsTextView: TextView = itemView.findViewById(R.id.starsTextView)
        private val forksTextView: TextView = itemView.findViewById(R.id.forksTextView)

        fun bind(repository: Repository) {
            nameTextView.text = repository.fullName
            descriptionTextView.text = repository.description ?: "暂无描述"
            languageTextView.text = repository.language ?: ""
            starsTextView.text = formatCount(repository.stargazersCount)
            forksTextView.text = formatCount(repository.forksCount)

            // 设置语言显示
            if (repository.language.isNullOrEmpty()) {
                languageTextView.visibility = View.GONE
            } else {
                languageTextView.visibility = View.VISIBLE
                languageTextView.text = repository.language
            }

            cardView.setOnClickListener {
                onItemClick(repository)
            }
        }

        private fun formatCount(count: Int): String {
            return when {
                count >= 1000000 -> "${count / 1000000}M"
                count >= 1000 -> "${count / 1000}K"
                else -> count.toString()
            }
        }
    }
    
    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            // 加载指示器不需要特殊处理
        }
    }
    
    inner class ErrorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val errorTextView: TextView = itemView.findViewById(R.id.errorTextView)
        private val retryButton: Button = itemView.findViewById(R.id.retryButton)
        
        fun bind(errorMessage: String) {
            errorTextView.text = errorMessage
            retryButton.setOnClickListener {
                onRetryClick()
            }
        }
    }
}