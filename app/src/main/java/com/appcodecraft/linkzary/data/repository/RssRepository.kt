package com.appcodecraft.linkzary.data.repository

import com.appcodecraft.linkzary.data.dao.RssFeedDao
import com.appcodecraft.linkzary.data.entity.RssFeed
import com.appcodecraft.linkzary.data.entity.RssFeedItem
import com.prof18.rssparser.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RssRepository @Inject constructor(
    private val rssFeedDao: RssFeedDao,
    private val rssParser: RssParser
) {
    fun getAllFeeds(): Flow<List<RssFeed>> = rssFeedDao.getAllFeeds().flowOn(Dispatchers.IO)

    fun getFeedItems(feedId: Long): Flow<List<RssFeedItem>> =
        rssFeedDao.getFeedItems(feedId).flowOn(Dispatchers.IO)

    fun getAllFeedItems(): Flow<List<RssFeedItem>> =
        rssFeedDao.getAllFeedItems().flowOn(Dispatchers.IO)

    fun getUnsavedItems(): Flow<List<RssFeedItem>> =
        rssFeedDao.getUnsavedItems().flowOn(Dispatchers.IO)

    suspend fun getFeedByUrl(url: String): RssFeed? = withContext(Dispatchers.IO) {
        rssFeedDao.getFeedByUrl(url)
    }

    suspend fun addFeed(url: String) = withContext(Dispatchers.IO) {
        val feed = RssFeed(url = url)
        val feedId = rssFeedDao.insertFeed(feed)
        // Try to fetch initial items
        try {
            refreshFeed(feedId)
        } catch (e: Exception) {
            // Feed added but initial fetch failed; will retry later
        }
    }

    suspend fun deleteFeed(feed: RssFeed) = withContext(Dispatchers.IO) {
        rssFeedDao.deleteFeed(feed)
    }

    suspend fun markItemAsRead(itemId: Long) = withContext(Dispatchers.IO) {
        rssFeedDao.markItemAsRead(itemId)
    }

    suspend fun markItemAsSaved(itemId: Long) = withContext(Dispatchers.IO) {
        rssFeedDao.markItemAsSaved(itemId)
    }

    suspend fun refreshFeed(feedId: Long) = withContext(Dispatchers.IO) {
        val feed = rssFeedDao.getFeedById(feedId) ?: return@withContext

        val channel = rssParser.getRssChannel(feed.url)

        // Update feed metadata
        val updatedFeed = feed.copy(
            title = channel.title ?: feed.title,
            siteUrl = channel.link,
            description = channel.description,
            lastFetched = Date()
        )
        rssFeedDao.updateFeed(updatedFeed)

        // Insert new items (skip duplicates by URL)
        val existingItems = rssFeedDao.getFeedItems(feedId).first()
        val existingUrls = existingItems.map { it.url }.toSet()

        val newItems = channel.items.mapNotNull { rssItem ->
            val itemUrl = rssItem.link ?: return@mapNotNull null
            if (itemUrl in existingUrls) return@mapNotNull null

            RssFeedItem(
                feedId = feedId,
                title = rssItem.title ?: "Untitled",
                url = itemUrl,
                description = rssItem.description,
                publishedDate = rssItem.pubDate?.let { parseDate(it) },
                fetchedDate = Date()
            )
        }

        if (newItems.isNotEmpty()) {
            rssFeedDao.insertItems(newItems)
        }
    }

    suspend fun refreshAllFeeds() = withContext(Dispatchers.IO) {
        val feedList = rssFeedDao.getAllFeeds().first()
        feedList.forEach { feed ->
            try {
                refreshFeed(feed.id)
            } catch (e: Exception) {
                // Skip failed feeds
            }
        }
    }

    suspend fun searchFeedItems(query: String): Flow<List<RssFeedItem>> = withContext(Dispatchers.IO) {
        rssFeedDao.searchFeedItems(query)
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            val formats = listOf(
                java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", java.util.Locale.ENGLISH),
                java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH),
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", java.util.Locale.ENGLISH),
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ENGLISH),
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.ENGLISH)
            )
            for (format in formats) {
                try {
                    return format.parse(dateString)
                } catch (_: Exception) {
                    continue
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
