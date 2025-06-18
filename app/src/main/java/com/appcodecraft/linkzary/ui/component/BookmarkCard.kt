package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkCard(
    modifier: Modifier = Modifier,
    link: SavedLink,
    collectionName: String? = null,
    collectionColor: String? = null,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Website preview image (if available)
            val previewImageUrl = extractPreviewImage(link.url)
            if (previewImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(previewImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Website preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Header row with favicon, title, and more button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favicon
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(link.favicon)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Favicon",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Title and URL
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = link.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (link.isPinned) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Text(
                        text = link.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // More button - Enhanced with better size and padding
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Note if present
            if (link.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = link.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Tags if present
            if (link.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    link.tags.split(",").take(3).forEach { tag ->
                        if (tag.isNotBlank()) {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = tag.trim(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer with collection and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Collection indicator
                if (collectionName != null && collectionColor != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(collectionColor.toColorInt()))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                // Save date
                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(link.saveDate),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Extract preview image URL from common website patterns
 */
fun extractPreviewImage(url: String): String? {
    return when {
        // YouTube videos
        url.contains("youtube.com/watch") -> {
            val videoId = url.substringAfter("v=").substringBefore("&")
            "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
        }
        url.contains("youtu.be/") -> {
            val videoId = url.substringAfter("youtu.be/").substringBefore("?")
            "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
        }
        // GitHub repositories
        url.contains("github.com") && url.count { it == '/' } >= 4 -> {
            "https://opengraph.githubassets.com/1/${url.substringAfter("github.com/")}"
        }
        // Twitter/X posts
        url.contains("twitter.com") || url.contains("x.com") -> {
            // Use a generic Twitter preview
            "https://abs.twimg.com/icons/apple-touch-icon-192x192.png"
        }
        // Medium articles
        url.contains("medium.com") -> {
            // Use Medium's default preview
            "https://miro.medium.com/v2/1*m-R_BkNf1Qjr1YbyOIJY2w.png"
        }
        // Dribbble shots
        url.contains("dribbble.com/shots") -> {
            // Use Dribbble's default preview
            "https://cdn.dribbble.com/assets/dribbble-ball-mark-2bd45f09c2fb58dbbfb44766d5d1d07c5a12972d602ef8b32204d28fa3dda554.svg"
        }
        // For other URLs, try to construct a generic preview
        else -> {
            try {
                val domain = url.substringAfter("://").substringBefore("/")
                "https://www.google.com/s2/favicons?domain=$domain&sz=128"
            } catch (e: Exception) {
                null
            }
        }
    }
}

@Preview
@Composable
fun BookmarkCardPreview() {
    LinkzaryTheme {
        BookmarkCard(
            modifier = Modifier.padding(16.dp),
            link = SavedLink(
                id = 1,
                title = "Beautiful UI Design Inspiration",
                url = "https://dribbble.com/shots/example",
                note = "Great examples of modern mobile app design patterns and user interface elements.",
                tags = "design, ui, inspiration",
                isPinned = true,
                saveDate = Date()
            ),
            collectionName = "Design",
            collectionColor = "#FF6B6B",
            onCardClick = { },
            onMoreClick = { }
        )
    }
}