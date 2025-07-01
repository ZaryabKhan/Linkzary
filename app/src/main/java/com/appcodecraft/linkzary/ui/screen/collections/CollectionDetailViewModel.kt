package com.appcodecraft.linkzary.ui.screen.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.util.UrlMetadataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionDetailUiState(
    val collection: Collection? = null,
    val links: List<SavedLink> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorMessage: String = "",
    val searchQuery: String = "",
    val filteredLinks: List<SavedLink> = emptyList(),
    val isGridView: Boolean = false
)

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val linkRepository: LinkRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val urlMetadataExtractor: UrlMetadataExtractor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()
    
    fun loadCollectionDetails(collectionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, errorMessage = "")
            
            try {
                val collectionIdLong = collectionId.toLong()
                
                // Load collection details
                val collection = collectionRepository.getCollectionById(collectionIdLong)
                _uiState.value = _uiState.value.copy(collection = collection)
                
                // Get grid view preference
                val isGridView = userPreferencesManager.isCollectionDetailGridView.value
                
                // Observe links in this collection for real-time updates
                linkRepository.getLinksByCollection(collectionIdLong).collect { links ->
                    val sortedLinks = links.sortedWith(
                        compareByDescending<SavedLink> { it.isPinned }
                            .thenByDescending { it.saveDate }
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        links = sortedLinks,
                        filteredLinks = sortedLinks,
                        isLoading = false,
                        error = null,
                        errorMessage = "",
                        isGridView = isGridView
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred",
                    errorMessage = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun togglePin(linkId: Long) {
        viewModelScope.launch {
            try {
                val linkToUpdate = _uiState.value.links.find { it.id == linkId }
                linkToUpdate?.let { link ->
                    linkRepository.updatePinStatus(linkId, !link.isPinned)
                    // UI will update automatically via Flow collection
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update link"
                )
            }
        }
    }
    
    fun deleteLink(linkId: Long) {
        viewModelScope.launch {
            try {
                val linkToDelete = _uiState.value.links.find { it.id == linkId }
                linkToDelete?.let { link ->
                    linkRepository.deleteLink(link)
                    // UI will update automatically via Flow collection
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete link"
                )
            }
        }
    }
    
    fun loadCollection() {
        // Clear error state and let the Flow handle the refresh
        _uiState.value = _uiState.value.copy(error = null, errorMessage = "")
        
        // Get the collection ID from the current state
        val collectionId = _uiState.value.collection?.id?.toString() ?: return
        
        // Load collection details
        loadCollectionDetails(collectionId)
    }
    
    fun editLink(updatedLink: SavedLink) {
        viewModelScope.launch {
            try {
                linkRepository.updateLink(updatedLink)
                // UI will update automatically via Flow collection
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update link"
                )
            }
        }
    }
    
    fun moveToCollection(linkId: Long, newCollectionId: Long?) {
        viewModelScope.launch {
            try {
                val linkToMove = _uiState.value.links.find { it.id == linkId }
                linkToMove?.let { link ->
                    val updatedLink = link.copy(collectionId = newCollectionId)
                    linkRepository.updateLink(updatedLink)
                    // UI will update automatically via Flow collection
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to move link"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun toggleGridView() {
        val newGridViewState = !_uiState.value.isGridView
        userPreferencesManager.setCollectionDetailGridView(newGridViewState)
        _uiState.value = _uiState.value.copy(isGridView = newGridViewState)
    }
    
    fun addLinkToCollection(url: String, collectionId: Long? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
                
                val metadata = urlMetadataExtractor.extractMetadata(url)
                
                val link = SavedLink(
                    title = metadata.title,
                    url = url,
                    collectionId = _uiState.value.collection?.id,
                    favicon = metadata.favicon
                )
                
                linkRepository.insertLink(link)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save link: ${e.message}"
                )
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        val filteredLinks = if (query.isEmpty()) {
            _uiState.value.links
        } else {
            _uiState.value.links.filter { link ->
                link.title.contains(query, ignoreCase = true) || 
                link.url.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredLinks = filteredLinks
        )
    }
}