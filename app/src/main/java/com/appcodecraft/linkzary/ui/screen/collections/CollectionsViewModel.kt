package com.appcodecraft.linkzary.ui.screen.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.ui.component.CollectionCover
import com.appcodecraft.linkzary.ui.component.extractDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
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
    // Spec v3 §2.4: up to 3 thumbnail URLs per collection for the card fan.
    val collectionThumbnails: Map<Long, List<String>> = emptyMap(),
    // v6 §3: mosaic covers (seed + image url, newest first) and the last
    // saved time per collection.
    val collectionCovers: Map<Long, List<CollectionCover>> = emptyMap(),
    val collectionLastSaved: Map<Long, Date> = emptyMap(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.NAME_ASC,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val linkRepository: LinkRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(
        runCatching { SortOrder.valueOf(userPreferencesManager.getCollectionsSortOrder()) }
            .getOrDefault(SortOrder.NAME_ASC)
    )
    private val _isLoading = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val isGridView: StateFlow<Boolean> = userPreferencesManager.isCollectionsGridView
    
    private val allCollections = collectionRepository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val collectionsWithCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    private val collectionThumbnails = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    private val collectionCovers = MutableStateFlow<Map<Long, List<CollectionCover>>>(emptyMap())
    private val collectionLastSaved = MutableStateFlow<Map<Long, Date>>(emptyMap())

    init {
        // Observe collections changes
        viewModelScope.launch {
            allCollections.collect { collections ->
                refreshLinkCounts(collections)
            }
        }

        // Observe all links changes to update counts and card covers
        viewModelScope.launch {
            linkRepository.getAllLinks().collect { links ->
                // When any link changes, refresh all collection counts
                refreshLinkCounts(allCollections.value)
                // Spec v3 §2.4: up to 3 thumbnails per collection for the fan
                collectionThumbnails.value = links
                    .asSequence()
                    .filter { it.collectionId != null }
                    .mapNotNull { link ->
                        val url = link.customImageUrl?.takeIf { it.isNotBlank() }
                            ?: link.previewImageUrl?.takeIf { it.isNotBlank() }
                        url?.let { link.collectionId!! to it }
                    }
                    .groupBy({ it.first }, { it.second })
                    .mapValues { (_, urls) -> urls.take(3) }
                // Spec v6 §3: up to 4 mosaic covers (newest first) + last saved
                val byCollection = links
                    .asSequence()
                    .filter { it.collectionId != null }
                    .groupBy { it.collectionId!! }
                collectionCovers.value = byCollection.mapValues { (_, collectionLinks) ->
                    collectionLinks
                        .sortedByDescending { it.saveDate }
                        .take(4)
                        .map { link ->
                            CollectionCover(
                                seed = extractDomain(link.url),
                                imageUrl = link.customImageUrl?.takeIf { it.isNotBlank() }
                                    ?: link.previewImageUrl?.takeIf { it.isNotBlank() }
                            )
                        }
                }
                collectionLastSaved.value = byCollection.mapValues { (_, collectionLinks) ->
                    collectionLinks.maxOf { it.saveDate }
                }
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
        collectionThumbnails,
        collectionCovers,
        collectionLastSaved,
        _searchQuery,
        _sortOrder,
        _isLoading,
        _isRefreshing,
        _error
    ) { flows ->
        val collections = flows[0] as List<Collection>
        val counts = flows[1] as Map<Long, Int>
        val thumbnails = flows[2] as Map<Long, List<String>>
        val covers = flows[3] as Map<Long, List<CollectionCover>>
        val lastSaved = flows[4] as Map<Long, Date>
        val searchQuery = flows[5] as String
        val sortOrder = flows[6] as SortOrder
        val isLoading = flows[7] as Boolean
        val isRefreshing = flows[8] as Boolean
        val error = flows[9] as String?

        CollectionsUiState(
            collections = collections,
            collectionsWithCounts = counts,
            collectionThumbnails = thumbnails,
            collectionCovers = covers,
            collectionLastSaved = lastSaved,
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
        userPreferencesManager.setCollectionsSortOrder(sortOrder.name)
    }

    fun toggleGridView() {
        userPreferencesManager.setCollectionsGridView(!isGridView.value)
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
    
    fun createCollection(name: String, color: Int, icon: String = "Folder") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val collection = Collection(
                    name = name.trim(),
                    color = String.format("#%06X", color and 0xFFFFFF),
                    icon = icon,
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

                // Unlink all links from this collection before deleting it
                linkRepository.unlinkLinksFromCollection(collection.id)
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