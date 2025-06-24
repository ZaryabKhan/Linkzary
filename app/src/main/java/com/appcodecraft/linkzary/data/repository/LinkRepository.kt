package com.appcodecraft.linkzary.data.repository

import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.entity.SavedLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepository @Inject constructor(
    private val savedLinkDao: SavedLinkDao
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

    suspend fun updatePinStatus(linkId: Long, isPinned: Boolean) = withContext(Dispatchers.IO) {
        savedLinkDao.updatePinStatus(linkId, isPinned)
    }
}