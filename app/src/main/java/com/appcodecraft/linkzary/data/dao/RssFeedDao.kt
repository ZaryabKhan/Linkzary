package com.appcodecraft.linkzary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.appcodecraft.linkzary.data.entity.RssFeed
import com.appcodecraft.linkzary.data.entity.RssFeedItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RssFeedDao {
    @Query("SELECT * FROM rss_feeds ORDER BY addedDate DESC")
    fun getAllFeeds(): Flow<List<RssFeed>>

    @Query("SELECT * FROM rss_feeds WHERE id = :feedId")
    suspend fun getFeedById(feedId: Long): RssFeed?

    @Query("SELECT * FROM rss_feeds WHERE url = :url LIMIT 1")
    suspend fun getFeedByUrl(url: String): RssFeed?

    @Insert
    suspend fun insertFeed(feed: RssFeed): Long

    @Update
    suspend fun updateFeed(feed: RssFeed)

    @Delete
    suspend fun deleteFeed(feed: RssFeed)

    @Query("DELETE FROM rss_feeds WHERE id = :feedId")
    suspend fun deleteFeedById(feedId: Long)

    @Query("SELECT * FROM rss_feed_items WHERE feedId = :feedId ORDER BY publishedDate DESC")
    fun getFeedItems(feedId: Long): Flow<List<RssFeedItem>>

    @Query("SELECT * FROM rss_feed_items ORDER BY publishedDate DESC")
    fun getAllFeedItems(): Flow<List<RssFeedItem>>

    @Query("SELECT * FROM rss_feed_items WHERE isSaved = 0 ORDER BY publishedDate DESC")
    fun getUnsavedItems(): Flow<List<RssFeedItem>>

    @Query("SELECT * FROM rss_feed_items WHERE id = :itemId")
    suspend fun getItemById(itemId: Long): RssFeedItem?

    @Insert
    suspend fun insertItem(item: RssFeedItem): Long

    @Insert
    suspend fun insertItems(items: List<RssFeedItem>): List<Long>

    @Update
    suspend fun updateItem(item: RssFeedItem)

    @Query("UPDATE rss_feed_items SET isRead = 1 WHERE id = :itemId")
    suspend fun markItemAsRead(itemId: Long)

    @Query("UPDATE rss_feed_items SET isSaved = 1 WHERE id = :itemId")
    suspend fun markItemAsSaved(itemId: Long)

    @Query("DELETE FROM rss_feed_items WHERE feedId = :feedId")
    suspend fun deleteItemsByFeedId(feedId: Long)

    @Query("SELECT COUNT(*) FROM rss_feed_items WHERE feedId = :feedId AND isRead = 0")
    fun getUnreadCount(feedId: Long): Flow<Int>

    @Query("""
        SELECT * FROM rss_feed_items 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        OR url LIKE '%' || :query || '%'
        ORDER BY publishedDate DESC
    """)
    fun searchFeedItems(query: String): Flow<List<RssFeedItem>>
}
