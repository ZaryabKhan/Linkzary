package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

private val dateFormatCache = ConcurrentHashMap<String, SimpleDateFormat>()

private fun getCachedDateFormat(pattern: String): SimpleDateFormat {
    return dateFormatCache.getOrPut(pattern) {
        SimpleDateFormat(pattern, Locale.getDefault())
    }
}

@Composable
fun BookmarkCard(
    modifier: Modifier = Modifier,
    link: SavedLink,
    collectionName: String? = null,
    collectionColor: String? = null,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit,
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onSelectClick: () -> Unit = {}
) {
    val domain = extractDomain(link.url)
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(isMultiSelectMode, onCardClick, onSelectClick, onLongPress) {
                detectTapGestures(
                    onTap = {
                        if (isMultiSelectMode) {
                            onSelectClick()
                        } else {
                            onCardClick()
                        }
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                )
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            hoveredElevation = 3.dp
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 0.5.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Preview Image Section with overlay badges
            var showPreviewFallback by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                if (!link.previewImageUrl.isNullOrBlank() && !showPreviewFallback) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(link.previewImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Preview for ${link.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { showPreviewFallback = true }
                    )
                } else {
                    // Fallback: domain-colored box with domain name
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(generateColorFromDomain(domain).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = domain.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = generateColorFromDomain(domain),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Multi-select checkbox overlay (top-left)
                if (isMultiSelectMode) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .size(28.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.home_selected),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Pin overlay (top-right) — only when not in multi-select
                if (link.isPinned && !isMultiSelectMode) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 2.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = stringResource(R.string.home_pinned),
                            modifier = Modifier.padding(6.dp).size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Collection badge overlay (bottom-left)
                if (collectionName != null && collectionColor != null && !isMultiSelectMode) {
                    val parsedColor = runCatching { Color(collectionColor.toColorInt()) }
                        .getOrDefault(MaterialTheme.colorScheme.primary)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = parsedColor.copy(alpha = 0.85f),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Card Content
            Column(modifier = Modifier.padding(16.dp)) {
                // Domain + broken link indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (link.isAlive == false) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = stringResource(R.string.link_broken),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title with read status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (link.readStatus) {
                        "READ" -> {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = stringResource(R.string.cd_read_status_read),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                        "ARCHIVED" -> {
                            Icon(
                                imageVector = Icons.Default.Archive,
                                contentDescription = stringResource(R.string.cd_read_status_archived),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    Text(
                        text = link.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (link.readStatus == "UNREAD") FontWeight.SemiBold else FontWeight.Normal,
                            lineHeight = 22.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (link.readStatus == "READ" || link.readStatus == "ARCHIVED") {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Note
                if (link.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = link.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Tags
                if (link.tags.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        link.tags.split(",").take(3).forEach { tag ->
                            if (tag.isNotBlank()) {
                                Text(
                                    text = "#${tag.trim()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer: date + more button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatSaveDate(link.saveDate),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.home_more_options),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Enhanced domain extraction with better handling
 */
fun extractDomain(url: String): String {
    return try {
        val cleanUrl = url.removePrefix("http://").removePrefix("https://")
        val domain = cleanUrl.substringBefore("/").substringBefore("?")
        domain.removePrefix("www.").lowercase()
    } catch (e: Exception) {
        "unknown"
    }
}

/**
 * Format save date with actual formatted date instead of relative labels
 */
fun formatSaveDate(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

    return when {
        diffInDays < 7 -> getCachedDateFormat("d MMM yyyy, h:mm a").format(date)
        else -> getCachedDateFormat("d MMM yyyy").format(date)
    }
}

/**
 * Fallback site icon composable
 */
@Composable
fun FallbackSiteIcon(
    domain: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = generateColorFromDomain(domain),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = domain.take(1).uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
}

/**
 * Generate consistent color from domain name
 */
fun generateColorFromDomain(domain: String): Color {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Violet
        Color(0xFFEC4899), // Pink
        Color(0xFFEF4444), // Red
        Color(0xFFF59E0B), // Amber
        Color(0xFF10B981), // Emerald
        Color(0xFF06B6D4), // Cyan
        Color(0xFF3B82F6), // Blue
    )

    val hash = domain.hashCode()
    return colors[kotlin.math.abs(hash) % colors.size]
}

@Preview
@Composable
fun EnhancedBookmarkCardPreview() {
    LinkzaryTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BookmarkCard(
                    link = SavedLink(
                        id = 1,
                        title = "Beautiful UI Design Inspiration & Best Practices",
                        url = "https://dribbble.com/shots/example-design-inspiration",
                        note = "An amazing collection of modern UI designs with clean layouts and stunning visual hierarchy. Perfect for reference.",
                        tags = "design, ui, inspiration",
                        isPinned = true,
                        saveDate = Date(System.currentTimeMillis() - 86400000)
                    ),
                    collectionName = "Design",
                    collectionColor = "#6366F1",
                    onCardClick = { },
                    onMoreClick = { }
                )

                BookmarkCard(
                    link = SavedLink(
                        id = 2,
                        title = "Advanced React Patterns and Hooks",
                        url = "https://github.com/react-patterns/advanced-hooks",
                        note = "",
                        tags = "react, hooks, patterns, javascript",
                        isPinned = false,
                        saveDate = Date(System.currentTimeMillis() - 172800000)
                    ),
                    collectionName = "Development",
                    collectionColor = "#10B981",
                    onCardClick = { },
                    onMoreClick = { }
                )

                BookmarkCard(
                    link = SavedLink(
                        id = 3,
                        title = "A Simple Guide to Healthy Eating",
                        url = "https://www.health.org/simple-nutrition-guide",
                        note = "Quick tips for balancing your diet and staying energized throughout the day. Very practical.",
                        tags = "",
                        isPinned = false,
                        saveDate = Date(System.currentTimeMillis() - 604800000)
                    ),
                    collectionName = null,
                    collectionColor = null,
                    onCardClick = { },
                    onMoreClick = { }
                )
            }
        }
    }
}
