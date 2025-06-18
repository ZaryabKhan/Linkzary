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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail/Preview Image
            val previewImageUrl = extractPreviewImage(link.url)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(previewImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Website preview",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
            
            // Content Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top section with title, domain and pin
                Column {
                    // Title with pin icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = link.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (link.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Domain
                    Text(
                        text = extractDomain(link.url),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Note if present
                    if (link.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = link.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Bottom section with collection, date and more button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Collection indicator
                        if (collectionName != null && collectionColor != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(collectionColor.toColorInt()))
                                )
                                Text(
                                    text = collectionName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Date
                        Text(
                            text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(link.saveDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    // More button
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Extract domain name from URL for display
 */
fun extractDomain(url: String): String {
    return try {
        val domain = url.substringAfter("://").substringBefore("/")
        domain.removePrefix("www.")
    } catch (e: Exception) {
        url
    }
}

/**
 * Extract high-quality preview image URL from various platforms
 */
fun extractPreviewImage(url: String): String? {
    return when {
        // YouTube videos - High quality thumbnails
        url.contains("youtube.com/watch") -> {
            val videoId = url.substringAfter("v=").substringBefore("&")
            "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
        }
        url.contains("youtu.be/") -> {
            val videoId = url.substringAfter("youtu.be/").substringBefore("?")
            "https://img.youtube.com/vi/$videoId/maxresdefault.jpg"
        }
        
        // GitHub repositories - OpenGraph images
        url.contains("github.com") && url.count { it == '/' } >= 4 -> {
            "https://opengraph.githubassets.com/1/${url.substringAfter("github.com/")}"
        }
        
        // Twitter/X posts - Try to extract actual post preview
        url.contains("twitter.com") || url.contains("x.com") -> {
            // Use screenshot service for actual tweet content
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // Medium articles - Use screenshot service for actual article
        url.contains("medium.com") -> {
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // LinkedIn posts and articles
        url.contains("linkedin.com") -> {
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // Instagram posts
        url.contains("instagram.com") -> {
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // Dribbble shots - Actual shot images
        url.contains("dribbble.com/shots") -> {
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // Reddit posts
        url.contains("reddit.com") -> {
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // News websites and blogs - Use screenshot service
        url.contains("techcrunch.com") || url.contains("theverge.com") || 
        url.contains("arstechnica.com") || url.contains("wired.com") ||
        url.contains("engadget.com") || url.contains("mashable.com") -> {
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // Dev.to articles
        url.contains("dev.to") -> {
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // Stack Overflow questions
        url.contains("stackoverflow.com") -> {
            "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
        }
        
        // For other URLs, use screenshot service for actual content preview
        else -> {
            try {
                "https://api.microlink.io/?url=${java.net.URLEncoder.encode(url, "UTF-8")}&screenshot=true&meta=false&embed=screenshot.url"
            } catch (e: Exception) {
                // Fallback to favicon if screenshot service fails
                val domain = url.substringAfter("://").substringBefore("/")
                "https://www.google.com/s2/favicons?domain=$domain&sz=256"
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
                url = "https://dribbble.com/shots/26163314-Website-Banking-UI-Design",
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