package com.appcodecraft.linkzary.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.MoreVert
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.theme.collectionContainerDark
import com.appcodecraft.linkzary.ui.theme.collectionContainerLight
import com.appcodecraft.linkzary.ui.theme.collectionHue
import com.appcodecraft.linkzary.ui.theme.collectionOnContainerDark
import com.appcodecraft.linkzary.ui.theme.collectionOnContainerLight
import androidx.compose.foundation.isSystemInDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

private val linkCardDateFormatCache = ConcurrentHashMap<String, SimpleDateFormat>()

private fun getCachedDateFormat(pattern: String): SimpleDateFormat {
    return linkCardDateFormatCache.getOrPut(pattern) {
        SimpleDateFormat(pattern, Locale.getDefault())
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
    cal2.timeInMillis = now.time
    cal2.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal2.set(java.util.Calendar.MINUTE, 0)
    cal2.set(java.util.Calendar.SECOND, 0)
    cal2.set(java.util.Calendar.MILLISECOND, 0)
    return ((cal2.timeInMillis - cal1.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
}

/**
 * Relative footer time for cards, per design audit #3. Pure and testable:
 * [now] is injectable; day math uses local calendar days, not 24h blocks, so
 * timezone/DST boundaries cannot shift a date a day off.
 *
 * [compact] = true (v4.1 footer fix) gives the stable SHORT forms the footer
 * allocates width by — "now", "5m", "2h", "1d", "6d", then "d MMM" — so the
 * timestamp never needs more than a few characters and the collection chip
 * keeps its room. Long forms stay available for other surfaces.
 */
fun formatRelativeSaveDate(date: Date, now: Date = Date(), compact: Boolean = false): String {
    if (compact) {
        val diffInMillis = now.time - date.time
        val minutes = diffInMillis / (60 * 1000)
        if (minutes < 1) return "now"
        if (minutes < 60) return "${minutes}m"
        val days = calendarDaysBetween(date, now)
        if (days < 1) return "${diffInMillis / (60 * 60 * 1000)}h"
        if (days < 7) return "${days}d"
        return getCachedDateFormat("d MMM").format(date)
    }
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
 * LinkCard v3 (spec v3 §2.1) — THE one link card for Home, CollectionDetail
 * and list mode. Vertical structure; NOTHING overlaps the thumbnail:
 *
 *   thumbnail 16:9 (top corners 24dp) — no chips, no text on image, ever
 *   favicon(18dp) + domain (12sp)
 *   title 16sp/600, 2 lines max
 *   snippet 13sp, 2 lines max (optional)
 *   footer: unread dot + timestamp · ONE collection pill · overflow
 *
 * Collection pill = tonal hue pair from the collection's id (§1.1); when no
 * collection, no pill. Favicon fallback = letter tile (never a raw square).
 * Press morph: scale 0.97 spring + corner 24→16dp (80ms), per spec §1.2.
 */
@Composable
fun LinkCard(
    link: SavedLink,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    collectionId: Long? = null,
    collectionName: String? = null,
    isMultiSelectMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onSelectClick: () -> Unit = {}
) {
    val domain = extractDomain(link.url)
    val haptic = LocalHapticFeedback.current
    val haptics = rememberLinkzaryHaptics()
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary

    // Press morph (spec §1.2): 24dp → 16dp corner squash + 0.97 scale.
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "card-press-scale"
    )
    val corner by animateDpAsState(
        targetValue = if (pressed) 16.dp else 24.dp,
        animationSpec = tween(durationMillis = 80),
        label = "card-press-corner"
    )

    val chipContainer: Color
    val chipOnContainer: Color
    if (collectionId != null) {
        val hue = collectionHue(collectionId)
        chipContainer = if (isDark) collectionContainerDark(hue, accent)
        else collectionContainerLight(hue, accent)
        chipOnContainer = if (isDark) collectionOnContainerDark(hue, accent)
        else collectionOnContainerLight(hue, accent)
    } else {
        chipContainer = MaterialTheme.colorScheme.surfaceContainerHigh
        chipOnContainer = MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(isMultiSelectMode, onCardClick, onSelectClick, onLongPress) {
                detectTapGestures(
                    onTap = {
                        // v4.1: subtle M3 CLOCK_TICK on card tap (gated).
                        haptics(LinkzaryHaptic.TICK)
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
        shape = RoundedCornerShape(corner),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary
        ) else null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Thumbnail 16:9, top corners clipped, NOTHING overlaid ──
            val effectiveImageUrl = link.customImageUrl?.takeIf { it.isNotBlank() }
                ?: link.previewImageUrl?.takeIf { it.isNotBlank() }
            var showPreviewFallback by remember(link.id, effectiveImageUrl) { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                if (!effectiveImageUrl.isNullOrBlank() && !showPreviewFallback) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(effectiveImageUrl)
                            .crossfade(true)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .build(),
                        contentDescription = null, // decorative preview; title conveys meaning
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { showPreviewFallback = true }
                    )
                } else {
                    // Quiet fallback tone + smaller centered wordmark (v2 §2.6 #8).
                    val brandTone = com.appcodecraft.linkzary.ui.theme.harmonizedContainerTone(
                        design = accent,
                        source = accent,
                        dark = isDark
                    )
                    val onBrandTone = com.appcodecraft.linkzary.ui.theme.harmonizedOnContainerTone(
                        design = accent,
                        source = accent,
                        dark = isDark
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brandTone),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = domain,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = onBrandTone,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // ── Content column: 16dp sides / 14dp top / 12dp bottom ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 12.dp)
            ) {
                // Favicon + domain row (12sp domain, 18dp letter-tile fallback)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (link.isAlive == false) {
                        Icon(
                            imageVector = Icons.Rounded.Error,
                            contentDescription = stringResource(R.string.link_broken),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    SiteBadge(
                        faviconUrl = link.favicon,
                        domain = domain,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title 16sp/600, 2 lines
                Text(
                    text = link.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (link.readStatus == "READ" || link.readStatus == "ARCHIVED") {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                // Snippet, 2 lines (optional)
                if (link.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = link.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Footer: unread dot + timestamp · ONE collection pill · kebab.
                // v4.1 allocation fix: the timestamp is MEASURED first at its
                // natural (now compact) width — no weight — and the chip takes
                // whatever remains with a minimum comfortable width before it
                // may ellipsize. Equal weights (the old fix) starved BOTH
                // texts even when space was free ("15h a…", "Te…").
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (link.readStatus == "UNREAD") {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = formatRelativeSaveDate(link.saveDate, compact = true),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        softWrap = false
                    )

                    if (collectionName != null && !isMultiSelectMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = chipContainer,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .widthIn(min = 48.dp)
                        ) {
                            Text(
                                text = collectionName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = chipOnContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
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
 * 18dp favicon with letter-tile fallback (spec v3 §2.1): a failed or missing
 * favicon renders as a tinted circle with the domain initial — never a raw
 * colored square. Tile hue derives from the domain hash, harmonized to brand.
 */
@Composable
fun SiteBadge(
    faviconUrl: String?,
    domain: String,
    modifier: Modifier = Modifier
) {
    if (faviconUrl.isNullOrBlank()) {
        LetterTile(domain = domain, modifier = modifier)
        return
    }
    var failed by remember(faviconUrl) { mutableStateOf(false) }
    if (failed) {
        LetterTile(domain = domain, modifier = modifier)
    } else {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(faviconUrl)
                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            modifier = modifier,
            onError = { failed = true }
        )
    }
}

@Composable
private fun LetterTile(domain: String, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary
    val hue = collectionHue(domain.hashCode().toLong())
    val container = if (isDark) collectionContainerDark(hue, accent)
    else collectionContainerLight(hue, accent)
    val onContainer = if (isDark) collectionOnContainerDark(hue, accent)
    else collectionOnContainerLight(hue, accent)
    Box(
        modifier = modifier
            .background(color = container, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = domain.take(1).uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = onContainer
        )
    }
}
