package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appcodecraft.linkzary.ui.theme.linkzaryCoverGradient

/**
 * CoverPlaceholder (spec v6 §1.2 / §6) — THE one cover for every card:
 * a real thumbnail when the web supplies one (centerCrop, nothing overlaps
 * it), otherwise a GENERATED gradient cover — never a grey placeholder with
 * floating domain text (the user's explicit dislike).
 *
 * Generated cover: 135° linear gradient between the two stops of the
 * domain's harmonized hue pair (linkzaryCoverGradient), with a centered
 * 40dp letter tile — white 22% alpha circle, domain initial 16sp/700 in
 * white 90%. The cover is decoration with identity, not a pretending
 * screenshot; the letter tile is decorative (contentDescription null).
 *
 * Shared by LinkCard (grid + list) and the CollectionCard mosaic slots,
 * so every cover-less link gets its own varied-but-coherent color.
 */
@Composable
fun CoverPlaceholder(
    seed: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    letterTileSize: Dp = 40.dp
) {
    val effectiveImageUrl = imageUrl?.takeIf { it.isNotBlank() }
    var imageFailed by remember(seed, effectiveImageUrl) { mutableStateOf(false) }
    val showImage = !effectiveImageUrl.isNullOrBlank() && !imageFailed

    Box(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        if (showImage) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(effectiveImageUrl)
                    .crossfade(true)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .build(),
                contentDescription = null, // decorative preview; title conveys meaning
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { imageFailed = true }
            )
        } else {
            GeneratedCover(
                seed = seed,
                letterTileSize = letterTileSize,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/** The generated gradient + letter tile (spec v6 §1.2). */
@Composable
fun GeneratedCover(
    seed: String,
    modifier: Modifier = Modifier,
    letterTileSize: Dp = 40.dp
) {
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary
    val (startColor, endColor) = linkzaryCoverGradient(seed, accent, isDark)

    Box(
        modifier = modifier.drawBehind {
            // 135° = top-left → bottom-right diagonal, CSS convention.
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(letterTileSize)
                .background(Color.White.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = seed.trim().take(1).uppercase().ifBlank { "•" },
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
