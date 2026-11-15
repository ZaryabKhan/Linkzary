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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.painter.ColorPainter
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
    isGrid: Boolean = false,
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
        // Spec §6: cards at 20dp (shapes.large), tone-on-tone surfaceContainerLowest,
        // no border, no shadow — depth comes from the tonal tier, not elevation.
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isSelected) BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary
        ) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Preview Image Section with overlay badges
            // User-set custom image wins, then auto-extracted preview, then favicon-derived fallback
            val effectiveImageUrl = link.customImageUrl?.takeIf { it.isNotBlank() }
                ?: link.previewImageUrl?.takeIf { it.isNotBlank() }
            // Key on both link.id and image url so fallback resets when URL updates
            var showPreviewFallback by remember(link.id, effectiveImageUrl) { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Spec v2 §2.5: large 16:9 (list) / 4:3 (grid) ratios only
                    // for REAL images; the fallback is a compact 56dp strip.
                    .then(
                        if (!effectiveImageUrl.isNullOrBlank() && !showPreviewFallback) {
                            Modifier.aspectRatio(if (isGrid) 4f / 3f else 16f / 9f)
                        } else {
                            Modifier.height(56.dp)
                        }
                    )
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                if (!effectiveImageUrl.isNullOrBlank() && !showPreviewFallback) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(effectiveImageUrl)
                            .crossfade(true)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Preview for ${link.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { showPreviewFallback = true }
                    )
                } else {
                    // Spec v2 §2.5 fallback strip: harmonized domain-tone
                    // container, favicon + small domain label, left-aligned.
                    // No giant wordmark, ever.
                    val domainTone = com.appcodecraft.linkzary.ui.theme.harmonizedContainerTone(
                        design = generateColorFromDomain(domain),
                        source = MaterialTheme.colorScheme.primary,
                        dark = androidx.compose.foundation.isSystemInDarkTheme()
                    )
                    val onDomainTone = com.appcodecraft.linkzary.ui.theme.harmonizedOnContainerTone(
                        design = generateColorFromDomain(domain),
                        source = MaterialTheme.colorScheme.primary,
                        dark = androidx.compose.foundation.isSystemInDarkTheme()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(domainTone)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("https://www.google.com/s2/favicons?domain=$domain&sz=64")
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            error = ColorPainter(onDomainTone.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = domain,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = onDomainTone,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                    // Spec v2 §1 rule 2: real container tone for the badge —
                    // harmonized container + matching on-color, never alpha.
                    val harmonized = com.appcodecraft.linkzary.ui.theme.harmonizedContainerTone(
                        design = parsedColor,
                        source = MaterialTheme.colorScheme.primary,
                        dark = androidx.compose.foundation.isSystemInDarkTheme()
                    )
                    val onHarmonized = com.appcodecraft.linkzary.ui.theme.harmonizedOnContainerTone(
                        design = parsedColor,
                        source = MaterialTheme.colorScheme.primary,
                        dark = androidx.compose.foundation.isSystemInDarkTheme()
                    )
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = harmonized
                    ) {
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = onHarmonized,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Card Content
            Column(modifier = Modifier.padding(16.dp)) {
                // Domain + favicon + broken link indicator
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
                    if (!link.favicon.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(link.favicon)
                                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                .build(),
                            contentDescription = "Favicon for ${link.title}",
                            modifier = Modifier.size(20.dp),
                            error = ColorPainter(generateColorFromDomain(domain).copy(alpha = 0.2f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                                tint = MaterialTheme.colorScheme.primary
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
                            // Full onSurfaceVariant: alpha-stacked variants fell below 4.5:1 (audit #9)
                            MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
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
                        text = formatRelativeSaveDate(link.saveDate),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.home_more_options),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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

/** Whole calendar days between [date] and [now], evaluated in the local timezone. */
internal fun calendarDaysBetween(date: Date, now: Date): Int {
    val cal1 = java.util.Calendar.getInstance()
    val cal2 = java.util.Calendar.getInstance()
    cal1.timeInMillis = date.time
    cal2.timeInMillis = now.time
    cal1.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal1.set(java.util.Calendar.MINUTE, 0)
    cal1.set(java.util.Calendar.SECOND, 0)
    cal1.set(java.util.Calendar.MILLISECOND, 0)
    cal2.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal2.set(java.util.Calendar.MINUTE, 0)
    cal2.set(java.util.Calendar.SECOND, 0)
    cal2.set(java.util.Calendar.MILLISECOND, 0)
    return ((cal2.timeInMillis - cal1.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
}

/**
 * Relative footer time for cards ("Just now", "5m ago", "2h ago", "Yesterday",
 * "4d ago", then an absolute date), per design audit #3. Pure and testable:
 * [now] is injectable; day math uses local calendar days, not 24h blocks, so
 * timezone/DST boundaries cannot shift a date a day off.
 */
fun formatRelativeSaveDate(date: Date, now: Date = Date()): String {
    val diffInMillis = now.time - date.time
    val minutes = diffInMillis / (60 * 1000)
    if (minutes < 1) return "Just now"
    if (minutes < 60) return "${minutes}m ago"
    val hours = diffInMillis / (60 * 60 * 1000)
    val days = calendarDaysBetween(date, now)
    if (days < 1) return "${hours}h ago"
    if (days == 1) return "Yesterday"
    if (days < 7) return "${days}d ago"
    return getCachedDateFormat("d MMM yyyy").format(date)
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
