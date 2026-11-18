package com.appcodecraft.linkzary.data.repository

import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.entity.SavedLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepository @Inject constructor(
    private val savedLinkDao: SavedLinkDao,
    private val urlMetadataExtractor: com.appcodecraft.linkzary.util.UrlMetadataExtractor
) {
    fun getAllLinks(): Flow<List<SavedLink>> = savedLinkDao.getAllLinks().flowOn(Dispatchers.IO)

    fun getLinksByCollection(collectionId: Long): Flow<List<SavedLink>> =
        savedLinkDao.getLinksByCollection(collectionId).flowOn(Dispatchers.IO)

    fun getUncategorizedLinks(): Flow<List<SavedLink>> = savedLinkDao.getUncategorizedLinks().flowOn(Dispatchers.IO)

    fun searchLinks(query: String): Flow<List<SavedLink>> = savedLinkDao.searchLinks(query).flowOn(Dispatchers.IO)

    suspend fun getLinkById(id: Long): SavedLink? = withContext(Dispatchers.IO) {
        savedLinkDao.getLinkById(id)
    }
    
    suspend fun getLinkByUrl(url: String): SavedLink? = withContext(Dispatchers.IO) {
        savedLinkDao.getLinkByUrl(url)
    }

    /**
     * Finds an already-saved link that points at the same page as [url],
     * ignoring scheme, "www.", trailing slash, fragments and tracking params.
     * Falls back to an exact URL match when [url] cannot be normalized.
     */
    suspend fun getEquivalentLink(url: String): SavedLink? = withContext(Dispatchers.IO) {
        val target = com.appcodecraft.linkzary.util.UrlNormalizer.normalize(url)
            ?: return@withContext savedLinkDao.getLinkByUrl(url)
        savedLinkDao.getAllLinksOnce().firstOrNull { existing ->
            val existingNormalized = com.appcodecraft.linkzary.util.UrlNormalizer.normalize(existing.url)
            existingNormalized == target ||
                // Stored URL unparseable: exact match is the only reliable signal.
                (existingNormalized == null && existing.url == url)
        }
    }

    /**
     * Inserts [link] only when no equivalent link is already saved (LZ-1).
     * Returns true when inserted, false when an equivalent link exists and the
     * insert was skipped. The check happens on Dispatchers.IO immediately
     * before the insert, closing the share-sheet double-save race where two
     * saves pass an earlier UI-level check while metadata is still loading.
     */
    suspend fun insertLinkIfNew(link: SavedLink): Boolean = withContext(Dispatchers.IO) {
        if (getEquivalentLink(link.url) != null) return@withContext false
        savedLinkDao.insertLink(link)
        true
    }

    /**
     * Groups saved links whose normalized URLs are identical.
     * Each group with more than one entry is a duplicate set.
     */
    suspend fun findDuplicateGroups(): List<List<SavedLink>> = withContext(Dispatchers.IO) {
        savedLinkDao.getAllLinksOnce()
            .groupBy { link -> com.appcodecraft.linkzary.util.UrlNormalizer.normalize(link.url) }
            .filter { it.key != null && it.value.size > 1 }
            .values
            .sortedByDescending { it.size }
    }

    /**
     * Merges each duplicate group into its oldest link: union of tags, first
     * non-blank note wins, pinned/read/offline flags win if set, non-blank
     * metadata (favicon, preview, custom image) is carried over. Keeps the
     * collection of the newest entry when the oldest is uncategorized.
     * Returns the number of links removed.
     */
    suspend fun mergeDuplicates(): Int = withContext(Dispatchers.IO) {
        val groups = findDuplicateGroups()
        var removed = 0
        groups.forEach { group ->
            val keep = group.minByOrNull { it.saveDate } ?: return@forEach
            val others = group.filter { it.id != keep.id }

            val mergedTags = group.flatMap { it.tags.split(",") }
                .map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                .joinToString(",")

            val merged = keep.copy(
                tags = mergedTags,
                note = keep.note.ifBlank { others.firstOrNull { it.note.isNotBlank() }?.note ?: "" },
                isPinned = group.any { it.isPinned },
                collectionId = keep.collectionId ?: others.lastOrNull { it.collectionId != null }?.collectionId,
                favicon = keep.favicon ?: others.firstOrNull { !it.favicon.isNullOrBlank() }?.favicon,
                previewImageUrl = keep.previewImageUrl ?: others.firstOrNull { !it.previewImageUrl.isNullOrBlank() }?.previewImageUrl,
                customImageUrl = keep.customImageUrl ?: others.firstOrNull { !it.customImageUrl.isNullOrBlank() }?.customImageUrl,
                textContent = keep.textContent ?: others.firstOrNull { !it.textContent.isNullOrBlank() }?.textContent,
                isOfflineAvailable = group.any { it.isOfflineAvailable },
                readingProgress = group.maxOfOrNull { it.readingProgress } ?: keep.readingProgress
            )
            savedLinkDao.updateLink(merged)
            savedLinkDao.deleteLinksByIds(others.map { it.id })
            removed += others.size
        }
        removed
    }

    suspend fun updateCustomImageUrl(linkId: Long, imageUrl: String?) = withContext(Dispatchers.IO) {
        savedLinkDao.updateCustomImageUrl(linkId, imageUrl)
    }

    suspend fun updateReminderAt(linkId: Long, reminderAt: java.util.Date?) = withContext(Dispatchers.IO) {
        savedLinkDao.updateReminderAt(linkId, reminderAt)
    }

    suspend fun insertLink(link: SavedLink): Long = withContext(Dispatchers.IO) {
        savedLinkDao.insertLink(link)
    }

    suspend fun updateLink(link: SavedLink) = withContext(Dispatchers.IO) {
        savedLinkDao.updateLink(link)
    }

    suspend fun deleteLink(link: SavedLink) = withContext(Dispatchers.IO) {
        savedLinkDao.deleteLink(link)
    }

    suspend fun deleteAllLinks() = withContext(Dispatchers.IO) {
        savedLinkDao.deleteAllLinksWithTransaction()
    }
    
    suspend fun insertLinks(links: List<SavedLink>) = withContext(Dispatchers.IO) {
        savedLinkDao.insertLinks(links)
    }
    
    suspend fun deleteLinksByIds(linkIds: List<Long>) = withContext(Dispatchers.IO) {
        savedLinkDao.deleteLinksByIds(linkIds)
    }

    suspend fun moveToCollection(linkId: Long, collectionId: Long?) = withContext(Dispatchers.IO) {
        savedLinkDao.moveToCollection(linkId, collectionId)
    }

    suspend fun unlinkLinksFromCollection(collectionId: Long) = withContext(Dispatchers.IO) {
        savedLinkDao.unlinkLinksFromCollection(collectionId)
    }

    suspend fun updatePinStatus(linkId: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        savedLinkDao.updatePinStatus(linkId, isPinned)
    }

    suspend fun updateReadStatus(linkId: Long, readStatus: String) = withContext(Dispatchers.IO) {
        savedLinkDao.updateReadStatus(linkId, readStatus)
    }

    suspend fun updateOfflineStatus(linkId: Long, isOfflineAvailable: Boolean) = withContext(Dispatchers.IO) {
        savedLinkDao.updateOfflineStatus(linkId, isOfflineAvailable)
    }

    fun getLinksByReadStatus(readStatus: String): Flow<List<SavedLink>> =
        savedLinkDao.getLinksByReadStatus(readStatus).flowOn(Dispatchers.IO)

    suspend fun getAllTagStrings(): List<String> = withContext(Dispatchers.IO) {
        savedLinkDao.getAllTagStrings()
    }

    suspend fun updateTags(linkId: Long, newTags: String) = withContext(Dispatchers.IO) {
        savedLinkDao.updateTags(linkId, newTags)
    }

    suspend fun refreshAllMetadata(onProgress: (Int, Int) -> Unit) = withContext(Dispatchers.IO) {
        val allLinks = savedLinkDao.getAllLinksSync()
        var processed = 0
        val total = allLinks.size

        allLinks.forEach { link ->
            try {
                val metadata = urlMetadataExtractor.extractMetadata(link.url)
                val updatedLink = link.copy(
                    title = if (metadata.title.isNotBlank()) metadata.title else link.title,
                    favicon = metadata.favicon ?: link.favicon,
                    previewImageUrl = metadata.previewImageUrl ?: link.previewImageUrl
                )
                if (updatedLink != link) {
                    savedLinkDao.updateLink(updatedLink)
                }
            } catch (e: Exception) {
                // Ignore errors for individual links
            }
            processed++
            onProgress(processed, total)
        }
    }

    suspend fun updateReadingProgress(linkId: Long, progress: Float) = withContext(Dispatchers.IO) {
        savedLinkDao.updateReadingProgress(linkId, progress)
    }

    suspend fun updateLinkHealth(linkId: Long, isAlive: Boolean) = withContext(Dispatchers.IO) {
        savedLinkDao.updateLinkHealth(linkId, isAlive, java.util.Date())
    }

    fun getDeadLinks(): Flow<List<SavedLink>> = savedLinkDao.getDeadLinks().flowOn(Dispatchers.IO)

    fun getOfflineLinks(): Flow<List<SavedLink>> = savedLinkDao.getOfflineLinks().flowOn(Dispatchers.IO)

    suspend fun getLinksNeedingHealthCheck(hoursOld: Int = 24): List<SavedLink> = withContext(Dispatchers.IO) {
        val threshold = java.util.Date(System.currentTimeMillis() - hoursOld * 3600_000L)
        savedLinkDao.getLinksNeedingHealthCheck(threshold)
    }

    fun getAllTags(): Flow<List<String>> = savedLinkDao.getAllTagStringsFlow().map { tagStrings ->
        tagStrings.flatMap { raw ->
            raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }.distinct().sorted()
    }.flowOn(Dispatchers.IO)

    suspend fun renameTag(oldTag: String, newTag: String) = withContext(Dispatchers.IO) {
        savedLinkDao.renameTag(oldTag, newTag)
    }

    suspend fun deleteTag(tag: String) = withContext(Dispatchers.IO) {
        savedLinkDao.deleteTag(tag)
    }

    suspend fun deleteTags(tags: List<String>) = withContext(Dispatchers.IO) {
        savedLinkDao.deleteTags(tags)
    }
}