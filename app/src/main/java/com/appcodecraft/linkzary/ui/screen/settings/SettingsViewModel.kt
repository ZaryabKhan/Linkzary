package com.appcodecraft.linkzary.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _isClearing = MutableStateFlow(false)
    val isClearing: StateFlow<Boolean> = _isClearing.asStateFlow()

    private val _clearDataResult = MutableStateFlow<ClearDataResult?>(null)
    val clearDataResult: StateFlow<ClearDataResult?> = _clearDataResult.asStateFlow()

    fun clearAllData() {
        viewModelScope.launch {
            try {
                _isClearing.value = true
                _clearDataResult.value = null
                
                // Validate that we have data to clear
                val linkCount = linkRepository.getAllLinks().first().size
                val collectionCount = collectionRepository.getAllCollections().first().size
                
                if (linkCount == 0 && collectionCount == 0) {
                    _clearDataResult.value = ClearDataResult.Error("No data to clear")
                    return@launch
                }
                
                // Clear data in proper order (links first, then collections)
                // This prevents foreign key constraint issues
                linkRepository.deleteAllLinks()
                collectionRepository.deleteAllCollections()
                
                // Verify data was actually cleared
                val remainingLinks = linkRepository.getAllLinks().first().size
                val remainingCollections = collectionRepository.getAllCollections().first().size
                
                if (remainingLinks > 0 || remainingCollections > 0) {
                    _clearDataResult.value = ClearDataResult.Error(
                        "Failed to clear all data. Remaining: $remainingLinks links, $remainingCollections collections"
                    )
                } else {
                    _clearDataResult.value = ClearDataResult.Success
                }
                
            } catch (e: Exception) {
                _clearDataResult.value = ClearDataResult.Error(
                    "Failed to clear data: ${e.message ?: "Unknown error occurred"}"
                )
            } finally {
                _isClearing.value = false
            }
        }
    }

    fun clearDataResultShown() {
        _clearDataResult.value = null
    }

    fun exportDataAsJson(): String {
        // TODO: Implement JSON export functionality
        // This would typically involve collecting all bookmarks and collections
        // and converting them to JSON format
        return ""
    }

    fun exportDataAsCsv(): String {
        // TODO: Implement CSV export functionality
        // This would typically involve collecting all bookmarks
        // and converting them to CSV format
        return ""
    }

    fun importDataFromJson(jsonData: String): Boolean {
        // TODO: Implement JSON import functionality
        // This would typically involve parsing JSON and inserting data
        return false
    }

    fun importDataFromCsv(csvData: String): Boolean {
        // TODO: Implement CSV import functionality
        // This would typically involve parsing CSV and inserting data
        return false
    }
}

sealed class ClearDataResult {
    object Success : ClearDataResult()
    data class Error(val message: String) : ClearDataResult()
}