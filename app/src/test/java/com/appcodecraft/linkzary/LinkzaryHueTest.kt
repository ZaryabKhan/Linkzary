package com.appcodecraft.linkzary

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.appcodecraft.linkzary.ui.theme.collectionContainerLight
import com.appcodecraft.linkzary.ui.theme.collectionHue
import com.appcodecraft.linkzary.ui.theme.collectionOnContainerLight
import com.appcodecraft.linkzary.ui.theme.linkzaryCoverGradient
import com.appcodecraft.linkzary.ui.theme.linkzaryHue
import com.appcodecraft.linkzary.ui.theme.linkzaryHuePair
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v6 §4 — the one seed→hue path. Deterministic, in range, and shared by
 * favicon tiles, generated covers and collection chrome so the "Use Linkzary
 * colors" toggle governs all of it.
 */
class LinkzaryHueTest {

    private val brandIndigo = Color(0xFF4F46E5)

    @Test
    fun `hue is deterministic and in range`() {
        repeat(50) { i ->
            val h = linkzaryHue("example$i.com")
            assertTrue(h in 0f..360f)
            assertEquals(h, linkzaryHue("example$i.com"), 0.0001f)
        }
    }

    @Test
    fun `hue is case and whitespace insensitive`() {
        assertEquals(linkzaryHue("GitHub.com"), linkzaryHue("github.COM"))
        assertEquals(linkzaryHue("  en.wikipedia.org "), linkzaryHue("en.wikipedia.org"))
    }

    @Test
    fun `distinct domains get distinct hues`() {
        val domains = listOf("github.com", "wikipedia.org", "youtube.com", "reddit.com")
        val hues = domains.map { linkzaryHue(it) }
        assertEquals(hues.size, hues.toSet().size)
    }

    /** Long-id path (collection chrome) keeps the v3 derivation. */
    @Test
    fun `long seed hue matches collectionHue`() {
        repeat(20) { id ->
            val (container, onContainer) = linkzaryHuePair(id.toLong(), brandIndigo, false)
            val hue = collectionHue(id.toLong())
            assertEquals(collectionContainerLight(hue, brandIndigo), container)
            assertEquals(collectionOnContainerLight(hue, brandIndigo), onContainer)
        }
    }

    /** v6 §1.2: generated-cover gradient stops — first stop is the LIGHTER
     *  one in both themes, and dark stops are darker than light stops. */
    @Test
    fun `cover gradient light stop comes first in both themes`() {
        for (domain in listOf("github.com", "wikipedia.org", "substack.com")) {
            val (lightStart, lightEnd) = linkzaryCoverGradient(domain, brandIndigo, dark = false)
            assertTrue(lightStart.luminance() > lightEnd.luminance())

            val (darkStart, darkEnd) = linkzaryCoverGradient(domain, brandIndigo, dark = true)
            assertTrue(darkStart.luminance() > darkEnd.luminance())
            assertTrue(darkEnd.luminance() < lightEnd.luminance())
        }
    }

    /** Chrome pair: light-theme container is much lighter than its on-container. */
    @Test
    fun `chrome pair container is lighter than on-container`() {
        val (container, onContainer) = linkzaryHuePair("github.com", brandIndigo, dark = false)
        assertTrue(container.luminance() > onContainer.luminance())
        // Consistent with the v3 recipe it must compose with.
        val hue = linkzaryHue("github.com")
        assertEquals(collectionContainerLight(hue, brandIndigo), container)
        assertEquals(collectionOnContainerLight(hue, brandIndigo), onContainer)
    }

    /** Dark/light variants differ (theme-awareness). */
    @Test
    fun `pair differs between themes`() {
        val (lightC, _) = linkzaryHuePair("wikipedia.org", brandIndigo, dark = false)
        val (darkC, _) = linkzaryHuePair("wikipedia.org", brandIndigo, dark = true)
        assertNotEquals(lightC, darkC)
    }
}
