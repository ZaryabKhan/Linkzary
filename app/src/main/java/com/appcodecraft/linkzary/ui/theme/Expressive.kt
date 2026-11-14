package com.appcodecraft.linkzary.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Material 3 expressive shape roles (spec §1). Pills come from CircleShape
 * at call sites (this material3 version has no Shapes.full role).
 */
val LinkzaryShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * Smooth wavy circle used for the expressive accent shapes: the "cookie"
 * scallop (FAB idle, active-collection badge) and the clover (collections
 * shape party, spec §1/§6). Drawn as a polar path so it scales with size.
 */
private fun wavyShape(lobes: Int, depth: Float): Shape = GenericShape { size, _, _ ->
    val radius = min(size.width, size.height) / 2f
    val cx = size.width / 2f
    val cy = size.height / 2f
    val steps = 144
    for (i in 0 until steps) {
        val t = (i * 2.0 * Math.PI / steps).toFloat()
        val r = radius * (1f - depth + depth * abs(cos(lobes * t)))
        val point = Offset(cx + r * cos(t), cy + r * sin(t))
        if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
    }
    close()
}

/** The M3 expressive "cookie" scallop — used sparingly (spec §1). */
val CookieShape = wavyShape(lobes = 8, depth = 0.05f)

/** Four-lobe clover for the collections "shape party" (spec §6). */
val CloverShape = wavyShape(lobes = 4, depth = 0.14f)

/**
 * Approximate Material color harmonization (spec §3): shift a design color's
 * hue toward the brand hue when they clash, keeping saturation, lightness and
 * alpha. Pure and unit-tested in HarmonizeTest.
 */
fun harmonizeWith(design: Color, source: Color): Color {
    val d = design.toHsl()
    val s = source.toHsl()
    var diff = d[0] - s[0]
    diff = ((diff + 540f) % 360f) - 180f
    val newHue = if (abs(diff) > 60f) {
        var h = d[0] - diff * 0.3f
        if (h < 0f) h += 360f
        if (h >= 360f) h -= 360f
        h
    } else {
        d[0]
    }
    return hslToColor(newHue, d[1], d[2], design.alpha)
}

/** [hue 0..360, saturation 0..1, lightness 0..1]; alpha dropped. */
internal fun Color.toHsl(): FloatArray {
    val r = red
    val g = green
    val b = blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val l = (max + min) / 2f
    val s = if (delta == 0f) 0f else delta / (1f - abs(2f * l - 1f))
    val h = when {
        delta == 0f -> 0f
        max == r -> 60f * (((g - b) / delta) % 6f)
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }
    return floatArrayOf(if (h < 0f) h + 360f else h, s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))
}

internal fun hslToColor(h: Float, s: Float, l: Float, alpha: Float = 1f): Color {
    if (s == 0f) return Color(l, l, l, alpha)
    val c = (1f - abs(2f * l - 1f)) * s
    val hp = h / 60f
    val x = c * (1f - abs(hp % 2f - 1f))
    val (r1, g1, b1) = when (hp.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = l - c / 2f
    return Color((r1 + m).coerceIn(0f, 1f), (g1 + m).coerceIn(0f, 1f), (b1 + m).coerceIn(0f, 1f), alpha)
}
