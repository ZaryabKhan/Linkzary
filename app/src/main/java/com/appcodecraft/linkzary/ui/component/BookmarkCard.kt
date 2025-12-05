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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import android.util.Log
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    val interactionSource = remember { MutableInteractionSource() }
    val domain = extractDomain(link.url)
    var showDomainFallbackIcon by remember { mutableStateOf(false) }

    // Generate favicon URLs with multiple fallbacks
    // The AsyncImage will try the first. If it fails, onError is called.
    val faviconUrls = listOf(
        "https://www.google.com/s2/favicons?domain=${domain}&sz=64",
        "https://${domain}/favicon.ico",
        "https://icons.duckduckgo.com/ip3/${domain}.ico",
        "https://www.google.com/s2/favicons?domain=${domain}&sz=32"
    )

    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(isMultiSelectMode, onCardClick, onSelectClick, onLongPress) {
                detectTapGestures(
                    onTap = {
                        Log.d("BookmarkCard", "Card tapped - isMultiSelectMode: $isMultiSelectMode")
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
        shape = RoundedCornerShape(12.dp), // Reduced corner radius for more compact look
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp, // Reduced elevation for more compact appearance
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
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Preview Image Section (if available)
            var showPreviewFallback by remember { mutableStateOf(false) }
            
            if (!link.previewImageUrl.isNullOrBlank() && !showPreviewFallback) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(link.previewImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Preview for ${link.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop,
                    onError = {
                        // Show fallback UI on error
                        showPreviewFallback = true
                    }
                )
            } else {
                // Fallback UI for missing images
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(generateColorFromDomain(domain).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = domain.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
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

            // Card Content
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
            // Header with collection badge and actions - standardized layout
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Collection badge or selection indicator - positioned at start
                if (isMultiSelectMode) {
                    // Selection indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(24.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.home_selected),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (collectionName != null && collectionColor != null) {
                    // Regular collection badge when not in multi-select mode
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .background(
                                color = Color(collectionColor.toColorInt()).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp) // Smaller radius for compact look
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp) // Reduced padding
                    ) {
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp // Slightly smaller font
                            ),
                            color = Color(collectionColor.toColorInt()),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Actions row - always positioned at end
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp) // Reduced spacing
                ) {
                    if (link.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = stringResource(R.string.home_pinned),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onMoreClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.home_more_options),
                            modifier = Modifier.size(14.dp), // Smaller icon
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

            // Site icon and domain info - more compact layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
            ) {
                // Site favicon with fallback logic - smaller size
                Box(
                    modifier = Modifier
                        .size(32.dp) // Smaller icon size
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (showDomainFallbackIcon) {
                        // Display generated fallback icon (first letter of domain)
                        FallbackSiteIcon(domain = domain, modifier = Modifier.size(32.dp))
                    } else {
                        // Try loading the favicon
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(faviconUrls.first())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Site icon for $domain",
                            modifier = Modifier.size(20.dp), // Smaller icon
                            contentScale = ContentScale.Fit,
                            onError = {
                                // If loading fails, show the domain fallback
                                showDomainFallbackIcon = true
                            }
                        )
                    }
                }

                // Domain and URL info - more compact
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp // Smaller font size
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = link.url,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp // Smaller font size
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Add a little text snippet of saved link when icon loading fails
                    if (showDomainFallbackIcon && link.title.isNotBlank()) {
                        Text(
                            text = link.title.take(30) + if (link.title.length > 30) "..." else "",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp // Smaller font size
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp) // Reduced padding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Reduced spacing

            // Title - more compact but still prominent
            Text(
                text = link.title,
                style = MaterialTheme.typography.titleSmall.copy( // Changed from titleMedium to titleSmall
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp // Reduced line height
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Description/Note - more compact
            if (link.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp)) // Reduced spacing
                Text(
                    text = link.note,
                    style = MaterialTheme.typography.bodySmall.copy(
                        lineHeight = 16.sp, // Reduced line height
                        fontSize = 11.sp // Smaller font size
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2, // Reduced from 3 to 2 lines for more compact display
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tags with more compact styling
            if (link.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp)) // Reduced spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp) // Reduced spacing
                ) {
                    link.tags.split(",").take(3).forEach { tag ->
                        if (tag.isNotBlank()) {
                            Text(
                                text = "#${tag.trim()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 9.sp // Smaller font size
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp)) // Smaller corner radius
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp) // Reduced padding
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp)) // Reduced spacing

            // Footer with enhanced date - more compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSaveDate(link.saveDate),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 9.sp // Smaller font size
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
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

    // Format for recent dates (today and yesterday) - show date and time
    val recentDateFormat = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())

    // Format for older dates - show only date
    val olderDateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    return when {
        diffInDays < 7 -> recentDateFormat.format(date) // Recent dates show time
        else -> olderDateFormat.format(date) // Older dates show only date
    }
}

/**
 * Fallback site icon composable
 * Displays the first letter of the domain on a colored circle.
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
        // Subtle background for the preview to better showcase cards
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background // Use theme background, should be slightly different from surface
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
                        saveDate = Date(System.currentTimeMillis() - 86400000) // Yesterday
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
                        note = "", // Empty note
                        tags = "react, hooks, patterns, javascript",
                        isPinned = false,
                        saveDate = Date(System.currentTimeMillis() - 172800000) // 2 days ago
                    ),
                    collectionName = "Development",
                    collectionColor = "#10B981",
                    onCardClick = { },
                    onMoreClick = { }
                )

                // Card with no collection name or tags to show adaptation
                BookmarkCard(
                    link = SavedLink(
                        id = 3,
                        title = "A Simple Guide to Healthy Eating",
                        url = "https://www.health.org/simple-nutrition-guide",
                        note = "Quick tips for balancing your diet and staying energized throughout the day. Very practical.",
                        tags = "", // Empty tags
                        isPinned = false,
                        saveDate = Date(System.currentTimeMillis() - 604800000) // 7 days ago
                    ),
                    collectionName = null, // No collection
                    collectionColor = null,
                    onCardClick = { },
                    onMoreClick = { }
                )
            }
        }
    }
}

