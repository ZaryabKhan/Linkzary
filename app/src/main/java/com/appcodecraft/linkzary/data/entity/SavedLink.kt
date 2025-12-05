package com.appcodecraft.linkzary.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

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
    val previewImageUrl: String? = null, // Open Graph preview image
    val textContent: String? = null, // Extracted article text for offline reading
    val isOfflineAvailable: Boolean = false
)