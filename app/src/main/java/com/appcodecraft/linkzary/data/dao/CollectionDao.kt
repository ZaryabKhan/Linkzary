package com.appcodecraft.linkzary.data.dao

import androidx.room.*
import com.appcodecraft.linkzary.data.entity.Collection
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY createdDate DESC")
    fun getAllCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections ORDER BY createdDate DESC LIMIT 5")
    fun getRecentCollections(): Flow<List<Collection>>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getCollectionById(id: Long): Collection?

    @Insert
    suspend fun insertCollection(collection: Collection): Long

    @Insert
    suspend fun insertCollections(collections: List<Collection>): List<Long>

    @Update
    suspend fun updateCollection(collection: Collection)

    @Delete
    suspend fun deleteCollection(collection: Collection)

    @Delete
    suspend fun deleteCollections(collections: List<Collection>)

    @Query("DELETE FROM collections")
    suspend fun deleteAllCollections()

    @Transaction
    suspend fun deleteAllCollectionsWithTransaction() {
        deleteAllCollections()
    }

    @Query("DELETE FROM collections WHERE id IN (:collectionIds)")
    suspend fun deleteCollectionsByIds(collectionIds: List<Long>)

    @Query("SELECT COUNT(*) FROM saved_links WHERE collectionId = :collectionId")
    suspend fun getLinksCountInCollection(collectionId: Long): Int

    @Query("SELECT COUNT(*) FROM saved_links WHERE collectionId = :collectionId")
    fun getLinksCountInCollectionFlow(collectionId: Long): Flow<Int>
}