package com.appcodecraft.linkzary.data.service

import android.content.Context
import android.net.Uri
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportExportService @Inject constructor() {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    /**
     * Exports links and collections to JSON format
     */
    suspend fun exportToJson(
        context: Context,
        uri: Uri,
        links: List<SavedLink>,
        collections: List<Collection>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val exportCollections = collections.map { collection ->
                ExportCollection(
                    id = collection.id,
                    name = collection.name,
                    description = "", // Collection entity doesn't have description
                    createdAt = dateFormat.format(collection.createdDate),
                    updatedAt = dateFormat.format(collection.createdDate)
                )
            }
            
            val exportLinks = links.map { link ->
                val collection = collections.find { it.id == link.collectionId }
                ExportLink(
                    id = link.id,
                    url = link.url,
                    title = link.title,
                    description = link.note, // Use note as description
                    collectionId = link.collectionId,
                    collectionName = collection?.name,
                    isPinned = link.isPinned,
                    createdAt = dateFormat.format(link.saveDate),
                    updatedAt = dateFormat.format(link.saveDate),
                    faviconUrl = link.favicon
                )
            }
            
            val exportData = ExportData(
                exportDate = dateFormat.format(Date()),
                collections = exportCollections,
                links = exportLinks
            )
            
            val jsonString = json.encodeToString(exportData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: return@withContext false
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Exports links and collections to CSV format
     */
    suspend fun exportToCsv(
        context: Context,
        uri: Uri,
        links: List<SavedLink>,
        collections: List<Collection>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val csvBuilder = StringBuilder()
            csvBuilder.appendLine("URL,Title,Description,Collection,IsPinned,CreatedAt,UpdatedAt,FaviconUrl")
            
            links.forEach { link ->
                val collection = collections.find { it.id == link.collectionId }
                csvBuilder.appendLine(
                    "${escapeCsvValue(link.url)}," +
                    "${escapeCsvValue(link.title)}," +
                    "${escapeCsvValue(link.note)}," +
                    "${escapeCsvValue(collection?.name ?: "")}," +
                    "${link.isPinned}," +
                    "${escapeCsvValue(dateFormat.format(link.saveDate))}," +
                    "${escapeCsvValue(dateFormat.format(link.saveDate))}," +
                    escapeCsvValue(link.favicon ?: "")
                )
            }
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
                outputStream.flush()
            } ?: return@withContext false
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Parses an import file and returns a preview of what would be imported
     */
    suspend fun parseImportFile(
        context: Context,
        uri: Uri,
        existingCollections: List<Collection>
    ): ImportPreview = withContext(Dispatchers.IO) {
        try {
            val content = readFileContent(context, uri)
            
            if (isJsonFormat(content)) {
                 parseJsonPreview(content, existingCollections)
             } else {
                 parseCsvPreview(content, existingCollections)
             }
        } catch (e: Exception) {
            throw IOException("Failed to parse import file: ${e.message}", e)
        }
    }
    
    /**
     * Imports data from a file with progress tracking
     */
    suspend fun importData(
        context: Context,
        uri: Uri,
        mode: ImportMode,
        existingCollections: List<Collection>,
        existingLinks: List<SavedLink>,
        onProgress: (ImportProgress) -> Unit,
        onCollectionInsert: suspend (Collection) -> Long,
        onLinkInsert: suspend (SavedLink) -> Unit
    ): Flow<ImportResult> = flow {
        try {
            onProgress(createProgress(ImportStep.PARSING_FILE, 0.1f, "Parsing import file...", 0, 0))
            
            val content = readFileContent(context, uri)
            
            val result = if (isJsonFormat(content)) {
                importFromJson(
                    content = content,
                    mode = mode,
                    existingCollections = existingCollections,
                    existingLinks = existingLinks,
                    onProgress = onProgress,
                    onCollectionInsert = onCollectionInsert,
                    onLinkInsert = onLinkInsert
                )
            } else {
                importFromCsv(
                    content = content,
                    mode = mode,
                    existingCollections = existingCollections,
                    existingLinks = existingLinks,
                    onProgress = onProgress,
                    onCollectionInsert = onCollectionInsert,
                    onLinkInsert = onLinkInsert
                )
            }
            
            emit(result)
        } catch (e: Exception) {
            emit(ImportResult(
                success = false,
                importedLinks = 0,
                importedCollections = 0,
                skippedLinks = 0,
                renamedCollections = emptyList(),
                errorMessage = e.message ?: "Unknown error occurred"
            ))
        }
    }
    
    // Private helper methods
    
    private fun readFileContent(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } ?: throw IOException("Cannot open file for reading")
    }
    
    private fun isJsonFormat(content: String): Boolean {
        return content.trim().startsWith("{")
    }
    
    private fun parseJsonPreview(content: String, existingCollections: List<Collection>): ImportPreview {
        try {
            val exportData = json.decodeFromString<ExportData>(content)
            val existingCollectionNames = existingCollections.map { it.name.lowercase() }.toSet()
            
            return ImportPreview(
                totalLinks = exportData.links.size,
                totalCollections = exportData.collections.size,
                collections = exportData.collections.map { exportCollection ->
                    ImportCollectionPreview(
                        name = exportCollection.name,
                        linkCount = exportData.links.count { it.collectionId == exportCollection.id },
                        alreadyExists = existingCollectionNames.contains(exportCollection.name.lowercase())
                    )
                },
                linksWithoutCollection = exportData.links.count { it.collectionId == null }
            )
        } catch (e: Exception) {
             throw IOException("Invalid JSON format: ${e.message}", e)
         }
    }
    
    private fun parseCsvPreview(content: String, existingCollections: List<Collection>): ImportPreview {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) throw IOException("File is empty")
        
        val dataLines = lines.drop(1) // Skip header
        if (dataLines.isEmpty()) throw IOException("No data rows found in CSV")
        
        val existingCollectionNames = existingCollections.map { it.name.lowercase() }.toSet()
        val uniqueCollectionNames = mutableSetOf<String>()
        var linksWithoutCollection = 0
        
        dataLines.forEach { line ->
            try {
                val columns = parseCsvLine(line)
                if (columns.size >= 4) {
                    val collectionName = columns[3].trim().removeSurrounding("\"")
                    if (collectionName.isNotEmpty()) {
                        uniqueCollectionNames.add(collectionName)
                    } else {
                        linksWithoutCollection++
                    }
                }
            } catch (_: Exception) {
                 // Skip malformed lines but continue processing
             }
        }
        
        return ImportPreview(
            totalLinks = dataLines.size,
            totalCollections = uniqueCollectionNames.size,
            collections = uniqueCollectionNames.map { collectionName ->
                ImportCollectionPreview(
                    name = collectionName,
                    linkCount = dataLines.count { line ->
                        try {
                            val columns = parseCsvLine(line)
                            columns.size >= 4 && columns[3].trim().removeSurrounding("\"") == collectionName
                        } catch (_: Exception) {
                             false
                         }
                    },
                    alreadyExists = existingCollectionNames.contains(collectionName.lowercase())
                )
            },
            linksWithoutCollection = linksWithoutCollection
        )
    }
    
    private suspend fun importFromJson(
        content: String,
        mode: ImportMode,
        existingCollections: List<Collection>,
        existingLinks: List<SavedLink>,
        onProgress: (ImportProgress) -> Unit,
        onCollectionInsert: suspend (Collection) -> Long,
        onLinkInsert: suspend (SavedLink) -> Unit
    ): ImportResult {
        val exportData = json.decodeFromString<ExportData>(content)
        val totalItems = exportData.collections.size + exportData.links.size
        
        onProgress(createProgress(
            ImportStep.PROCESSING_COLLECTIONS, 
            0.2f, 
            "Processing collections...", 
            0, 
            totalItems
        ))
        
        val collectionIdMap = mutableMapOf<Long, Long>()
        val renamedCollections = mutableListOf<String>()
        var importedCollections = 0
        
        // Process collections
        exportData.collections.forEachIndexed { index, exportCollection ->
            val existingCollection = existingCollections.find { 
                it.name.equals(exportCollection.name, ignoreCase = true)
            }
            
            val finalName = when {
                existingCollection != null && mode == ImportMode.MERGE_WITH_EXISTING -> {
                     val newName = generateUniqueCollectionName(exportCollection.name, existingCollections)
                     renamedCollections.add("${exportCollection.name} → $newName")
                     newName
                 }
                 existingCollection != null && mode == ImportMode.IMPORT_ONLY -> {
                     // Use existing collection
                     collectionIdMap[exportCollection.id] = existingCollection.id
                     onProgress(createProgress(
                         ImportStep.PROCESSING_COLLECTIONS,
                         0.2f + (0.3f * (index + 1) / exportData.collections.size),
                         "Processing collections... (${index + 1}/${exportData.collections.size})",
                         index + 1,
                         totalItems
                     ))
                     return@forEachIndexed
                 }
                 else -> exportCollection.name
            }
            
            val newCollection = Collection(
                name = finalName,
                color = "#2196F3" // Default blue color
            )
            
            val newId = onCollectionInsert(newCollection)
            collectionIdMap[exportCollection.id] = newId
            importedCollections++
            
            onProgress(createProgress(
                ImportStep.PROCESSING_COLLECTIONS,
                0.2f + (0.3f * (index + 1) / exportData.collections.size),
                "Processing collections... (${index + 1}/${exportData.collections.size})",
                index + 1,
                totalItems
            ))
        }
        
        onProgress(createProgress(
            ImportStep.PROCESSING_LINKS,
            0.5f,
            "Processing links...",
            exportData.collections.size,
            totalItems
        ))
        
        var importedLinks = 0
        var skippedLinks = 0
        
        // Process links
        exportData.links.forEachIndexed { index, exportLink ->
            val isDuplicate = existingLinks.any { it.url == exportLink.url }
            
            if (!isDuplicate || mode == ImportMode.IMPORT_ONLY) {
                val newCollectionId = exportLink.collectionId?.let { collectionIdMap[it] }
                
                val newLink = SavedLink(
                    url = exportLink.url,
                    title = exportLink.title,
                    note = exportLink.description ?: "",
                    collectionId = newCollectionId,
                    isPinned = exportLink.isPinned,
                    favicon = exportLink.faviconUrl
                )
                
                onLinkInsert(newLink)
                importedLinks++
            } else {
                skippedLinks++
            }
            
            onProgress(createProgress(
                ImportStep.PROCESSING_LINKS,
                0.5f + (0.4f * (index + 1) / exportData.links.size),
                "Processing links... (${index + 1}/${exportData.links.size})",
                exportData.collections.size + index + 1,
                totalItems
            ))
        }
        
        onProgress(createProgress(
            ImportStep.COMPLETED,
            1.0f,
            "Import completed successfully!",
            totalItems,
            totalItems
        ))
        
        return ImportResult(
            success = true,
            importedLinks = importedLinks,
            importedCollections = importedCollections,
            skippedLinks = skippedLinks,
            renamedCollections = renamedCollections,
            errorMessage = null
        )
    }
    
    private suspend fun importFromCsv(
        content: String,
        mode: ImportMode,
        existingCollections: List<Collection>,
        existingLinks: List<SavedLink>,
        onProgress: (ImportProgress) -> Unit,
        onCollectionInsert: suspend (Collection) -> Long,
        onLinkInsert: suspend (SavedLink) -> Unit
    ): ImportResult {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) throw IOException("File is empty")
        
        val dataLines = lines.drop(1) // Skip header
        if (dataLines.isEmpty()) throw IOException("No data rows found in CSV")
        
        onProgress(createProgress(
            ImportStep.PROCESSING_COLLECTIONS,
            0.2f,
            "Processing collections...",
            0,
            dataLines.size
        ))
        
        val collectionNameToIdMap = mutableMapOf<String, Long>()
        val renamedCollections = mutableListOf<String>()
        var importedCollections = 0
        var importedLinks = 0
        var skippedLinks = 0
        
        // First pass: identify and create unique collections
        val uniqueCollectionNames = dataLines.mapNotNull { line ->
            try {
                val columns = parseCsvLine(line)
                if (columns.size >= 4) {
                    val collectionName = columns[3].trim().removeSurrounding("\"")
                    if (collectionName.isNotEmpty()) collectionName else null
                } else null
            } catch (_: Exception) {
                 null
             }
        }.distinct()
        
        uniqueCollectionNames.forEach { collectionName ->
            val existingCollection = existingCollections.find { 
                it.name.equals(collectionName, ignoreCase = true)
            }
            
            when {
                existingCollection != null && mode == ImportMode.MERGE_WITH_EXISTING -> {
                    val newName = generateUniqueCollectionName(collectionName, existingCollections)
                    renamedCollections.add("$collectionName → $newName")
                    
                    val newCollection = Collection(
                        name = newName,
                        color = "#2196F3" // Default blue color
                    )
                    
                    val newId = onCollectionInsert(newCollection)
                    collectionNameToIdMap[collectionName] = newId
                    importedCollections++
                }
                existingCollection != null && mode == ImportMode.IMPORT_ONLY -> {
                    collectionNameToIdMap[collectionName] = existingCollection.id
                }
                else -> {
                    val newCollection = Collection(
                        name = collectionName,
                        color = "#2196F3" // Default blue color
                    )
                    
                    val newId = onCollectionInsert(newCollection)
                    collectionNameToIdMap[collectionName] = newId
                    importedCollections++
                }
            }
        }
        
        onProgress(createProgress(
            ImportStep.PROCESSING_LINKS,
            0.4f,
            "Processing links...",
            0,
            dataLines.size
        ))
        
        // Second pass: create links
        dataLines.forEachIndexed { index, line ->
            try {
                val columns = parseCsvLine(line)
                if (columns.size >= 5) {
                    val url = columns[0].trim().removeSurrounding("\"")
                    val title = columns[1].trim().removeSurrounding("\"")
                    val description = columns[2].trim().removeSurrounding("\"").takeIf { it.isNotEmpty() }
                    val collectionName = columns[3].trim().removeSurrounding("\"").takeIf { it.isNotEmpty() }
                    val isPinned = columns[4].trim().removeSurrounding("\"").equals("true", ignoreCase = true)
                    val faviconUrl = if (columns.size > 7) {
                        columns[7].trim().removeSurrounding("\"").takeIf { it.isNotEmpty() }
                    } else null
                    
                    val isDuplicate = existingLinks.any { it.url == url }
                    
                    if (!isDuplicate || mode == ImportMode.IMPORT_ONLY) {
                        val collectionId = collectionName?.let { collectionNameToIdMap[it] }
                        
                        val newLink = SavedLink(
                            url = url,
                            title = title,
                            note = description ?: "",
                            collectionId = collectionId,
                            isPinned = isPinned,
                            favicon = faviconUrl
                        )
                        
                        onLinkInsert(newLink)
                        importedLinks++
                    } else {
                        skippedLinks++
                    }
                }
            } catch (_: Exception) {
                 // Skip malformed lines
                 skippedLinks++
             }
            
            onProgress(createProgress(
                ImportStep.PROCESSING_LINKS,
                0.4f + (0.5f * (index + 1) / dataLines.size),
                "Processing links... (${index + 1}/${dataLines.size})",
                index + 1,
                dataLines.size
            ))
        }
        
        onProgress(createProgress(
            ImportStep.COMPLETED,
            1.0f,
            "Import completed successfully!",
            dataLines.size,
            dataLines.size
        ))
        
        return ImportResult(
            success = true,
            importedLinks = importedLinks,
            importedCollections = importedCollections,
            skippedLinks = skippedLinks,
            renamedCollections = renamedCollections,
            errorMessage = null
        )
    }
    
    private fun generateUniqueCollectionName(baseName: String, existingCollections: List<Collection>): String {
        val existingNames = existingCollections.map { it.name.lowercase() }.toSet()
        var counter = 1
        var candidateName = "${baseName}_imported"
        
        while (existingNames.contains(candidateName.lowercase())) {
            candidateName = "${baseName}_imported_$counter"
            counter++
        }
        
        return candidateName
    }
    
    private fun createProgress(
        step: ImportStep,
        progress: Float,
        message: String,
        processedItems: Int,
        totalItems: Int
    ): ImportProgress {
        return ImportProgress(
            currentStep = step,
            progress = progress.coerceIn(0f, 1f),
            message = message,
            processedItems = processedItems,
            totalItems = totalItems
        )
    }
    
    private fun escapeCsvValue(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return if (escaped.contains(',') || escaped.contains('\"') || escaped.contains('\n')) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
    
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            
            when {
                char == '\"' && !inQuotes -> {
                    inQuotes = true
                }
                char == '\"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        // Escaped quote
                        current.append('\"')
                        i++ // Skip next quote
                    } else {
                        // End of quoted field
                        inQuotes = false
                    }
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> {
                    current.append(char)
                }
            }
            i++
        }
        
        result.add(current.toString())
        return result
    }
}