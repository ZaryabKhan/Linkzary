package com.appcodecraft.linkzary.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.theme.linkzaryHuePair
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
 * Relative time, pure and testable: [now] is injectable; day math uses local
 * calendar days, not 24h blocks, so timezone/DST boundaries cannot shift a
 * date a day off. [compact] gives the stable short forms used where space is
 * tight; v6's source line uses the long form ("2h ago").
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
 * LinkCard v6 (spec v6 §1–2) — the complete card redesign. Quiet body
 * (surfaceContainerLowest, no stroke, no shadow), LOUD cover, metadata
 * minimal:
 *
 *   cover 16:9 (grid) / 96dp square (list) — image OR generated gradient,
 *       never a grey placeholder (CoverPlaceholder)
 *   source line 18dp: unread dot · favicon 16dp · domain · · timestamp
 *       (the timestamp moved here from the footer — de-cramping step 1)
 *   title 16sp/600 max 2 lines · snippet 13sp/400 max 2 lines, collapses
 *   footer 32dp: ONE collection chip left, kebab right — exactly 2 elements
 *
 * Press morph: scale 0.97 + corner morph 80ms (v3 stands).
 */
@Composable
fun LinkCard(
    link: SavedLink,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    collectionId: Long? = null,
    collectionName: String? = null,
    listMode: Boolean = false,
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

    // Press morph (v3 §1.2): 24dp → 16dp corner squash + 0.97 scale.
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "card-press-scale"
    )
    val restCorner = if (listMode) 20.dp else 24.dp
    val corner by animateDpAsState(
        targetValue = if (pressed) 16.dp else restCorner,
        animationSpec = tween(durationMillis = 80),
        label = "card-press-corner"
    )

    val (chipContainer, chipOnContainer) = if (collectionId != null) {
        linkzaryHuePair(collectionId, accent, isDark)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val tapGesture = Modifier.pointerInput(isMultiSelectMode, onCardClick, onSelectClick, onLongPress) {
        detectTapGestures(
            onTap = {
                // v4.1: subtle M3 CLOCK_TICK on card tap (gated).
                haptics(LinkzaryHaptic.TICK)
                if (isMultiSelectMode) onSelectClick() else onCardClick()
            },
            onLongPress = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongPress()
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(tapGesture),
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
        if (listMode) {
            ListCardBody(
                link = link,
                domain = domain,
                collectionName = collectionName,
                chipContainer = chipContainer,
                chipOnContainer = chipOnContainer,
                isMultiSelectMode = isMultiSelectMode,
                onMoreClick = onMoreClick
            )
        } else {
            GridCardBody(
                link = link,
                domain = domain,
                collectionName = collectionName,
                chipContainer = chipContainer,
                chipOnContainer = chipOnContainer,
                isMultiSelectMode = isMultiSelectMode,
                onMoreClick = onMoreClick
            )
        }
    }
}

/** Effective cover image: custom override first, then the preview URL. */
private fun coverImageOf(link: SavedLink): String? =
    link.customImageUrl?.takeIf { it.isNotBlank() }
        ?: link.previewImageUrl?.takeIf { it.isNotBlank() }

/**
 * Grid body (spec v6 §1): 16:9 cover, content column 14dp sides / 12dp top /
 * 14dp bottom with 6dp gaps, footer = chip + kebab only.
 */
@Composable
private fun GridCardBody(
    link: SavedLink,
    domain: String,
    collectionName: String?,
    chipContainer: Color,
    chipOnContainer: Color,
    isMultiSelectMode: Boolean,
    onMoreClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Cover 16:9, top corners clipped, NOTHING overlaps it.
        CoverPlaceholder(
            seed = domain,
            imageUrl = coverImageOf(link),
            cornerRadius = 24.dp,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 12.dp, end = 14.dp, bottom = 14.dp)
        ) {
            SourceLine(link = link, domain = domain)

            Spacer(modifier = Modifier.height(6.dp))

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

            Spacer(modifier = Modifier.height(10.dp))

            CardFooter(
                collectionName = collectionName,
                chipContainer = chipContainer,
                chipOnContainer = chipOnContainer,
                isMultiSelectMode = isMultiSelectMode,
                onMoreClick = onMoreClick
            )
        }
    }
}

/**
 * List body (spec v6 §2): 104dp row, 96dp cover left (16dp radius, 12dp from
 * row edges), quiet content right. Row gap lives on the screen (10dp).
 */
@Composable
private fun ListCardBody(
    link: SavedLink,
    domain: String,
    collectionName: String?,
    chipContainer: Color,
    chipOnContainer: Color,
    isMultiSelectMode: Boolean,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 104.dp)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoverPlaceholder(
            seed = domain,
            imageUrl = coverImageOf(link),
            cornerRadius = 16.dp,
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            SourceLine(link = link, domain = domain)

            Spacer(modifier = Modifier.height(4.dp))

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

            if (link.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = link.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            CardFooter(
                collectionName = collectionName,
                chipContainer = chipContainer,
                chipOnContainer = chipOnContainer,
                isMultiSelectMode = isMultiSelectMode,
                onMoreClick = onMoreClick
            )
        }
    }
}

/**
 * Source line (spec v6 §1.3, de-cramping step 1): unread dot · favicon ·
 * domain · · timestamp — metadata about the source, 18dp tall.
 */
@Composable
private fun SourceLine(link: SavedLink, domain: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(18.dp)
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

        SiteBadge(
            faviconUrl = link.favicon,
            domain = domain,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        val displayDomain = if (domain.length > 20) domain.take(20) + "…" else domain
        Text(
            text = displayDomain,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "·",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 1,
            softWrap = false
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = formatRelativeSaveDate(link.saveDate),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Footer (spec v6 §1.5, de-cramping step 2): 32dp row, chip left, kebab
 * right — exactly two elements. The chip keeps v5.1's corrected text
 * metrics: it renders at natural width when it fits (half-width grid cards
 * show "Test Collection" whole) and ellipsizes only into the space the kebab
 * leaves. Kebab: 24dp glyph in a 40dp target (48dp effective via M3 minimum
 * touch).
 */
@Composable
private fun CardFooter(
    collectionName: String?,
    chipContainer: Color,
    chipOnContainer: Color,
    isMultiSelectMode: Boolean,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (collectionName != null && !isMultiSelectMode) {
            Surface(
                shape = RoundedCornerShape(50),
                color = chipContainer,
                modifier = Modifier
                    .heightIn(min = 28.dp)
                    .weight(1f, fill = false)
            ) {
                Text(
                    text = collectionName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = chipOnContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.home_more_options),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Favicon with letter-tile fallback (v3 rule): a failed or missing favicon
 * renders as a tinted circle with the domain initial — never a raw colored
 * square. Tile hue via the one v6 §4 utility, so "Use Linkzary colors"
 * governs it like every other surface.
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
        coil.compose.AsyncImage(
            model = coil.request.ImageRequest.Builder(LocalContext.current)
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
    val (container, onContainer) = linkzaryHuePair(domain, accent, isDark)
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
