package com.appcodecraft.linkzary.ui.screen.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
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
    val error: String? = null
)

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val linkRepository: LinkRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState: StateFlow<CollectionDetailUiState> = _uiState.asStateFlow()
    
    fun loadCollectionDetails(collectionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val collectionIdLong = collectionId.toLong()
                
                // Load collection details
                val collection = collectionRepository.getCollectionById(collectionIdLong)
                _uiState.value = _uiState.value.copy(collection = collection)
                
                // Observe links in this collection for real-time updates
                linkRepository.getLinksByCollection(collectionIdLong).collect { links ->
                    _uiState.value = _uiState.value.copy(
                        links = links.sortedWith(
                            compareByDescending<SavedLink> { it.isPinned }
                                .thenByDescending { it.saveDate }
                        ),
                        isLoading = false,
                        error = null
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
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
    
    fun refreshCollection() {
        // Clear error state and let the Flow handle the refresh
        _uiState.value = _uiState.value.copy(error = null)
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
}