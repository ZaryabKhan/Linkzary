package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.ui.theme.collectionContainerDark
import com.appcodecraft.linkzary.ui.theme.collectionContainerLight
import com.appcodecraft.linkzary.ui.theme.collectionHue
import com.appcodecraft.linkzary.ui.theme.collectionOnContainerDark
import com.appcodecraft.linkzary.ui.theme.collectionOnContainerLight
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec v3 §1.1 collection hue system: deterministic per-id hues turned into
 * harmonized container/on-container pairs. The hue composes with (never
 * overrides) the user-picked icon persistence fix.
 */
class CollectionHueTest {

    private val accent = Color(0xFF4648D8)

    @Test
    fun `hue is deterministic and in range`() {
        for (id in listOf(0L, 1L, 2L, 42L, 999999L, -7L)) {
            val h = collectionHue(id)
            assertEquals(h, collectionHue(id))
            assertTrue("hue $h out of range for id $id", h >= 0f && h < 360f)
        }
    }

    @Test
    fun `different ids spread across hues`() {
        val hues = (1L..20L).map { collectionHue(it) }.toSet()
        assertTrue("expected spread, got $hues", hues.size > 10)
    }

    @Test
    fun `light pair is a light container with dark on-color`() {
        val container = collectionContainerLight(collectionHue(1L), accent)
        val on = collectionOnContainerLight(collectionHue(1L), accent)
        val l = { c: Color -> (c.red + c.green + c.blue) / 3f }
        assertTrue(l(container) > 0.7f)
        assertTrue(l(on) < 0.45f)
    }

    @Test
    fun `dark pair is a dark container with light on-color`() {
        val container = collectionContainerDark(collectionHue(1L), accent)
        val on = collectionOnContainerDark(collectionHue(1L), accent)
        val l = { c: Color -> (c.red + c.green + c.blue) / 3f }
        assertTrue(l(container) < 0.4f)
        assertTrue(l(on) > 0.6f)
    }

    @Test
    fun `pairs stay stable for the same id`() {
        val h = collectionHue(7L)
        assertEquals(
            collectionContainerLight(h, accent),
            collectionContainerLight(collectionHue(7L), accent)
        )
        assertEquals(
            collectionOnContainerDark(h, accent),
            collectionOnContainerDark(collectionHue(7L), accent)
        )
    }
}
