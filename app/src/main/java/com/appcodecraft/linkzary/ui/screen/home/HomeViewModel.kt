package com.appcodecraft.linkzary.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.util.UrlMetadataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

enum class LinkSortOrder {
    DATE_DESC,
    DATE_ASC,
    TITLE_ASC,
    TITLE_DESC,
    PINNED_FIRST
}

data class HomeUiState(
    val links: List<SavedLink> = emptyList(),
    val recentCollections: List<Collection> = emptyList(),
    val allCollections: List<Collection> = emptyList(),
    val collectionsWithCounts: Map<Long, Int> = emptyMap(),
    val searchQuery: String = "",
    val sortOrder: LinkSortOrder = LinkSortOrder.PINNED_FIRST,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val collectionRepository: CollectionRepository,
    private val urlMetadataExtractor: UrlMetadataExtractor,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(
        LinkSortOrder.valueOf(userPreferencesManager.getHomeSortOrder())
    )
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val isGridView: StateFlow<Boolean> = userPreferencesManager.isHomeGridView

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = combine(
        _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {
                linkRepository.getAllLinks()
            } else {
                linkRepository.searchLinks(query)
            }
        },
        collectionRepository.getRecentCollections(),
        collectionRepository.getAllCollections(),
        _sortOrder,
        _isLoading,
        _error
    ) { flows ->
        val links = flows[0] as List<SavedLink>
        val recentCollections = flows[1] as List<Collection>
        val allCollections = flows[2] as List<Collection>
        val sortOrder = flows[3] as LinkSortOrder
        val isLoading = flows[4] as Boolean
        val error = flows[5] as String?
        
        val sortedLinks = when (sortOrder) {
            LinkSortOrder.PINNED_FIRST -> links.sortedWith(
                compareByDescending<SavedLink> { it.isPinned }
                    .thenByDescending { it.saveDate }
            )
            LinkSortOrder.DATE_DESC -> links.sortedByDescending { it.saveDate }
            LinkSortOrder.DATE_ASC -> links.sortedBy { it.saveDate }
            LinkSortOrder.TITLE_ASC -> links.sortedBy { it.title.lowercase() }
            LinkSortOrder.TITLE_DESC -> links.sortedByDescending { it.title.lowercase() }
        }
        
        HomeUiState(
            links = sortedLinks,
            recentCollections = recentCollections,
            allCollections = allCollections,
            collectionsWithCounts = emptyMap(), // Will be populated by separate flow
            searchQuery = _searchQuery.value,
            sortOrder = sortOrder,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    // Real-time collection counts that update when links are added/removed
    @OptIn(ExperimentalCoroutinesApi::class)
    val collectionsWithCounts: StateFlow<Map<Long, Int>> = uiState
        .map { it.recentCollections }
        .distinctUntilChanged()
        .flatMapLatest { collections ->
            if (collections.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(
                    collections.map { collection ->
                        collectionRepository.getLinksCountInCollectionFlow(collection.id)
                            .map { count -> collection.id to count }
                    }
                ) { counts ->
                    counts.toMap()
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Combined UI state with real-time collection counts
    val combinedUiState: StateFlow<HomeUiState> = combine(
        uiState,
        collectionsWithCounts
    ) { state, counts ->
        state.copy(collectionsWithCounts = counts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSortOrder(sortOrder: LinkSortOrder) {
        _sortOrder.value = sortOrder
        userPreferencesManager.setHomeSortOrder(sortOrder.name)
    }

    fun toggleGridView() {
        userPreferencesManager.setHomeGridView(!isGridView.value)
    }

    fun clearError() {
        _error.value = null
    }

    fun saveLink(url: String, collectionId: Long? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val metadata = urlMetadataExtractor.extractMetadata(url)
                
                val link = SavedLink(
                    title = metadata.title,
                    url = url,
                    collectionId = collectionId,
                    favicon = metadata.favicon
                )
                
                linkRepository.insertLink(link)
            } catch (e: Exception) {
                _error.value = "Failed to save link: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun saveSharedLink(url: String, collectionId: Long? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Check if link already exists
                val existingLink = linkRepository.getLinkByUrl(url)
                if (existingLink != null) {
                    _error.value = "Link already saved"
                    return@launch
                }
                
                val metadata = urlMetadataExtractor.extractMetadata(url)
                
                val link = SavedLink(
                    title = metadata.title,
                    url = url,
                    collectionId = collectionId,
                    favicon = metadata.favicon
                )
                
                linkRepository.insertLink(link)
            } catch (e: Exception) {
                _error.value = "Failed to save link: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLink(link: SavedLink) {
        viewModelScope.launch {
            try {
                linkRepository.deleteLink(link)
            } catch (e: Exception) {
                _error.value = "Failed to delete link: ${e.message}"
            }
        }
    }

    fun togglePinStatus(link: SavedLink) {
        viewModelScope.launch {
            try {
                linkRepository.updatePinStatus(link.id, !link.isPinned)
            } catch (e: Exception) {
                _error.value = "Failed to update pin status: ${e.message}"
            }
        }
    }

    fun moveToCollection(linkId: Long, collectionId: Long?) {
        viewModelScope.launch {
            try {
                linkRepository.moveToCollection(linkId, collectionId)
            } catch (e: Exception) {
                _error.value = "Failed to move link: ${e.message}"
            }
        }
    }

    fun updateLink(link: SavedLink) {
        viewModelScope.launch {
            try {
                linkRepository.updateLink(link)
            } catch (e: Exception) {
                _error.value = "Failed to update link: ${e.message}"
            }
        }
    }

    fun createCollection(name: String, color: Int, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val collection = Collection(
                    name = name.trim(),
                    color = String.format("#%06X", color and 0xFFFFFF),
                    createdDate = Date()
                )
                
                val collectionId = collectionRepository.insertCollection(collection)
                onCreated(collectionId)
            } catch (e: Exception) {
                _error.value = "Failed to create collection: ${e.message}"
            }
        }
    }
}