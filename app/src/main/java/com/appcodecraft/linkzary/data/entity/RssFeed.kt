package com.appcodecraft.linkzary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "rss_feeds")
data class RssFeed(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String = "",
    val siteUrl: String? = null,
    val description: String? = null,
    val addedDate: Date = Date(),
    val lastFetched: Date? = null,
    val isActive: Boolean = true
)

@Entity(
    tableName = "rss_feed_items",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = RssFeed::class,
            parentColumns = ["id"],
            childColumns = ["feedId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ]
)
data class RssFeedItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val feedId: Long,
    val title: String,
    val url: String,
    val description: String? = null,
    val publishedDate: Date? = null,
    val isRead: Boolean = false,
    val isSaved: Boolean = false, // true = already saved as a bookmark
    val fetchedDate: Date = Date()
)
