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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Website preview image with better aspect ratio
            val previewImageUrl = extractPreviewImage(link.url)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(previewImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Website preview",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header row with favicon, title, and more button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Favicon with better styling
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(link.favicon)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Favicon",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Title and URL with better spacing
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = link.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            lineHeight = MaterialTheme.typography.titleMedium.lineHeight * 1.2
                        )
                        
                        if (link.isPinned) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = extractDomain(link.url),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // More button with better positioning
                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier
                        .size(44.dp)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Note if present with better styling
            if (link.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = link.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                )
            }
            
            // Tags with improved layout
            if (link.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    link.tags.split(",").take(3).forEach { tag ->
                        if (tag.isNotBlank()) {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = tag.trim(),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Footer with collection and date - improved spacing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Collection indicator with better styling
                if (collectionName != null && collectionColor != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(collectionColor.toColorInt()))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                // Save date with better styling
                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(link.saveDate),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
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