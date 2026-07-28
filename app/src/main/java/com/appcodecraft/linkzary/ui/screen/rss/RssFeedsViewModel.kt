package com.appcodecraft.linkzary.ui.screen.rss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.RssFeed
import com.appcodecraft.linkzary.data.entity.RssFeedItem
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.data.repository.RssRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RssFeedsUiState(
    val feeds: List<RssFeed> = emptyList(),
    val feedItems: List<RssFeedItem> = emptyList(),
    val selectedFeed: RssFeed? = null,
    val isLoading: Boolean = true,
    val isAddingFeed: Boolean = false,
    val feedUrl: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class RssFeedsViewModel @Inject constructor(
    private val rssRepository: RssRepository,
    private val linkRepository: LinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RssFeedsUiState())
    val uiState: StateFlow<RssFeedsUiState> = _uiState.asStateFlow()

    init {
        loadFeeds()
    }

    private fun loadFeeds() {
        viewModelScope.launch {
            rssRepository.getAllFeeds().collect { feeds ->
                _uiState.value = _uiState.value.copy(
                    feeds = feeds,
                    isLoading = false
                )
            }
        }
    }

    fun selectFeed(feed: RssFeed?) {
        _uiState.value = _uiState.value.copy(selectedFeed = feed)
        if (feed != null) {
            loadFeedItems(feed.id)
            // Refresh from network in background
            viewModelScope.launch {
                try {
                    rssRepository.refreshFeed(feed.id)
                } catch (e: Exception) {
                    // Non-critical: items already loaded from DB
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(feedItems = emptyList())
        }
    }

    private fun loadFeedItems(feedId: Long) {
        viewModelScope.launch {
            rssRepository.getFeedItems(feedId).collect { items ->
                _uiState.value = _uiState.value.copy(feedItems = items)
            }
        }
    }

    fun updateFeedUrl(url: String) {
        _uiState.value = _uiState.value.copy(feedUrl = url)
    }

    fun startAddingFeed() {
        _uiState.value = _uiState.value.copy(
            isAddingFeed = true,
            feedUrl = "",
            errorMessage = null
        )
    }

    fun cancelAddingFeed() {
        _uiState.value = _uiState.value.copy(
            isAddingFeed = false,
            feedUrl = "",
            errorMessage = null
        )
    }

    fun confirmAddingFeed() {
        val url = _uiState.value.feedUrl.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter a valid URL")
            return
        }

        viewModelScope.launch {
            try {
                val existingFeed = rssRepository.getFeedByUrl(url)
                if (existingFeed != null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Feed already exists")
                    return@launch
                }

                rssRepository.addFeed(url)
                _uiState.value = _uiState.value.copy(
                    isAddingFeed = false,
                    feedUrl = "",
                    successMessage = "Feed added successfully",
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add feed: ${e.message}"
                )
            }
        }
    }

    fun deleteFeed(feed: RssFeed) {
        viewModelScope.launch {
            try {
                rssRepository.deleteFeed(feed)
                if (_uiState.value.selectedFeed?.id == feed.id) {
                    _uiState.value = _uiState.value.copy(
                        selectedFeed = null,
                        feedItems = emptyList()
                    )
                }
                _uiState.value = _uiState.value.copy(successMessage = "Feed deleted")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete feed: ${e.message}")
            }
        }
    }

    fun saveLinkFromItem(item: RssFeedItem) {
        viewModelScope.launch {
            try {
                val link = SavedLink(
                    title = item.title,
                    url = item.url,
                    note = item.description ?: ""
                )
                linkRepository.insertLink(link)
                rssRepository.markItemAsSaved(item.id)
                _uiState.value = _uiState.value.copy(successMessage = "Link saved to bookmarks")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save link: ${e.message}")
            }
        }
    }

    fun markItemAsRead(item: RssFeedItem) {
        viewModelScope.launch {
            rssRepository.markItemAsRead(item.id)
        }
    }

    fun refreshFeed(feed: RssFeed) {
        viewModelScope.launch {
            try {
                rssRepository.refreshFeed(feed.id)
                _uiState.value = _uiState.value.copy(successMessage = "Feed refreshed")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to refresh feed: ${e.message}")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
