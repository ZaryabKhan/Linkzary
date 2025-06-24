package com.appcodecraft.linkzary.data.repository

import com.appcodecraft.linkzary.data.dao.CollectionDao
import com.appcodecraft.linkzary.data.entity.Collection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val collectionDao: CollectionDao
) {
    fun getAllCollections(): Flow<List<Collection>> = collectionDao.getAllCollections().flowOn(Dispatchers.IO)

    fun getRecentCollections(): Flow<List<Collection>> = collectionDao.getRecentCollections().flowOn(Dispatchers.IO)

    suspend fun getCollectionById(id: Long): Collection? = withContext(Dispatchers.IO) {
        collectionDao.getCollectionById(id)
    }

    suspend fun insertCollection(collection: Collection): Long = withContext(Dispatchers.IO) {
        collectionDao.insertCollection(collection)
    }

    suspend fun updateCollection(collection: Collection) = withContext(Dispatchers.IO) {
        collectionDao.updateCollection(collection)
    }

    suspend fun deleteCollection(collection: Collection) = withContext(Dispatchers.IO) {
        collectionDao.deleteCollection(collection)
    }

    suspend fun deleteAllCollections() = withContext(Dispatchers.IO) {
        collectionDao.deleteAllCollectionsWithTransaction()
    }
    
    suspend fun insertCollections(collections: List<Collection>) = withContext(Dispatchers.IO) {
        collectionDao.insertCollections(collections)
    }
    
    suspend fun deleteCollectionsByIds(collectionIds: List<Long>) = withContext(Dispatchers.IO) {
        collectionDao.deleteCollectionsByIds(collectionIds)
    }

    suspend fun getLinksCountInCollection(collectionId: Long): Int = withContext(Dispatchers.IO) {
        collectionDao.getLinksCountInCollection(collectionId)
    }

    fun getLinksCountInCollectionFlow(collectionId: Long): Flow<Int> =
        collectionDao.getLinksCountInCollectionFlow(collectionId).flowOn(Dispatchers.IO)
}