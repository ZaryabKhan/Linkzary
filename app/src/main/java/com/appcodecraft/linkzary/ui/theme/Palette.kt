package com.appcodecraft.linkzary.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Container tones for custom colors (spec v2 §1 rule 2 / §2.1 hero): a tinted
 * container is a REAL container color — the harmonized hue pushed to the
 * Material container tone (dark: tone ~30, light: tone ~90) — never an
 * alpha slab over a surface.
 *
 * Tone here is the perceptual lightness L* scale from Material's tonal
 * palettes; we approximate it with the existing HSL utilities, which track it
 * closely enough for UI containers and keeps the helpers pure and testable.
 */
fun harmonizedContainerTone(design: Color, source: Color, dark: Boolean): Color {
    val harmonized = harmonizeWith(design = design, source = source)
    return withTone(harmonized, targetTone = if (dark) 0.30f else 0.90f)
}

/** Target on-container color for a tone-matched container (dark: tone ~90, light: tone ~20). */
fun harmonizedOnContainerTone(design: Color, source: Color, dark: Boolean): Color {
    val harmonized = harmonizeWith(design = design, source = source)
    return withTone(harmonized, targetTone = if (dark) 0.90f else 0.20f)
}

/** Set [color]'s HSL lightness (≈ tone) to [targetTone], keeping hue and saturation. */
private fun withTone(color: Color, targetTone: Float): Color {
    val hsl = color.toHsl()
    return hslToColor(hsl[0], hsl[1], targetTone, color.alpha)
}
