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

    @Query("UPDATE saved_links SET isPinned = :isPinned WHERE id = :linkId")
    suspend fun updatePinStatus(linkId: Long, isPinned: Boolean)
}