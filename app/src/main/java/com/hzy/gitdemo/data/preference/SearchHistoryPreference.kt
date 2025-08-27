package com.hzy.gitdemo.data.preference

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.searchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")

class SearchHistoryPreference(private val context: Context) {
    
    companion object {
        private val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history_list")
        private const val MAX_HISTORY_SIZE = 10 // 最多保存10条历史记录
        private const val SEPARATOR = "||" // 分隔符
    }
    
    /**
     * 获取搜索历史记录
     */
    fun getSearchHistory(): Flow<List<String>> {
        return context.searchHistoryDataStore.data.map { preferences ->
            val historyString = preferences[SEARCH_HISTORY_KEY] ?: ""
            if (historyString.isBlank()) {
                emptyList()
            } else {
                historyString.split(SEPARATOR).filter { it.isNotBlank() }
            }
        }
    }
    
    /**
     * 添加搜索历史记录
     */
    suspend fun addSearchHistory(query: String) {
        if (query.isBlank()) {
            Log.d("SearchHistoryPreference", "Query is blank, not adding to history")
            return
        }
        
        Log.d("SearchHistoryPreference", "Adding search history: $query")
        
        context.searchHistoryDataStore.edit { preferences ->
            // 获取当前历史记录
            val currentHistoryString = preferences[SEARCH_HISTORY_KEY] ?: ""
            Log.d("SearchHistoryPreference", "Current history string: $currentHistoryString")
            
            val currentHistory = if (currentHistoryString.isBlank()) {
                mutableListOf()
            } else {
                currentHistoryString.split(SEPARATOR).filter { it.isNotBlank() }.toMutableList()
            }
            
            Log.d("SearchHistoryPreference", "Current history list: $currentHistory")
            
            // 如果已存在，先移除
            currentHistory.remove(query)
            
            // 添加到最前面
            currentHistory.add(0, query)
            
            // 限制数量
            val limitedHistory = if (currentHistory.size > MAX_HISTORY_SIZE) {
                currentHistory.take(MAX_HISTORY_SIZE)
            } else {
                currentHistory
            }
            
            // 保存为字符串
            val historyString = limitedHistory.joinToString(SEPARATOR)
            Log.d("SearchHistoryPreference", "Saving history string: $historyString")
            preferences[SEARCH_HISTORY_KEY] = historyString
        }
        
        Log.d("SearchHistoryPreference", "Search history saved successfully")
    }
    
    /**
     * 清除所有搜索历史记录
     */
    suspend fun clearSearchHistory() {
        context.searchHistoryDataStore.edit { preferences ->
            preferences.remove(SEARCH_HISTORY_KEY)
        }
    }
    
    /**
     * 删除指定的搜索历史记录
     */
    suspend fun removeSearchHistory(query: String) {
        context.searchHistoryDataStore.edit { preferences ->
            val currentHistoryString = preferences[SEARCH_HISTORY_KEY] ?: ""
            val currentHistory = if (currentHistoryString.isBlank()) {
                mutableListOf()
            } else {
                currentHistoryString.split(SEPARATOR).filter { it.isNotBlank() }.toMutableList()
            }
            
            currentHistory.remove(query)
            
            val historyString = currentHistory.joinToString(SEPARATOR)
            preferences[SEARCH_HISTORY_KEY] = historyString
        }
    }
}