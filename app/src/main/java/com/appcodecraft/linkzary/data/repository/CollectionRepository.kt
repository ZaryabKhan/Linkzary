package com.appcodecraft.linkzary.data.repository

import com.appcodecraft.linkzary.data.dao.CollectionDao
import com.appcodecraft.linkzary.data.entity.Collection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val collectionDao: CollectionDao
) {
    fun getAllCollections(): Flow<List<Collection>> = collectionDao.getAllCollections()

    fun getRecentCollections(): Flow<List<Collection>> = collectionDao.getRecentCollections()

    suspend fun getCollectionById(id: Long): Collection? = collectionDao.getCollectionById(id)

    suspend fun insertCollection(collection: Collection): Long = collectionDao.insertCollection(collection)

    suspend fun updateCollection(collection: Collection) = collectionDao.updateCollection(collection)

    suspend fun deleteCollection(collection: Collection) = collectionDao.deleteCollection(collection)

    suspend fun deleteAllCollections() = collectionDao.deleteAllCollectionsWithTransaction()
    
    suspend fun insertCollections(collections: List<Collection>) = collectionDao.insertCollections(collections)
    
    suspend fun deleteCollectionsByIds(collectionIds: List<Long>) = collectionDao.deleteCollectionsByIds(collectionIds)

    suspend fun getLinksCountInCollection(collectionId: Long): Int =
        collectionDao.getLinksCountInCollection(collectionId)

    fun getLinksCountInCollectionFlow(collectionId: Long): Flow<Int> =
        collectionDao.getLinksCountInCollectionFlow(collectionId)
}