package com.appcodecraft.linkzary.ui.screen.tags

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagInfo(
    val name: String,
    val count: Int,
    val links: List<SavedLink> = emptyList()
)

data class TagsUiState(
    val tags: List<TagInfo> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTag: TagInfo? = null,
    val isRenaming: Boolean = false,
    val renamingFrom: String = "",
    val renamingTo: String = "",
    val isMultiSelectMode: Boolean = false,
    val selectedTags: Set<String> = emptySet(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val linkRepository: LinkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagsUiState())
    val uiState: StateFlow<TagsUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    private fun loadTags() {
        viewModelScope.launch {
            linkRepository.getAllLinks().combine(linkRepository.getAllTags()) { links, tagNames ->
                val tagInfoList = tagNames.map { tag ->
                    val matchingLinks = links.filter { link ->
                        link.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.contains(tag)
                    }
                    TagInfo(
                        name = tag,
                        count = matchingLinks.size,
                        links = matchingLinks
                    )
                }
                tagInfoList
            }.collect { tagInfoList ->
                _uiState.value = _uiState.value.copy(
                    tags = tagInfoList,
                    isLoading = false
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    val filteredTags: List<TagInfo>
        get() {
            val query = _uiState.value.searchQuery.trim()
            return if (query.isBlank()) {
                _uiState.value.tags
            } else {
                _uiState.value.tags.filter { it.name.contains(query, ignoreCase = true) }
            }
        }

    fun selectTag(tag: TagInfo?) {
        _uiState.value = _uiState.value.copy(selectedTag = tag)
    }

    fun startRenaming(from: String) {
        _uiState.value = _uiState.value.copy(
            isRenaming = true,
            renamingFrom = from,
            renamingTo = from,
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateRenamingTo(to: String) {
        _uiState.value = _uiState.value.copy(renamingTo = to)
    }

    fun cancelRenaming() {
        _uiState.value = _uiState.value.copy(
            isRenaming = false,
            renamingFrom = "",
            renamingTo = "",
            errorMessage = null
        )
    }

    fun confirmRenaming() {
        val state = _uiState.value
        val oldTag = state.renamingFrom.trim()
        val newTag = state.renamingTo.trim()

        if (newTag.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Tag name cannot be empty")
            return
        }
        if (oldTag == newTag) {
            cancelRenaming()
            return
        }

        viewModelScope.launch {
            try {
                linkRepository.renameTag(oldTag, newTag)
                _uiState.value = _uiState.value.copy(
                    isRenaming = false,
                    renamingFrom = "",
                    renamingTo = "",
                    successMessage = "Tag renamed to '$newTag'",
                    errorMessage = null
                )
                loadTags()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to rename tag: ${e.message}"
                )
            }
        }
    }

    fun enterMultiSelectMode(initialTag: String? = null) {
        val selected = if (initialTag != null) setOf(initialTag) else emptySet()
        _uiState.value = _uiState.value.copy(
            isMultiSelectMode = true,
            selectedTags = selected,
            errorMessage = null,
            successMessage = null
        )
    }

    fun toggleTagSelection(tag: String) {
        val current = _uiState.value.selectedTags
        _uiState.value = _uiState.value.copy(
            selectedTags = if (current.contains(tag)) current - tag else current + tag
        )
    }

    fun selectAllTags() {
        _uiState.value = _uiState.value.copy(
            selectedTags = filteredTags.map { it.name }.toSet()
        )
    }

    fun exitMultiSelectMode() {
        _uiState.value = _uiState.value.copy(
            isMultiSelectMode = false,
            selectedTags = emptySet()
        )
    }

    fun deleteSelectedTags() {
        val tags = _uiState.value.selectedTags.toList()
        if (tags.isEmpty()) return

        viewModelScope.launch {
            try {
                linkRepository.deleteTags(tags)
                _uiState.value = _uiState.value.copy(
                    isMultiSelectMode = false,
                    selectedTags = emptySet(),
                    successMessage = "Deleted ${tags.size} tag(s) from ${getAffectedLinksCount(tags)} link(s)",
                    errorMessage = null
                )
                loadTags()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete tags: ${e.message}"
                )
            }
        }
    }

    private fun getAffectedLinksCount(tags: List<String>): Int {
        val tagSet = tags.toSet()
        return _uiState.value.tags
            .filter { it.name in tagSet }
            .sumOf { it.count }
    }

    fun deleteSingleTag(tag: String) {
        viewModelScope.launch {
            try {
                linkRepository.deleteTag(tag)
                _uiState.value = _uiState.value.copy(
                    successMessage = "Deleted tag '$tag'",
                    errorMessage = null
                )
                loadTags()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete tag: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}