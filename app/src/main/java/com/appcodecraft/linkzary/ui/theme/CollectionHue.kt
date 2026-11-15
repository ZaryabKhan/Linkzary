package com.appcodecraft.linkzary.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Collection hue system (spec v3 §1.1): every collection gets a deterministic
 * identity hue derived from its id — `hue = hash(id) mod 360` — turned into a
 * container/on-container pair per theme, then tonal-harmonized against the
 * active palette so hues decorate identity without ever clashing with it.
 *
 * User-picked icons always win over hue defaults (bug build 2026-11-15): the
 * hue colors the tile, the stored icon supplies the glyph.
 *
 * Pure and unit-tested in [com.appcodecraft.linkzary.CollectionHueTest].
 */

/** Deterministic hue 0..360 for a collection id (stable across launches). */
fun collectionHue(collectionId: Long): Float {
    val h = abs(collectionId * 2654435761L) // Knuth multiplicative hash
    return (h % 360L).toFloat()
}

/** Light theme: HSL(hue, 60%, 90%) container, HSL(hue, 55%, 28%) on-container. */
fun collectionContainerLight(hue: Float, accent: Color): Color =
    harmonizeWith(hslToColor(hue, 0.60f, 0.90f), accent)

fun collectionOnContainerLight(hue: Float, accent: Color): Color =
    harmonizeWith(hslToColor(hue, 0.55f, 0.28f), accent)

/** Dark theme: HSL(hue, 45%, 26%) container, HSL(hue, 60%, 88%) on-container. */
fun collectionContainerDark(hue: Float, accent: Color): Color =
    harmonizeWith(hslToColor(hue, 0.45f, 0.26f), accent)

fun collectionOnContainerDark(hue: Float, accent: Color): Color =
    harmonizeWith(hslToColor(hue, 0.60f, 0.88f), accent)
