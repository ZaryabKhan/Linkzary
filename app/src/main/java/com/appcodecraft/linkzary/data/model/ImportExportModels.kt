package com.appcodecraft.linkzary.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExportData(
    val version: String = "1.0",
    val exportDate: String,
    val collections: List<ExportCollection>,
    val links: List<ExportLink>
)

@Serializable
data class ExportCollection(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class ExportLink(
    val id: Long,
    val url: String,
    val title: String,
    val description: String?,
    val collectionId: Long?,
    val collectionName: String?,
    val isPinned: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val faviconUrl: String?,
    val previewImageUrl: String? = null
)

data class ImportPreview(
    val totalLinks: Int,
    val totalCollections: Int,
    val collections: List<ImportCollectionPreview>,
    val linksWithoutCollection: Int
)

data class ImportCollectionPreview(
    val name: String,
    val linkCount: Int,
    val alreadyExists: Boolean
)

data class ImportProgress(
    val currentStep: ImportStep,
    val progress: Float,
    val message: String,
    val processedItems: Int,
    val totalItems: Int
)

enum class ImportStep {
    PARSING_FILE,
    PROCESSING_COLLECTIONS,
    PROCESSING_LINKS,
    FINALIZING,
    COMPLETED
}

enum class ImportMode {
    MERGE_WITH_EXISTING,
    IMPORT_ONLY
}

data class ImportResult(
    val success: Boolean,
    val importedLinks: Int,
    val importedCollections: Int,
    val skippedLinks: Int,
    val renamedCollections: List<String>,
    val errorMessage: String?
)

data class DuplicateHandlingResult(
    val action: DuplicateAction,
    val newName: String? = null
)

enum class DuplicateAction {
    SKIP,
    RENAME,
    REPLACE
}