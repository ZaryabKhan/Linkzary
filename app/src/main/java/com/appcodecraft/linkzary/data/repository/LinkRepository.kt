package com.appcodecraft.linkzary.data.repository

import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.entity.SavedLink
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LinkRepository @Inject constructor(
    private val savedLinkDao: SavedLinkDao
) {
    fun getAllLinks(): Flow<List<SavedLink>> = savedLinkDao.getAllLinks()

    fun getLinksByCollection(collectionId: Long): Flow<List<SavedLink>> =
        savedLinkDao.getLinksByCollection(collectionId)

    fun getUncategorizedLinks(): Flow<List<SavedLink>> = savedLinkDao.getUncategorizedLinks()

    fun searchLinks(query: String): Flow<List<SavedLink>> = savedLinkDao.searchLinks(query)

    suspend fun getLinkById(id: Long): SavedLink? = savedLinkDao.getLinkById(id)

    suspend fun insertLink(link: SavedLink): Long = savedLinkDao.insertLink(link)

    suspend fun updateLink(link: SavedLink) = savedLinkDao.updateLink(link)

    suspend fun deleteLink(link: SavedLink) = savedLinkDao.deleteLink(link)

    suspend fun deleteAllLinks() = savedLinkDao.deleteAllLinks()

    suspend fun moveToCollection(linkId: Long, collectionId: Long?) =
        savedLinkDao.moveToCollection(linkId, collectionId)

    suspend fun updatePinStatus(linkId: Long, isPinned: Boolean) =
        savedLinkDao.updatePinStatus(linkId, isPinned)
}