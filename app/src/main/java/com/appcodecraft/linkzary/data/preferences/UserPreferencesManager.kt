package com.appcodecraft.linkzary.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

enum class ThemeMode {
    LIGHT, DARK, SYSTEM;
    
    companion object {
        fun fromString(value: String): ThemeMode {
            return when (value) {
                "LIGHT" -> LIGHT
                "DARK" -> DARK
                else -> SYSTEM
            }
        }
    }
}

class UserPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "linkzary_preferences",
        Context.MODE_PRIVATE
    )

    // Theme preferences
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    // Language preferences
    private val _currentLanguage = MutableStateFlow(getCurrentLanguage())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()
    
    // View state preferences
    private val _isHomeGridView = MutableStateFlow(getHomeGridView())
    val isHomeGridView: StateFlow<Boolean> = _isHomeGridView.asStateFlow()

    private val _isCollectionsGridView = MutableStateFlow(getCollectionsGridView())
    val isCollectionsGridView: StateFlow<Boolean> = _isCollectionsGridView.asStateFlow()

    private val _isCollectionDetailGridView = MutableStateFlow(getCollectionDetailGridView())
    val isCollectionDetailGridView: StateFlow<Boolean> = _isCollectionDetailGridView.asStateFlow()
    
    // Donation preferences
    private val _hasDonated = MutableStateFlow(getHasDonated())
    val hasDonated: StateFlow<Boolean> = _hasDonated.asStateFlow()

    // Search history
    private val _searchHistory = MutableStateFlow(getSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    // Home screen preferences
    fun setHomeGridView(isGridView: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_HOME_GRID_VIEW, isGridView)
            .apply()
        _isHomeGridView.value = isGridView
    }

    private fun getHomeGridView(): Boolean {
        return sharedPreferences.getBoolean(KEY_HOME_GRID_VIEW, true)
    }

    // Collections screen preferences
    fun setCollectionsGridView(isGridView: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_COLLECTIONS_GRID_VIEW, isGridView)
            .apply()
        _isCollectionsGridView.value = isGridView
    }

    private fun getCollectionsGridView(): Boolean {
        return sharedPreferences.getBoolean(KEY_COLLECTIONS_GRID_VIEW, true)
    }

    // Collection detail screen preferences
    fun setCollectionDetailGridView(isGridView: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_COLLECTION_DETAIL_GRID_VIEW, isGridView)
            .apply()
        _isCollectionDetailGridView.value = isGridView
    }

    private fun getCollectionDetailGridView(): Boolean {
        return sharedPreferences.getBoolean(KEY_COLLECTION_DETAIL_GRID_VIEW, false)
    }

    // Sort preferences
    fun setHomeSortOrder(sortOrder: String) {
        sharedPreferences.edit()
            .putString(KEY_HOME_SORT_ORDER, sortOrder)
            .apply()
    }

    fun getHomeSortOrder(): String {
        return sharedPreferences.getString(KEY_HOME_SORT_ORDER, "PINNED_FIRST") ?: "PINNED_FIRST"
    }

    fun setCollectionsSortOrder(sortOrder: String) {
        sharedPreferences.edit()
            .putString(KEY_COLLECTIONS_SORT_ORDER, sortOrder)
            .apply()
    }

    fun getCollectionsSortOrder(): String {
        return sharedPreferences.getString(KEY_COLLECTIONS_SORT_ORDER, "DATE_DESC") ?: "DATE_DESC"
    }

    // Theme preferences
    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
        _themeMode.value = mode
    }
    
    private fun getThemeMode(): ThemeMode {
        val themeName = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return ThemeMode.fromString(themeName)
    }
    
    // Language preferences
    fun setCurrentLanguage(languageCode: String) {
        sharedPreferences.edit()
            .putString(KEY_CURRENT_LANGUAGE, languageCode)
            .apply()
        _currentLanguage.value = languageCode
    }
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(KEY_CURRENT_LANGUAGE, "en") ?: "en"
    }
    
    // Donation methods
    fun setHasDonated(hasDonated: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_HAS_DONATED, hasDonated)
            .apply()
        _hasDonated.value = hasDonated
    }
    
    private fun getHasDonated(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_DONATED, false)
    }

    // Search history methods
    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        val current = getSearchHistory().toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > MAX_SEARCH_HISTORY) {
            current.subList(MAX_SEARCH_HISTORY, current.size).clear()
        }
        val jsonArray = JSONArray(current)
        sharedPreferences.edit()
            .putString(KEY_SEARCH_HISTORY, jsonArray.toString())
            .apply()
        _searchHistory.value = current
    }

    fun removeSearchQuery(query: String) {
        val current = getSearchHistory().toMutableList()
        current.remove(query)
        val jsonArray = JSONArray(current)
        sharedPreferences.edit()
            .putString(KEY_SEARCH_HISTORY, jsonArray.toString())
            .apply()
        _searchHistory.value = current
    }

    fun clearSearchHistory() {
        sharedPreferences.edit()
            .remove(KEY_SEARCH_HISTORY)
            .apply()
        _searchHistory.value = emptyList()
    }

    private fun getSearchHistory(): List<String> {
        val json = sharedPreferences.getString(KEY_SEARCH_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(json)
            (0 until jsonArray.length()).map { jsonArray.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_CURRENT_LANGUAGE = "current_language"
        private const val KEY_HOME_GRID_VIEW = "home_grid_view"
        private const val KEY_COLLECTIONS_GRID_VIEW = "collections_grid_view"
        private const val KEY_COLLECTION_DETAIL_GRID_VIEW = "collection_detail_grid_view"
        private const val KEY_HAS_DONATED = "has_donated"
        private const val KEY_HOME_SORT_ORDER = "home_sort_order"
        private const val KEY_COLLECTIONS_SORT_ORDER = "collections_sort_order"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val MAX_SEARCH_HISTORY = 10
    }
}