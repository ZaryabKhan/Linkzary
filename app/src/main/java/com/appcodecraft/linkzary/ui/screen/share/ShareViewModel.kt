package com.appcodecraft.linkzary.ui.screen.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.util.ArticleContentExtractor
import com.appcodecraft.linkzary.util.UrlMetadataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class ShareUiState(
    val title: String = "",
    val url: String = "",
    val note: String = "",
    val tags: String = "",
    val availableCollections: List<Collection> = emptyList(),
    val selectedCollectionId: Long? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val collectionRepository: CollectionRepository,
    private val urlMetadataExtractor: UrlMetadataExtractor,
    private val articleContentExtractor: ArticleContentExtractor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    val allCollections: StateFlow<List<Collection>> = collectionRepository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onUrlShared(sharedUrl: String) {
        if (_uiState.value.url.isNotEmpty()) return // Already processed or processing

        _uiState.value = _uiState.value.copy(url = sharedUrl, isLoading = true)
        
        viewModelScope.launch {
            try {
                // Check if already saved
                val existingLink = linkRepository.getLinkByUrl(sharedUrl)
                if (existingLink != null) {
                    _uiState.value = _uiState.value.copy(
                        title = existingLink.title,
                        note = existingLink.note,
                        tags = existingLink.tags,
                        selectedCollectionId = existingLink.collectionId,
                        isLoading = false,
                        error = "Link already saved"
                    )
                    return@launch
                }

                // Extract metadata
                val metadata = urlMetadataExtractor.extractMetadata(sharedUrl)
                _uiState.value = _uiState.value.copy(
                    title = metadata.title,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load metadata: ${e.message}"
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateUrl(url: String) {
        _uiState.value = _uiState.value.copy(url = url)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun updateTags(tags: String) {
        _uiState.value = _uiState.value.copy(tags = tags)
    }

    fun selectCollection(collectionId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCollectionId = collectionId)
    }

    fun saveLink() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val state = _uiState.value

                // Re-fetch metadata for favicon/image if needed, or just use what we have
                val metadata = urlMetadataExtractor.extractMetadata(state.url)
                
                // Extract article content
                val content = articleContentExtractor.extractContent(state.url)

                val link = SavedLink(
                    title = state.title,
                    url = state.url,
                    note = state.note,
                    tags = state.tags,
                    collectionId = state.selectedCollectionId,
                    favicon = metadata.favicon,
                    previewImageUrl = metadata.previewImageUrl,
                    textContent = content,
                    isOfflineAvailable = content != null
                )

                linkRepository.insertLink(link)
                _uiState.value = _uiState.value.copy(isLoading = false, isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to save: ${e.message}"
                )
            }
        }
    }

    fun createCollection(name: String, color: Int, icon: String = "Folder") {
        viewModelScope.launch {
            try {
                val collection = Collection(
                    name = name.trim(),
                    color = String.format("#%06X", color and 0xFFFFFF),
                    icon = icon,
                    createdDate = Date()
                )

                val collectionId = collectionRepository.insertCollection(collection)
                selectCollection(collectionId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to create collection: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
