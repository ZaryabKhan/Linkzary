package com.appcodecraft.linkzary.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.model.*
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.data.service.ImportExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
                _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
                
                val links = linkRepository.getAllLinks().first()
                val collections = collectionRepository.getAllCollections().first()
                
                val success = importExportService.exportToJson(context, uri, links, collections)
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = success,
                    exportError = if (!success) "Failed to export data" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun exportToCsv(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true, exportError = null)
                
                val links = linkRepository.getAllLinks().first()
                val collections = collectionRepository.getAllCollections().first()
                
                val success = importExportService.exportToCsv(context, uri, links, collections)
                
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccess = success,
                    exportError = if (!success) "Failed to export data" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportError = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun previewImport(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingPreview = true, previewError = null)
                
                val existingCollections = collectionRepository.getAllCollections().first()
                
                val preview = importExportService.parseImportFile(
                    context, uri, existingCollections
                )
                
                _importPreview.value = preview
                _uiState.value = _uiState.value.copy(isLoadingPreview = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingPreview = false,
                    previewError = e.message ?: "Failed to parse import file"
                )
            }
        }
    }

    fun importData(context: Context, uri: Uri, mode: ImportMode) {
        viewModelScope.launch {
            try {
                val existingCollections = collectionRepository.getAllCollections().first()
                val existingLinks = linkRepository.getAllLinks().first()
                
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
                        collectionRepository.insertCollection(collection)
                    },
                    onLinkInsert = { link ->
                        linkRepository.insertLink(link)
                    }
                ).collect { result ->
                    _importResult.value = result
                    _importProgress.value = null
                }
                
            } catch (e: Exception) {
                _importResult.value = ImportResult(
                    success = false,
                    errorMessage = e.message ?: "Unknown error occurred",
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