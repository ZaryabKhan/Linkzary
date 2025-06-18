package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val domain = extractDomain(link.url)
    val domainColor = getDomainColor(domain)
    val headerImageUrl = extractHeaderImage(link.url)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Large header image or domain thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(domainColor)
            ) {
                if (headerImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(headerImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Website header",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = android.R.drawable.ic_menu_gallery),
                        fallback = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )
                } else {
                    DomainThumbnail(
                        domain = domain,
                        color = domainColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Pin indicator overlay
                if (link.isPinned) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                CircleShape
                            )
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = link.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Domain chip
                Surface(
                    modifier = Modifier.wrapContentSize(),
                    shape = RoundedCornerShape(20.dp),
                    color = domainColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.labelMedium,
                        color = domainColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Note if present
                if (link.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = link.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Bottom section with collection, date and more button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Collection indicator
                        if (collectionName != null && collectionColor != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(collectionColor.toColorInt()))
                                )
                                Text(
                                    text = collectionName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // Date
                        Text(
                            text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(link.saveDate),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    // More button
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(20.dp),
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
    } catch (_: Exception) {
        url
    }
}

/**
 * Extract header image URL for professional look
 */
fun extractHeaderImage(url: String): String? {
    val domain = extractDomain(url)
    
    return when {
        // YouTube - use thumbnail
        domain.contains("youtube.com") || domain.contains("youtu.be") -> {
            val videoId = when {
                url.contains("youtube.com/watch?v=") -> url.substringAfter("v=").substringBefore("&")
                url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
                else -> null
            }
            videoId?.let { "https://img.youtube.com/vi/$it/maxresdefault.jpg" }
        }
        
        // GitHub - use OpenGraph image
        domain.contains("github.com") -> {
            val parts = url.substringAfter("github.com/").split("/")
            if (parts.size >= 2) {
                "https://opengraph.githubassets.com/1/${parts[0]}/${parts[1]}"
            } else null
        }
        
        // Medium - use their header images
        domain.contains("medium.com") -> {
            "https://api.microlink.io/screenshot?url=${java.net.URLEncoder.encode(url, "UTF-8")}&viewport.width=1200&viewport.height=400&type=jpeg&overlay.browser=false&element=article"
        }
        
        // News sites - get header/hero images
        domain.contains("techcrunch.com") ||
        domain.contains("theverge.com") ||
        domain.contains("wired.com") ||
        domain.contains("arstechnica.com") ||
        domain.contains("engadget.com") -> {
            "https://api.microlink.io/screenshot?url=${java.net.URLEncoder.encode(url, "UTF-8")}&viewport.width=1200&viewport.height=400&type=jpeg&overlay.browser=false&element=.hero,.header-image,article img:first-of-type"
        }
        
        // For other sites, don't use header images - will fall back to domain thumbnail
        else -> null
    }
}

/**
 * Get a consistent color for a domain
 */
fun getDomainColor(domain: String): Color {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Violet  
        Color(0xFFEC4899), // Pink
        Color(0xFFEF4444), // Red
        Color(0xFFF97316), // Orange
        Color(0xFFF59E0B), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF06B6D4), // Cyan
        Color(0xFF3B82F6), // Blue
        Color(0xFF8B5CF6), // Purple
        Color(0xFF84CC16), // Lime
        Color(0xFFF43F5E), // Rose
    )
    
    val hash = domain.hashCode()
    val index = kotlin.math.abs(hash) % colors.size
    return colors[index]
}

/**
 * Domain thumbnail composable
 */
@Composable
fun DomainThumbnail(
    domain: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Background with gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.8f),
                            color.copy(alpha = 0.6f)
                        )
                    )
                )
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Domain initial or icon
            val initial = domain.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            
            Text(
                text = initial,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = domain,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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