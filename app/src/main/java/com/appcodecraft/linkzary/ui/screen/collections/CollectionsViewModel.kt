package com.appcodecraft.linkzary.ui.screen.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    LINK_COUNT_DESC,
    DATE_DESC
}

data class CollectionsUiState(
    val collections: List<Collection> = emptyList(),
    val collectionsWithCounts: Map<Long, Int> = emptyMap(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val linkRepository: LinkRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.NAME_ASC)
    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    private val allCollections = collectionRepository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val collectionsWithCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    
    init {
        // Observe collections changes
        viewModelScope.launch {
            allCollections.collect { collections ->
                refreshLinkCounts(collections)
            }
        }
        
        // Observe all links changes to update counts in real-time
        viewModelScope.launch {
            linkRepository.getAllLinks().collect { _ ->
                // When any link changes, refresh all collection counts
                refreshLinkCounts(allCollections.value)
            }
        }
    }
    
    private suspend fun refreshLinkCounts(collections: List<Collection>) {
        val counts = mutableMapOf<Long, Int>()
        collections.forEach { collection ->
            counts[collection.id] = collectionRepository.getLinksCountInCollection(collection.id)
        }
        collectionsWithCounts.value = counts
    }
    
    private val filteredAndSortedCollections = combine(
        allCollections,
        collectionsWithCounts,
        _searchQuery,
        _sortOrder
    ) { collections, counts, query, sortOrder ->
        val filtered = if (query.isBlank()) {
            collections
        } else {
            collections.filter { collection ->
                collection.name.contains(query, ignoreCase = true)
            }
        }
        
        when (sortOrder) {
            SortOrder.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            SortOrder.LINK_COUNT_DESC -> filtered.sortedByDescending { counts[it.id] ?: 0 }
            SortOrder.DATE_DESC -> filtered.sortedByDescending { it.createdDate }
        }
    }
    
    val uiState: StateFlow<CollectionsUiState> = combine(
        filteredAndSortedCollections,
        collectionsWithCounts,
        _searchQuery,
        _sortOrder,
        _isLoading,
        _isRefreshing,
        _error
    ) { flows ->
        val collections = flows[0] as List<Collection>
        val counts = flows[1] as Map<Long, Int>
        val searchQuery = flows[2] as String
        val sortOrder = flows[3] as SortOrder
        val isLoading = flows[4] as Boolean
        val isRefreshing = flows[5] as Boolean
        val error = flows[6] as String?
        
        CollectionsUiState(
            collections = collections,
            collectionsWithCounts = counts,
            searchQuery = searchQuery,
            sortOrder = sortOrder,
            isLoading = isLoading,
            isRefreshing = isRefreshing,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CollectionsUiState()
    )
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }
    
    fun refreshCollections() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                _error.value = null
                
                // Refresh link counts for all collections
                refreshLinkCounts(allCollections.value)
                
            } catch (e: Exception) {
                _error.value = "Failed to refresh collections: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun refreshLinkCountsForCollection(collectionId: Long) {
        viewModelScope.launch {
            try {
                val currentCounts = collectionsWithCounts.value.toMutableMap()
                currentCounts[collectionId] = collectionRepository.getLinksCountInCollection(collectionId)
                collectionsWithCounts.value = currentCounts
            } catch (e: Exception) {
                _error.value = "Failed to refresh link count: ${e.message}"
            }
        }
    }
    
    fun createCollection(name: String, color: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val collection = Collection(
                    name = name.trim(),
                    color = String.format("#%06X", color and 0xFFFFFF),
                    createdDate = Date()
                )
                
                collectionRepository.insertCollection(collection)
            } catch (e: Exception) {
                _error.value = "Failed to create collection: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateCollection(collection: Collection) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                collectionRepository.updateCollection(collection)
            } catch (e: Exception) {
                _error.value = "Failed to update collection: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteCollection(collection: Collection) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                collectionRepository.deleteCollection(collection)
            } catch (e: Exception) {
                _error.value = "Failed to delete collection: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}