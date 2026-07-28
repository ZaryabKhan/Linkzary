package com.appcodecraft.linkzary.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.util.ArticleContentExtractor
import com.appcodecraft.linkzary.util.UrlMetadataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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

enum class ReadStatusFilter {
    ALL,
    UNREAD,
    READ,
    ARCHIVED
}

data class HomeUiState(
    val links: List<SavedLink> = emptyList(),
    val recentCollections: List<Collection> = emptyList(),
    val allCollections: List<Collection> = emptyList(),
    val collectionsWithCounts: Map<Long, Int> = emptyMap(),
    val searchQuery: String = "",
    val sortOrder: LinkSortOrder = LinkSortOrder.PINNED_FIRST,
    val readStatusFilter: ReadStatusFilter = ReadStatusFilter.ALL,
    val unreadCount: Int = 0,
    val readCount: Int = 0,
    val archivedCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isMultiSelectMode: Boolean = false,
    val selectedLinks: Set<Long> = emptySet(),
    val batchOperationMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val collectionRepository: CollectionRepository,
    private val urlMetadataExtractor: UrlMetadataExtractor,
    private val articleContentExtractor: ArticleContentExtractor,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(
        runCatching { LinkSortOrder.valueOf(userPreferencesManager.getHomeSortOrder()) }
            .getOrDefault(LinkSortOrder.PINNED_FIRST)
    )
    private val _readStatusFilter = MutableStateFlow(ReadStatusFilter.ALL)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _isMultiSelectMode = MutableStateFlow(false)
    private val _selectedLinks = MutableStateFlow<Set<Long>>(emptySet())
    private val _batchOperationMessage = MutableStateFlow<String?>(null)

    val isGridView: StateFlow<Boolean> = userPreferencesManager.isHomeGridView
    val searchHistory: StateFlow<List<String>> = userPreferencesManager.searchHistory

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
        _readStatusFilter,
        _isLoading,
        _error,
        _isMultiSelectMode,
        _selectedLinks,
        _batchOperationMessage
    ) { flows ->
        val links = flows[0] as List<SavedLink>
        val recentCollections = flows[1] as List<Collection>
        val allCollections = flows[2] as List<Collection>
        val sortOrder = flows[3] as LinkSortOrder
        val readStatusFilter = flows[4] as ReadStatusFilter
        val isLoading = flows[5] as Boolean
        val error = flows[6] as String?
        val isMultiSelectMode = flows[7] as Boolean
        val selectedLinks = flows[8] as Set<Long>
        val batchOperationMessage = flows[9] as String?

        // Count by read status
        val unreadCount = links.count { it.readStatus == "UNREAD" }
        val readCount = links.count { it.readStatus == "READ" }
        val archivedCount = links.count { it.readStatus == "ARCHIVED" }

        // Apply read status filter (only when not searching)
        val filteredLinks = if (_searchQuery.value.isBlank()) {
            when (readStatusFilter) {
                ReadStatusFilter.ALL -> links
                ReadStatusFilter.UNREAD -> links.filter { it.readStatus == "UNREAD" }
                ReadStatusFilter.READ -> links.filter { it.readStatus == "READ" }
                ReadStatusFilter.ARCHIVED -> links.filter { it.readStatus == "ARCHIVED" }
            }
        } else {
            links
        }

        val sortedLinks = when (sortOrder) {
            LinkSortOrder.PINNED_FIRST -> filteredLinks.sortedWith(
                compareByDescending<SavedLink> { it.isPinned }
                    .thenByDescending { it.saveDate }
            )
            LinkSortOrder.DATE_DESC -> filteredLinks.sortedByDescending { it.saveDate }
            LinkSortOrder.DATE_ASC -> filteredLinks.sortedBy { it.saveDate }
            LinkSortOrder.TITLE_ASC -> filteredLinks.sortedBy { it.title.lowercase() }
            LinkSortOrder.TITLE_DESC -> filteredLinks.sortedByDescending { it.title.lowercase() }
        }

        HomeUiState(
            links = sortedLinks,
            recentCollections = recentCollections,
            allCollections = allCollections,
            collectionsWithCounts = emptyMap(), // Will be populated by separate flow
            searchQuery = _searchQuery.value,
            sortOrder = sortOrder,
            readStatusFilter = readStatusFilter,
            unreadCount = unreadCount,
            readCount = readCount,
            archivedCount = archivedCount,
            isLoading = isLoading,
            error = error,
            isMultiSelectMode = isMultiSelectMode,
            selectedLinks = selectedLinks,
            batchOperationMessage = batchOperationMessage
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

    fun saveSearchQuery(query: String) {
        if (query.isNotBlank()) {
            userPreferencesManager.addSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        userPreferencesManager.clearSearchHistory()
    }

    fun removeSearchQuery(query: String) {
        userPreferencesManager.removeSearchQuery(query)
    }

    fun setSortOrder(sortOrder: LinkSortOrder) {
        _sortOrder.value = sortOrder
        userPreferencesManager.setHomeSortOrder(sortOrder.name)
    }

    fun setReadStatusFilter(filter: ReadStatusFilter) {
        _readStatusFilter.value = filter
    }

    fun toggleGridView() {
        userPreferencesManager.setHomeGridView(!isGridView.value)
    }

    fun clearError() {
        _error.value = null
    }

    fun clearBatchOperationMessage() {
        _batchOperationMessage.value = null
    }

    // Multi-select mode methods
    fun enterMultiSelectMode(initialLinkId: Long? = null) {
        _isMultiSelectMode.value = true
        if (initialLinkId != null) {
            _selectedLinks.value = setOf(initialLinkId)
        } else {
            _selectedLinks.value = emptySet()
        }
    }

    fun exitMultiSelectMode() {
        _isMultiSelectMode.value = false
        _selectedLinks.value = emptySet()
    }

    fun toggleLinkSelection(linkId: Long) {
        val currentSelection = _selectedLinks.value
        _selectedLinks.value = if (currentSelection.contains(linkId)) {
            currentSelection - linkId
        } else {
            currentSelection + linkId
        }

        // Auto-exit multi-select mode if no items are selected
        if (_selectedLinks.value.isEmpty() && _isMultiSelectMode.value) {
            exitMultiSelectMode()
        }
    }

    fun startMultiSelectWithToggle(linkId: Long) {
        if (!_isMultiSelectMode.value) {
            // Enter multi-select mode and select the item
            _isMultiSelectMode.value = true
            _selectedLinks.value = setOf(linkId)
        } else {
            // Already in multi-select mode, just toggle the selection
            toggleLinkSelection(linkId)
        }
    }

    // Batch operations
    fun batchMoveToCollection(collectionId: Long?) {
        viewModelScope.launch {
            try {
                val count = _selectedLinks.value.size
                _selectedLinks.value.forEach { linkId ->
                    linkRepository.moveToCollection(linkId, collectionId)
                }

                val collectionName = if (collectionId == null) {
                    "Uncategorized"
                } else {
                    collectionRepository.getCollectionById(collectionId)?.name ?: "Unknown"
                }

                _batchOperationMessage.value = "Moved $count bookmarks to $collectionName"
                exitMultiSelectMode()
            } catch (e: Exception) {
                _error.value = "Failed to move links: ${e.message}"
            }
        }
    }

    fun batchDeleteLinks() {
        viewModelScope.launch {
            try {
                val count = _selectedLinks.value.size
                linkRepository.deleteLinksByIds(_selectedLinks.value.toList())
                _batchOperationMessage.value = "Deleted $count bookmarks"
                exitMultiSelectMode()
            } catch (e: Exception) {
                _error.value = "Failed to delete links: ${e.message}"
            }
        }
    }

    fun saveLink(url: String, collectionId: Long? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val metadataJob = async(Dispatchers.IO) {
                    urlMetadataExtractor.extractMetadata(url)
                }
                val contentJob = async(Dispatchers.IO) {
                    articleContentExtractor.extractContent(url)
                }

                val metadata = metadataJob.await()
                val content = contentJob.await()

                val link = SavedLink(
                    title = metadata.title,
                    url = url,
                    collectionId = collectionId,
                    favicon = metadata.favicon,
                    previewImageUrl = metadata.previewImageUrl,
                    textContent = content,
                    isOfflineAvailable = content != null
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

                val existingLink = linkRepository.getLinkByUrl(url)
                if (existingLink != null) {
                    _error.value = "Link already saved"
                    return@launch
                }

                val metadataJob = async(Dispatchers.IO) {
                    urlMetadataExtractor.extractMetadata(url)
                }
                val contentJob = async(Dispatchers.IO) {
                    articleContentExtractor.extractContent(url)
                }

                val metadata = metadataJob.await()
                val content = contentJob.await()

                val link = SavedLink(
                    title = metadata.title,
                    url = url,
                    collectionId = collectionId,
                    favicon = metadata.favicon,
                    previewImageUrl = metadata.previewImageUrl,
                    textContent = content,
                    isOfflineAvailable = content != null
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

    fun toggleReadStatus(link: SavedLink) {
        viewModelScope.launch {
            try {
                val newStatus = when (link.readStatus) {
                    "UNREAD" -> "READ"
                    "READ" -> "ARCHIVED"
                    "ARCHIVED" -> "UNREAD"
                    else -> "UNREAD"
                }
                linkRepository.updateReadStatus(link.id, newStatus)
            } catch (e: Exception) {
                _error.value = "Failed to update read status: ${e.message}"
            }
        }
    }

    fun toggleOfflineStatus(link: SavedLink) {
        viewModelScope.launch {
            try {
                linkRepository.updateOfflineStatus(link.id, !link.isOfflineAvailable)
            } catch (e: Exception) {
                _error.value = "Failed to update offline status: ${e.message}"
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

    fun createCollection(name: String, color: Int, icon: String = "Folder", onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val collection = Collection(
                    name = name.trim(),
                    color = String.format("#%06X", color and 0xFFFFFF),
                    icon = icon,
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
