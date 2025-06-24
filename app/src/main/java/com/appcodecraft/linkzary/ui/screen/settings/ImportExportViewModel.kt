package com.appcodecraft.linkzary.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.model.ImportMode
import com.appcodecraft.linkzary.data.model.ImportPreview
import com.appcodecraft.linkzary.data.model.ImportProgress
import com.appcodecraft.linkzary.data.model.ImportResult
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.data.service.ImportExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    private val importExportService: ImportExportService,
    private val linkRepository: LinkRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportExportUiState())
    val uiState: StateFlow<ImportExportUiState> = _uiState.asStateFlow()

    private val _importProgress = MutableStateFlow<ImportProgress?>(null)
    val importProgress: StateFlow<ImportProgress?> = _importProgress.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    private val _importPreview = MutableStateFlow<ImportPreview?>(null)
    val importPreview: StateFlow<ImportPreview?> = _importPreview.asStateFlow()

    fun exportToJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true, exportError = null, exportSuccess = false)
                
                // Validate data exists before export
                val links = linkRepository.getAllLinks().first()
                val collections = collectionRepository.getAllCollections().first()
                
                if (links.isEmpty() && collections.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = "No data to export. Add some links or collections first."
                    )
                    return@launch
                }
                
                // Validate URI is writable
                try {
                    context.contentResolver.openOutputStream(uri)?.use { }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = "Cannot write to selected file location: ${e.message}"
                    )
                    return@launch
                }
                
                val success = importExportService.exportToJson(context, uri, links, collections)
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = success,
                    exportError = if (!success) "Failed to export data. Please check file permissions and try again." else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = "Export failed: ${e.message ?: "Unknown error occurred"}"
                )
            }
        }
    }

    fun exportToCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true, exportError = null, exportSuccess = false)
                
                // Validate data exists before export
                val links = linkRepository.getAllLinks().first()
                val collections = collectionRepository.getAllCollections().first()
                
                if (links.isEmpty() && collections.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = "No data to export. Add some links or collections first."
                    )
                    return@launch
                }
                
                // Validate URI is writable
                try {
                    context.contentResolver.openOutputStream(uri)?.use { }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = "Cannot write to selected file location: ${e.message}"
                    )
                    return@launch
                }
                
                val success = importExportService.exportToCsv(context, uri, links, collections)
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = success,
                    exportError = if (!success) "Failed to export data. Please check file permissions and try again." else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = "Export failed: ${e.message ?: "Unknown error occurred"}"
                )
            }
        }
    }

    fun previewImport(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingPreview = true, previewError = null)
                _importPreview.value = null
                
                // Validate file is readable
                try {
                    context.contentResolver.openInputStream(uri)?.use { }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPreview = false,
                        previewError = "Cannot read selected file: ${e.message}"
                    )
                    return@launch
                }
                
                // Check file size (limit to 50MB)
                val fileSize = try {
                    context.contentResolver.openInputStream(uri)?.use { it.available() } ?: 0
                } catch (e: Exception) {
                    0
                }
                
                if (fileSize > 50 * 1024 * 1024) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPreview = false,
                        previewError = "File is too large (${fileSize / (1024 * 1024)}MB). Maximum size is 50MB."
                    )
                    return@launch
                }
                
                val existingCollections = collectionRepository.getAllCollections().first()
                
                val preview = importExportService.parseImportFile(
                    context, uri, existingCollections
                )
                
                if (preview.totalLinks == 0 && preview.totalCollections == 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoadingPreview = false,
                        previewError = "No valid data found in the selected file. Please check the file format."
                    )
                    return@launch
                }
                
                _importPreview.value = preview
                _uiState.value = _uiState.value.copy(isLoadingPreview = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPreview = false,
                    previewError = "Failed to parse import file: ${e.message ?: "Unknown error occurred"}"
                )
                _importPreview.value = null
            }
        }
    }

    fun importData(context: Context, uri: Uri, mode: ImportMode) {
        viewModelScope.launch {
            try {
                _importResult.value = null
                _importProgress.value = null
                
                // Validate file is still accessible
                try {
                    context.contentResolver.openInputStream(uri)?.use { }
                } catch (e: Exception) {
                    _importResult.value = ImportResult(
                        success = false,
                        errorMessage = "Cannot read selected file: ${e.message}",
                        importedLinks = 0,
                        importedCollections = 0,
                        skippedLinks = 0,
                        renamedCollections = emptyList()
                    )
                    return@launch
                }
                
                // Get current state for potential rollback
                val existingCollections = collectionRepository.getAllCollections().first()
                val existingLinks = linkRepository.getAllLinks().first()
                val initialCollectionCount = existingCollections.size
                val initialLinkCount = existingLinks.size
                
                var importedCollectionIds = mutableListOf<Long>()
                var importedLinkIds = mutableListOf<Long>()
                
                importExportService.importData(
                    context = context,
                    uri = uri,
                    mode = mode,
                    existingCollections = existingCollections,
                    existingLinks = existingLinks,
                    onProgress = { progress ->
                        _importProgress.value = progress
                    },
                    onCollectionInsert = { collection ->
                        val id = collectionRepository.insertCollection(collection)
                        importedCollectionIds.add(id)
                        id
                    },
                    onLinkInsert = { link ->
                        val id = linkRepository.insertLink(link)
                        importedLinkIds.add(id)
                    }
                ).collect { result ->
                    if (result.success) {
                        // Verify import was successful
                        val finalCollectionCount = collectionRepository.getAllCollections().first().size
                        val finalLinkCount = linkRepository.getAllLinks().first().size
                        
                        val actualImportedCollections = finalCollectionCount - initialCollectionCount
                        val actualImportedLinks = finalLinkCount - initialLinkCount
                        
                        _importResult.value = result.copy(
                            importedCollections = actualImportedCollections,
                            importedLinks = actualImportedLinks
                        )
                    } else {
                        _importResult.value = result
                    }
                    _importProgress.value = null
                }
                
            } catch (e: Exception) {
                _importResult.value = ImportResult(
                    success = false,
                    errorMessage = "Import failed: ${e.message ?: "Unknown error occurred"}",
                    importedLinks = 0,
                    importedCollections = 0,
                    skippedLinks = 0,
                    renamedCollections = emptyList()
                )
                _importProgress.value = null
            }
        }
    }

    fun clearExportState() {
        _uiState.value = _uiState.value.copy(
            exportSuccess = false,
            exportError = null,
            isExporting = false
        )
    }

    fun clearImportState() {
        _importPreview.value = null
        _importResult.value = null
        _importProgress.value = null
        _uiState.value = _uiState.value.copy(
            isLoadingPreview = false,
            previewError = null
        )
    }

    fun clearPreviewError() {
        _uiState.value = _uiState.value.copy(previewError = null)
    }
}

data class ImportExportUiState(
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportError: String? = null,
    val isLoadingPreview: Boolean = false,
    val previewError: String? = null
)