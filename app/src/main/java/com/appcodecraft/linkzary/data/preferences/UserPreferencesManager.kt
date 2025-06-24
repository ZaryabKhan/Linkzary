package com.appcodecraft.linkzary.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "linkzary_preferences",
        Context.MODE_PRIVATE
    )

    // View state preferences
    private val _isHomeGridView = MutableStateFlow(getHomeGridView())
    val isHomeGridView: StateFlow<Boolean> = _isHomeGridView.asStateFlow()

    private val _isCollectionsGridView = MutableStateFlow(getCollectionsGridView())
    val isCollectionsGridView: StateFlow<Boolean> = _isCollectionsGridView.asStateFlow()

    private val _isCollectionDetailGridView = MutableStateFlow(getCollectionDetailGridView())
    val isCollectionDetailGridView: StateFlow<Boolean> = _isCollectionDetailGridView.asStateFlow()

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

    companion object {
        private const val KEY_HOME_GRID_VIEW = "home_grid_view"
        private const val KEY_COLLECTIONS_GRID_VIEW = "collections_grid_view"
        private const val KEY_COLLECTION_DETAIL_GRID_VIEW = "collection_detail_grid_view"
        private const val KEY_HOME_SORT_ORDER = "home_sort_order"
        private const val KEY_COLLECTIONS_SORT_ORDER = "collections_sort_order"
    }
}