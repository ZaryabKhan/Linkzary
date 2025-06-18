package com.appcodecraft.linkzary.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.util.UrlMetadataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class HomeUiState(
    val links: List<SavedLink> = emptyList(),
    val recentCollections: List<Collection> = emptyList(),
    val allCollections: List<Collection> = emptyList(),
    val collectionsWithCounts: Map<Long, Int> = emptyMap(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val collectionRepository: CollectionRepository,
    private val urlMetadataExtractor: UrlMetadataExtractor
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

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
        _isLoading,
        _error
    ) { links, recentCollections, allCollections, isLoading, error ->
        
        // Get collection counts for recent collections
        val collectionsWithCounts = mutableMapOf<Long, Int>()
        recentCollections.forEach { collection ->
            viewModelScope.launch {
                val count = collectionRepository.getLinksCountInCollection(collection.id)
                collectionsWithCounts[collection.id] = count
            }
        }
        
        HomeUiState(
            links = links, // Keep DAO sorting (pinned first, then by date)
            recentCollections = recentCollections,
            allCollections = allCollections,
            collectionsWithCounts = collectionsWithCounts,
            searchQuery = _searchQuery.value,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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