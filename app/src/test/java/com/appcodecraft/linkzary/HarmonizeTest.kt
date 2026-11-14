package com.appcodecraft.linkzary

import androidx.compose.ui.graphics.Color
import com.appcodecraft.linkzary.ui.theme.harmonizeWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Material color harmonization helper (spec §3):
 * design colors that clash with the brand hue shift toward it;
 * close hues pass through; alpha is always preserved.
 */
class HarmonizeTest {

    private val brandIndigo = Color(0xFF4F46E5) // hue ~239

    @Test
    fun `alpha is preserved`() {
        val translucent = Color(0x80FF5722) // orange at 50% alpha
        val result = harmonizeWith(translucent, brandIndigo)
        assertEquals(0x80 / 255f, result.alpha, 0.001f)
    }

    @Test
    fun `opaque color stays opaque`() {
        val result = harmonizeWith(Color(0xFFEF4444), brandIndigo)
        assertEquals(1f, result.alpha, 0.001f)
    }

    @Test
    fun `close hue is unchanged`() {
        // Purple (hue ~270) is within 60deg of indigo (~239) — no shift
        val purple = Color(0xFF9C27B0)
        val result = harmonizeWith(purple, brandIndigo)
        assertEquals(purple, result)
    }

    @Test
    fun `clashing hue is shifted toward brand`() {
        // Red (hue ~0) clashes with indigo (~239, distance ~121deg)
        val red = Color(0xFFEF4444)
        val result = harmonizeWith(red, brandIndigo)
        val before = hueDistance(red, brandIndigo)
        val after = hueDistance(result, brandIndigo)
        assertTrue(
            "harmonized hue should be closer to brand ($before -> $after)",
            after < before
        )
    }

    @Test
    fun `neutral colors pass through`() {
        // Zero saturation: hue is meaningless, must not crash or shift lightness
        val gray = Color(0xFF808080)
        val result = harmonizeWith(gray, brandIndigo)
        assertEquals(gray.red, result.red, 0.01f)
        assertEquals(gray.green, result.green, 0.01f)
        assertEquals(gray.blue, result.blue, 0.01f)
    }

    @Test
    fun `harmonized color is never identical to clashing input`() {
        val green = Color(0xFF22C55E) // hue ~142
        val result = harmonizeWith(green, brandIndigo)
        assertTrue(result != green)
    }

    private fun hueDistance(a: Color, b: Color): Float {
        val ha = hslHue(a)
        val hb = hslHue(b)
        var diff = kotlin.math.abs(ha - hb)
        if (diff > 180f) diff = 360f - diff
        return diff
    }

    private fun hslHue(c: Color): Float {
        val max = maxOf(c.red, c.green, c.blue)
        val min = minOf(c.red, c.green, c.blue)
        val delta = max - min
        if (delta == 0f) return 0f
        val h = when (max) {
            c.red -> 60f * (((c.green - c.blue) / delta) % 6f)
            c.green -> 60f * (((c.blue - c.red) / delta) + 2f)
            else -> 60f * (((c.red - c.green) / delta) + 4f)
        }
        return if (h < 0f) h + 360f else h
    }
}
