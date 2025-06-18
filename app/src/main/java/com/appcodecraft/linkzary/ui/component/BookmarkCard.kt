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
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.sp
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
    val domainColor = getUrlBasedColor(link.url) // Changed to use full URL
    val headerImageUrl = extractHeaderImage(link.url)
    var imageLoadFailed by remember { mutableStateOf(false) }

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
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header image or domain thumbnail - reduced height to match reference
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Reduced from 200dp to match reference
            ) {
                if (headerImageUrl != null && !imageLoadFailed) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(headerImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Website header",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { imageLoadFailed = true },
                        fallback = null
                    )
                }

                // Show domain thumbnail if no image or if image failed to load
                if (headerImageUrl == null || imageLoadFailed) {
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
                            .padding(8.dp)
                            .background(
                                Color.White.copy(alpha = 0.9f),
                                CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Content section - reduced padding to match reference
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp) // Reduced from 16dp
            ) {
                // Title - adjusted typography to match reference
                Text(
                    text = link.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description/Note - smaller text like in reference
                if (link.note.isNotBlank()) {
                    Text(
                        text = link.note,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Bottom section with domain, date and more button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Domain name - styled like in reference
                        Text(
                            text = domain,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )

                        // Collection and date row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Collection indicator - smaller to match reference
                            if (collectionName != null && collectionColor != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp) // Smaller dot
                                            .clip(CircleShape)
                                            .background(Color(collectionColor.toColorInt()))
                                    )
                                    Text(
                                        text = collectionName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Date - smaller text
                            Text(
                                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(link.saveDate),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // More button - smaller to match reference
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(32.dp) // Reduced from 40dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(16.dp), // Reduced from 20dp
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
 * Simplified header image extraction - focusing on reliability
 * Note: Real-world image extraction for sites like Instagram often requires
 * web scraping (parsing HTML for og:image meta tags) or using specific APIs,
 * which cannot be directly implemented or demonstrated within this isolated
 * Android Compose code snippet due to network access and parsing complexities.
 * For such cases, a backend service or a more robust client-side scraping
 * library would typically be used.
 */
fun extractHeaderImage(url: String): String? {
    val domain = extractDomain(url)

    return when {
        // YouTube - use thumbnail (most reliable)
        domain.contains("youtube.com") || domain.contains("youtu.be") -> {
            val videoId = when {
                url.contains("youtube.com/watch?v=") -> url.substringAfter("v=").substringBefore("&")
                url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
                else -> null
            }
            videoId?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" } // Using hqdefault for better reliability
        }

        // GitHub - use repository social image
        domain.contains("github.com") -> {
            val path = url.substringAfter("github.com/")
            val parts = path.split("/")
            if (parts.size >= 2) {
                "https://opengraph.githubassets.com/1/${parts[0]}/${parts[1]}"
            } else null
        }

        // Instagram - direct image extraction from URL is not straightforward without web scraping
        // or an API. For now, it will fall back to the domain thumbnail.
        domain.contains("instagram.com") -> {
            // TODO: In a real application, you would implement web scraping here
            // to find the <meta property="og:image" content="..."> tag in the HTML.
            // This would involve:
            // 1. Making an HTTP request to the Instagram post URL.
            // 2. Parsing the HTML response (e.g., with a library like Jsoup).
            // 3. Extracting the content of the "og:image" meta tag.
            // Example of what you'd look for in the HTML:
            // <meta property="og:image" content="[image_url_here]" />
            null // Fallback to domain thumbnail
        }

        // Twitter/X - for now, return null and let domain thumbnail show
        domain.contains("twitter.com") || domain.contains("x.com") -> {
            null // Fallback to domain thumbnail
        }

        // For other sites, don't attempt complex image extraction to avoid failures
        // This will fall back to the domain thumbnail which is more reliable
        else -> null
    }
}

/**
 * Get a consistent color based on the full URL for variety
 */
fun getUrlBasedColor(url: String): Color {
    val colors = listOf(
        // Primary and Secondary inspired (more muted/desaturated)
        Color(0xFF6200EE), // Deep Purple 500 (similar to Material primary purple)
        Color(0xFF3F51B5), // Indigo 500 (strong but not overly vibrant)
        Color(0xFF03DAC6), // Teal A200 (Material secondary accent, often used for contrast)
        Color(0xFF018786), // Teal 700 (a slightly darker teal for secondary)

        // Neutral and Grayscale variations (essential for minimal design)
        Color(0xFFB00020), // Material Error Red (important for states, but not for general accents)
        Color(0xFF212121), // Grey 900 (very dark, almost black, for strong contrast text/icons)
        Color(0xFF424242), // Grey 800 (dark grey for secondary text/icons)
        Color(0xFF757575), // Grey 600 (medium grey, good for disabled states or subtle elements)
        Color(0xFFBDBDBD), // Grey 400 (light grey, good for dividers or backgrounds)

        // Subtle, desaturated accent colors (minimal use, for variety)
        Color(0xFF8BC34A), // Light Green 500 (more subdued green)
        Color(0xFFFFC107), // Amber 500 (classic Material amber, but use sparingly)
        Color(0xFFFF9800), // Orange 500 (a more toned-down orange)
        Color(0xFF9C27B0), // Purple 500 (classic Material purple)
        Color(0xFF00BCD4), // Cyan 500 (a desaturated cyan)
        Color(0xFFE91E63), // Pink 500 (classic Material pink, but can be a bit strong)
        Color(0xFF00ACC1), // Cyan 600 (slightly darker cyan)
        Color(0xFF6D4C41), // Brown 600 (earthy tone, can be good for minimal)
        Color(0xFF546E7A), // Blue Grey 600 (muted blue-grey)
        Color(0xFFC0CA33), // Lime 600 (a more muted lime green)
    )

    // Use full URL for hash to ensure different colors for same domain
    val hash = url.hashCode()
    val index = kotlin.math.abs(hash) % colors.size
    return colors[index]
}

/**
 * Get a consistent color for a domain (kept for backward compatibility)
 */
fun getDomainColor(domain: String): Color {
    return getUrlBasedColor(domain)
}

/**
 * Domain thumbnail composable - refined to match reference image
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
        // Solid background color instead of gradient for consistency
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Domain initial - adjusted size for smaller card
            val initial = domain.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

            Text(
                text = initial,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 32.sp // Reduced from displayLarge
                ),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Domain name - smaller text
            Text(
                text = domain,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.sp
                ),
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
fun BookmarkCardPreview() {
    LinkzaryTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card with domain thumbnail (like X/Twitter in reference)
            BookmarkCard(
                link = SavedLink(
                    id = 1,
                    title = "𝕏 (formerly Twitter)",
                    url = "https://x.com/NotionHQ",
                    note = "Whenever you need me, I'll be there to greet you 😊",
                    tags = "social",
                    isPinned = false,
                    saveDate = Date()
                ),
                collectionName = null,
                collectionColor = null,
                onCardClick = { },
                onMoreClick = { }
            )

            // Card with YouTube thumbnail
            BookmarkCard(
                link = SavedLink(
                    id = 2,
                    title = "FACELESS Videos 100% Automated",
                    url = "https://youtube.com/watch?v=dQw4w9WgXcQ",
                    note = "Amazing tutorial on automation",
                    tags = "youtube, automation",
                    isPinned = false,
                    saveDate = Date()
                ),
                collectionName = null,
                collectionColor = null,
                onCardClick = { },
                onMoreClick = { }
            )

            // Card with Instagram (will show domain thumbnail as real image extraction isn't implemented here)
            BookmarkCard(
                link = SavedLink(
                    id = 3,
                    title = "Cool Instagram Post",
                    url = "https://www.instagram.com/p/C7Uo_fRRxP5/",
                    note = "Check out this awesome photo!",
                    tags = "instagram, photo",
                    isPinned = false,
                    saveDate = Date()
                ),
                collectionName = "Social Media",
                collectionColor = "#FF6347", // Tomato color for example
                onCardClick = { },
                onMoreClick = { }
            )
        }
    }
}
