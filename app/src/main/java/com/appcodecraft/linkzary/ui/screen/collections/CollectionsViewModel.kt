package com.appcodecraft.linkzary.ui.screen.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class CollectionsUiState(
    val collections: List<Collection> = emptyList(),
    val collectionsWithCounts: Map<Long, Int> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    private val allCollections = collectionRepository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val collectionsWithCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    
    init {
        viewModelScope.launch {
            allCollections.collect { collections ->
                val counts = mutableMapOf<Long, Int>()
                collections.forEach { collection ->
                    counts[collection.id] = collectionRepository.getLinksCountInCollection(collection.id)
                }
                collectionsWithCounts.value = counts
            }
        }
    }
    
    private val filteredCollections = combine(
        allCollections,
        _searchQuery
    ) { collections, query ->
        if (query.isBlank()) {
            collections
        } else {
            collections.filter { collection ->
                collection.name.contains(query, ignoreCase = true)
            }
        }
    }
    
    val uiState: StateFlow<CollectionsUiState> = combine(
        filteredCollections,
        collectionsWithCounts,
        _searchQuery,
        _isLoading,
        _error
    ) { collections, counts, searchQuery, isLoading, error ->
        CollectionsUiState(
            collections = collections,
            collectionsWithCounts = counts,
            searchQuery = searchQuery,
            isLoading = isLoading,
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