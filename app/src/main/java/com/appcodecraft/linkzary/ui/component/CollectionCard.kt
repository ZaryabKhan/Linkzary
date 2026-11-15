package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.ui.theme.linkzaryHuePair
import java.util.Date

/**
 * The one glyph mapping: M3 rounded glyphs inside a hue-tinted tile. The
 * user-picked icon ALWAYS wins (bug build 2026-11-15); blank/Folder/unknown
 * falls back to the bookmark glyph.
 */
fun getCollectionGlyph(iconName: String?): ImageVector {
    if (iconName.isNullOrBlank() || iconName == "Folder") {
        return Icons.Rounded.Bookmark
    }
    return when (iconName) {
        "Work" -> Icons.Rounded.Work
        "School" -> Icons.Rounded.School
        "Home" -> Icons.Rounded.Home
        "Star" -> Icons.Rounded.Star
        "Favorite" -> Icons.Rounded.Favorite
        "Code" -> Icons.Rounded.Code
        "Book" -> Icons.Rounded.Book
        "Music" -> Icons.Rounded.MusicNote
        "Movie" -> Icons.Rounded.Movie
        "Image" -> Icons.Rounded.Image
        "Map" -> Icons.Rounded.Map
        "Shopping" -> Icons.Rounded.ShoppingCart
        "Travel" -> Icons.Rounded.Flight
        "Sports" -> Icons.Rounded.SportsSoccer
        "Game" -> Icons.Rounded.Gamepad
        else -> Icons.Rounded.Bookmark
    }
}

/** Hue container/on-container pair for a collection id, theme-aware (spec v3 §1.1).
 *  v6 §4: delegates to the one shared linkzaryHuePair util. */
@Composable
fun collectionHuePair(collectionId: Long): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary
    return linkzaryHuePair(collectionId, accent, isDark)
}

/** One mosaic slot for a collection's newest links (spec v6 §3): the seed
 *  drives the generated cover when the link has no image. */
data class CollectionCover(
    val seed: String,
    val imageUrl: String?
)

/**
 * CollectionCard v6 (spec v6 §3): the collection's own content is its cover.
 * 16:9 mosaic — 2×2 of the newest four link covers (real or generated);
 * 1–3 links span the full cover with one cover; 0 links show a calm hue
 * field with one centered 48dp glyph tile. Header: 32dp glyph tile + name +
 * count pill; meta line "Last saved 2h ago". v3's top-edge hue strip and
 * the thumbnail fan are retired.
 */
@Composable
fun CollectionCard(
    collection: Collection,
    linkCount: Int,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    covers: List<CollectionCover> = emptyList(),
    lastSaved: Date? = null
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "collection-press-scale"
    )
    val (hueContainer, hueOnContainer) = collectionHuePair(collection.id)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onCardClick,
                onLongClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onMoreClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            MosaicCover(
                covers = covers,
                hueContainer = hueContainer,
                hueOnContainer = hueOnContainer,
                glyph = getCollectionGlyph(collection.icon)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 12.dp)
            ) {
                // Header: glyph tile + name + count pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(hueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCollectionGlyph(collection.icon),
                            contentDescription = stringResource(R.string.collections_collection_icon),
                            tint = hueOnContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = hueContainer
                    ) {
                        Text(
                            text = linkCount.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = hueOnContainer,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                if (lastSaved != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.collections_last_saved, formatRelativeSaveDate(lastSaved)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * The 16:9 mosaic (spec v6 §3): 2×2 grid of the newest four covers with 3dp
 * inner gaps, outer top corners following the card (24dp) and inner corners
 * 8dp. 1–3 links: the first cover spans the full area. 0 links: the whole
 * cover area in collContainer with one centered 48dp glyph tile.
 */
@Composable
private fun MosaicCover(
    covers: List<CollectionCover>,
    hueContainer: Color,
    hueOnContainer: Color,
    glyph: ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(hueContainer)
    ) {
        when {
            covers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(hueOnContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = glyph,
                            contentDescription = null,
                            tint = hueContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            covers.size < 4 -> {
                CoverPlaceholder(
                    seed = covers[0].seed,
                    imageUrl = covers[0].imageUrl,
                    cornerRadius = 0.dp,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        MosaicCell(covers[0], Modifier.weight(1f))
                        MosaicCell(covers[1], Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        MosaicCell(covers[2], Modifier.weight(1f))
                        MosaicCell(covers[3], Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.MosaicCell(cover: CollectionCover, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        CoverPlaceholder(
            seed = cover.seed,
            imageUrl = cover.imageUrl,
            cornerRadius = 8.dp,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Home shelf mini card (spec v3 §3.1): 148dp wide, name + hue glyph tile +
 * count. The user-picked icon always shows on the tile.
 */
@Composable
fun SmallCollectionCard(
    collection: Collection,
    linkCount: Int,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (hueContainer, hueOnContainer) = collectionHuePair(collection.id)
    Card(
        modifier = modifier
            .width(148.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(hueContainer)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = collection.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(hueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCollectionGlyph(collection.icon),
                        contentDescription = stringResource(R.string.collections_collection_icon),
                        tint = hueOnContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(hueContainer, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = linkCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = hueOnContainer
                    )
                }
            }
        }
    }
}
