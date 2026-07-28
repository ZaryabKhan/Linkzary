package com.appcodecraft.linkzary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.appcodecraft.linkzary.data.entity.SavedLink
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLinkDao {
    @Query("SELECT * FROM saved_links ORDER BY isPinned DESC, saveDate DESC")
    fun getAllLinks(): Flow<List<SavedLink>>

    @Query("SELECT * FROM saved_links")
    fun getAllLinksSync(): List<SavedLink>

    @Query("SELECT * FROM saved_links WHERE collectionId = :collectionId ORDER BY isPinned DESC, saveDate DESC")
    fun getLinksByCollection(collectionId: Long): Flow<List<SavedLink>>

    @Query("SELECT * FROM saved_links WHERE collectionId IS NULL ORDER BY isPinned DESC, saveDate DESC")
    fun getUncategorizedLinks(): Flow<List<SavedLink>>

    @Query("SELECT * FROM saved_links WHERE title LIKE '%' || :query || '%' OR url LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY isPinned DESC, saveDate DESC")
    fun searchLinks(query: String): Flow<List<SavedLink>>

    @Query("SELECT * FROM saved_links WHERE id = :id")
    suspend fun getLinkById(id: Long): SavedLink?
    
    @Query("SELECT * FROM saved_links WHERE url = :url LIMIT 1")
    suspend fun getLinkByUrl(url: String): SavedLink?

    @Insert
    suspend fun insertLink(link: SavedLink): Long

    @Insert
    suspend fun insertLinks(links: List<SavedLink>): List<Long>

    @Update
    suspend fun updateLink(link: SavedLink)

    @Delete
    suspend fun deleteLink(link: SavedLink)

    @Delete
    suspend fun deleteLinks(links: List<SavedLink>)

    @Query("DELETE FROM saved_links")
    suspend fun deleteAllLinks()

    @Transaction
    suspend fun deleteAllLinksWithTransaction() {
        deleteAllLinks()
    }

    @Query("DELETE FROM saved_links WHERE id IN (:linkIds)")
    suspend fun deleteLinksByIds(linkIds: List<Long>)

    @Query("UPDATE saved_links SET collectionId = :collectionId WHERE id = :linkId")
    suspend fun moveToCollection(linkId: Long, collectionId: Long?)

    @Query("UPDATE saved_links SET collectionId = NULL WHERE collectionId = :collectionId")
    suspend fun unlinkLinksFromCollection(collectionId: Long)

    @Query("UPDATE saved_links SET isPinned = :isPinned WHERE id = :linkId")
    suspend fun updatePinStatus(linkId: Long, isPinned: Boolean)

    @Query("UPDATE saved_links SET readStatus = :readStatus WHERE id = :linkId")
    suspend fun updateReadStatus(linkId: Long, readStatus: String)

    @Query("UPDATE saved_links SET isOfflineAvailable = :isOfflineAvailable WHERE id = :linkId")
    suspend fun updateOfflineStatus(linkId: Long, isOfflineAvailable: Boolean)

    @Query("SELECT * FROM saved_links WHERE readStatus = :readStatus ORDER BY isPinned DESC, saveDate DESC")
    fun getLinksByReadStatus(readStatus: String): Flow<List<SavedLink>>

    @Query("SELECT DISTINCT tags FROM saved_links WHERE tags != ''")
    suspend fun getAllTagStrings(): List<String>

    @Query("SELECT * FROM saved_links WHERE tags LIKE '%' || :tag || '%' ORDER BY isPinned DESC, saveDate DESC")
    fun getLinksByTag(tag: String): Flow<List<SavedLink>>

    @Query("UPDATE saved_links SET tags = :newTags WHERE id = :linkId")
    suspend fun updateTags(linkId: Long, newTags: String)



    @Query("UPDATE saved_links SET readingProgress = :progress WHERE id = :linkId")
    suspend fun updateReadingProgress(linkId: Long, progress: Float)

    @Query("UPDATE saved_links SET isAlive = :isAlive, lastChecked = :checkedAt WHERE id = :linkId")
    suspend fun updateLinkHealth(linkId: Long, isAlive: Boolean, checkedAt: java.util.Date)

    @Query("SELECT * FROM saved_links WHERE isAlive = 0 AND lastChecked IS NOT NULL")
    fun getDeadLinks(): Flow<List<SavedLink>>

    @Query("SELECT * FROM saved_links WHERE isOfflineAvailable = 1 ORDER BY saveDate DESC")
    fun getOfflineLinks(): Flow<List<SavedLink>>

    @Query("SELECT * FROM saved_links WHERE lastChecked IS NULL OR lastChecked < :threshold ORDER BY saveDate DESC")
    suspend fun getLinksNeedingHealthCheck(threshold: java.util.Date): List<SavedLink>

    @Query("SELECT DISTINCT tags FROM saved_links WHERE tags != ''")
    fun getAllTagStringsFlow(): Flow<List<String>>

    @Transaction
    suspend fun renameTag(oldTag: String, newTag: String) {
        // Get all links that contain the old tag
        val links = getAllLinksSync()
        links.filter { link ->
            link.tags.split(",").map { it.trim() }.any { it == oldTag }
        }.forEach { link ->
            val updatedTags = link.tags.split(",")
                .map { it.trim() }
                .map { if (it == oldTag) newTag else it }
                .joinToString(",")
            updateTags(link.id, updatedTags)
        }
    }

    @Transaction
    suspend fun deleteTag(tag: String) {
        // Remove the tag from any link that contains it
        val links = getAllLinksSync()
        links.filter { link ->
            link.tags.split(",").map { it.trim() }.any { it == tag }
        }.forEach { link ->
            val updatedTags = link.tags.split(",")
                .map { it.trim() }
                .filter { it != tag }
                .joinToString(",")
            updateTags(link.id, updatedTags)
        }
    }

    @Transaction
    suspend fun deleteTags(tags: List<String>) {
        val tagSet = tags.toSet()
        val links = getAllLinksSync()
        links.filter { link ->
            link.tags.split(",").map { it.trim() }.any { it in tagSet }
        }.forEach { link ->
            val updatedTags = link.tags.split(",")
                .map { it.trim() }
                .filter { it !in tagSet }
                .joinToString(",")
            updateTags(link.id, updatedTags)
        }
    }
}