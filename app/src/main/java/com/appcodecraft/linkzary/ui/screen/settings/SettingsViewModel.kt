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
    private val collectionRepository: CollectionRepository,
    private val importExportService: com.appcodecraft.linkzary.data.service.ImportExportService,
    private val driveBackupService: com.appcodecraft.linkzary.data.service.DriveBackupService
) : ViewModel() {

    private val _isClearing = MutableStateFlow(false)

    private val _clearDataResult = MutableStateFlow<ClearDataResult?>(null)
    val clearDataResult: StateFlow<ClearDataResult?> = _clearDataResult.asStateFlow()

    private val _metadataRefreshProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val metadataRefreshProgress: StateFlow<Pair<Int, Int>?> = _metadataRefreshProgress.asStateFlow()

    private val _isMetadataDialogHidden = MutableStateFlow(false)
    val isMetadataDialogHidden: StateFlow<Boolean> = _isMetadataDialogHidden.asStateFlow()

    // Duplicate merging
    private val _duplicateCount = MutableStateFlow<Int?>(null)
    val duplicateCount: StateFlow<Int?> = _duplicateCount.asStateFlow()

    private val _duplicateLinks = MutableStateFlow<List<List<com.appcodecraft.linkzary.data.entity.SavedLink>>>(emptyList())
    val duplicateLinks: StateFlow<List<List<com.appcodecraft.linkzary.data.entity.SavedLink>>> = _duplicateLinks.asStateFlow()

    private val _mergeResult = MutableStateFlow<Int?>(null)
    val mergeResult: StateFlow<Int?> = _mergeResult.asStateFlow()

    init {
        refreshDuplicateCount()
    }

    fun refreshDuplicateCount() {
        viewModelScope.launch {
            try {
                _duplicateLinks.value = linkRepository.findDuplicateGroups()
                _duplicateCount.value = _duplicateLinks.value.sumOf { it.size - 1 }
            } catch (_: Exception) {
            }
        }
    }

    fun mergeDuplicates() {
        viewModelScope.launch {
            try {
                val removed = linkRepository.mergeDuplicates()
                _mergeResult.value = removed
                refreshDuplicateCount()
            } catch (e: Exception) {
                _mergeResult.value = -1
            }
        }
    }

    fun clearMergeResult() {
        _mergeResult.value = null
    }

    // Drive backup / restore
    private val _driveStatus = MutableStateFlow<DriveBackupStatus?>(null)
    val driveStatus: StateFlow<DriveBackupStatus?> = _driveStatus.asStateFlow()

    fun isDriveConnected(context: android.content.Context): Boolean =
        driveBackupService.getSignedInAccount(context) != null

    fun backupToDrive(
        context: android.content.Context,
        onAuthIntent: (android.content.Intent) -> Unit = {}
    ) {
        viewModelScope.launch {
            _driveStatus.value = DriveBackupStatus.InProgress
            try {
                val links = linkRepository.getAllLinks().first()
                val collections = collectionRepository.getAllCollections().first()
                val json = importExportService.buildBackupJson(links, collections)
                val result = driveBackupService.backup(context, json, onAuthIntent)
                _driveStatus.value = when {
                    result == null -> DriveBackupStatus.Success
                    result == "signin" -> DriveBackupStatus.NeedsSignIn
                    result == "permission" -> DriveBackupStatus.NeedsPermission
                    else -> DriveBackupStatus.Error(result.removePrefix("error: "))
                }
            } catch (e: Exception) {
                _driveStatus.value = DriveBackupStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun restoreFromDrive(
        context: android.content.Context,
        onAuthIntent: (android.content.Intent) -> Unit = {}
    ) {
        viewModelScope.launch {
            _driveStatus.value = DriveBackupStatus.InProgress
            try {
                val json = driveBackupService.restore(context, onAuthIntent)
                if (json == null) {
                    _driveStatus.value = if (driveBackupService.getSignedInAccount(context) == null) {
                        DriveBackupStatus.NeedsSignIn
                    } else {
                        DriveBackupStatus.Error("No backup found")
                    }
                    return@launch
                }
                val data = importExportService.parseBackupJson(json)
                val existingLinks = linkRepository.getAllLinks().first()
                val existingCollections = collectionRepository.getAllCollections().first()

                // Recreate collections, then links, skipping what already exists
                val idMap = mutableMapOf<Long, Long>()
                data.collections.forEach { export ->
                    val existing = existingCollections.find { it.name.equals(export.name, ignoreCase = true) }
                    if (existing != null) {
                        idMap[export.id] = existing.id
                    } else {
                        val newId = collectionRepository.insertCollection(
                            com.appcodecraft.linkzary.data.entity.Collection(
                                name = export.name,
                                color = "#2196F3"
                            )
                        )
                        idMap[export.id] = newId
                    }
                }
                var imported = 0
                val normalized = existingLinks.mapNotNull { com.appcodecraft.linkzary.util.UrlNormalizer.normalize(it.url) }.toSet()
                data.links.forEach { export ->
                    if (com.appcodecraft.linkzary.util.UrlNormalizer.normalize(export.url) !in normalized) {
                        linkRepository.insertLink(
                            com.appcodecraft.linkzary.data.entity.SavedLink(
                                title = export.title,
                                url = export.url,
                                note = export.description ?: "",
                                tags = "",
                                collectionId = export.collectionId?.let { idMap[it] },
                                isPinned = export.isPinned,
                                favicon = export.faviconUrl,
                                previewImageUrl = export.previewImageUrl,
                                saveDate = importExportService.parseExportDate(export.createdAt)
                            )
                        )
                        imported++
                    }
                }
                _driveStatus.value = DriveBackupStatus.Restored(imported)
            } catch (e: Exception) {
                _driveStatus.value = DriveBackupStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearDriveStatus() {
        _driveStatus.value = null
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                _isClearing.value = true
                _clearDataResult.value = null
                
                val linkCount = linkRepository.getAllLinks().first().size
                val collectionCount = collectionRepository.getAllCollections().first().size
                
                if (linkCount == 0 && collectionCount == 0) {
                    _clearDataResult.value = ClearDataResult.Error("No data to clear")
                    return@launch
                }
                
                linkRepository.deleteAllLinks()
                collectionRepository.deleteAllCollections()
                
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

    fun refreshMetadata() {
        if (_metadataRefreshProgress.value != null) return

        _isMetadataDialogHidden.value = false

        viewModelScope.launch {
            try {
                _metadataRefreshProgress.value = 0 to 0
                
                linkRepository.refreshAllMetadata { current, total ->
                    _metadataRefreshProgress.value = current to total
                }
            } catch (e: Exception) {
                // Handle error if needed
            } finally {
                _metadataRefreshProgress.value = null
                _isMetadataDialogHidden.value = false
            }
        }
    }

    fun dismissMetadataDialog() {
        _isMetadataDialogHidden.value = true
    }

}

sealed class ClearDataResult {
    object Success : ClearDataResult()
    data class Error(val message: String) : ClearDataResult()
}

sealed class DriveBackupStatus {
    object InProgress : DriveBackupStatus()
    object Success : DriveBackupStatus()
    object NeedsSignIn : DriveBackupStatus()
    object NeedsPermission : DriveBackupStatus()
    data class Restored(val importedLinks: Int) : DriveBackupStatus()
    data class Error(val message: String) : DriveBackupStatus()
}