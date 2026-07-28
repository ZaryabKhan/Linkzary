package com.appcodecraft.linkzary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class ReadStatus {
    UNREAD, READ, ARCHIVED
}

@Entity(tableName = "saved_links")
data class SavedLink(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val note: String = "",
    val tags: String = "", // Comma-separated tags
    val collectionId: Long? = null,
    val isPinned: Boolean = false,
    val saveDate: Date = Date(),
    val favicon: String? = null,
    val previewImageUrl: String? = null,
    val textContent: String? = null,
    val isOfflineAvailable: Boolean = false,
    val readStatus: String = ReadStatus.UNREAD.name,
    val isAlive: Boolean? = null, // null = unchecked, true = alive, false = dead
    val lastChecked: Date? = null,
    val readingProgress: Float = 0f // 0.0 to 1.0
)