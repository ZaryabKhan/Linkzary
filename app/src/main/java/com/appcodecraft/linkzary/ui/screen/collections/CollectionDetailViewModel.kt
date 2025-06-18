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
                // Load collection details
                val collection = collectionRepository.getCollectionById(collectionId.toLong())
                
                // Load links in this collection
                linkRepository.getLinksByCollection(collectionId.toLong()).collect { links ->
                    _uiState.value = _uiState.value.copy(
                        collection = collection,
                        links = links,
                        isLoading = false
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
                val currentLinks = _uiState.value.links
                val linkToUpdate = currentLinks.find { it.id == linkId }
                
                linkToUpdate?.let { link ->
                    linkRepository.updatePinStatus(linkId, !link.isPinned)
                    
                    // Update UI state
                    val updatedLinks = currentLinks.map { 
                        if (it.id == linkId) it.copy(isPinned = !it.isPinned) else it 
                    }
                    _uiState.value = _uiState.value.copy(links = updatedLinks)
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
                    
                    // Update UI state
                    val updatedLinks = _uiState.value.links.filter { it.id != linkId }
                    _uiState.value = _uiState.value.copy(links = updatedLinks)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete link"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}