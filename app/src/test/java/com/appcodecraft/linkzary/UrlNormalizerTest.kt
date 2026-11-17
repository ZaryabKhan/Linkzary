package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.util.UrlNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlNormalizerTest {

    @Test
    fun `same page with different scheme and www is equivalent`() {
        assertTrue(
            UrlNormalizer.areEquivalent(
                "https://www.example.com/article",
                "http://example.com/article"
            )
        )
    }

    @Test
    fun `trailing slash and fragment are ignored`() {
        assertTrue(
            UrlNormalizer.areEquivalent(
                "https://example.com/page/",
                "https://example.com/page#section"
            )
        )
    }

    @Test
    fun `tracking parameters are stripped`() {
        assertTrue(
            UrlNormalizer.areEquivalent(
                "https://example.com/post?utm_source=twitter&id=5",
                "https://example.com/post?id=5"
            )
        )
    }

    @Test
    fun `different pages are not equivalent`() {
        assertFalse(
            UrlNormalizer.areEquivalent(
                "https://example.com/a",
                "https://example.com/b"
            )
        )
    }

    @Test
    fun `scheme-less urls match https`() {
        assertTrue(
            UrlNormalizer.areEquivalent(
                "www.example.com",
                "https://example.com"
            )
        )
    }

    @Test
    fun `blank url normalizes to null`() {
        assertNull(UrlNormalizer.normalize("  "))
    }

    @Test
    fun `normal form is stable`() {
        assertEquals(
            UrlNormalizer.normalize("https://Example.com/Path/"),
            UrlNormalizer.normalize("https://example.com/Path")
        )
    }

    @Test
    fun `raw spaces in path still normalize and match encoded form`() {
        // LZ-1: URI() used to throw on raw spaces, so detection silently
        // fell back to exact matching and the link got saved twice.
        assertEquals(
            UrlNormalizer.normalize("https://example.com/my article"),
            UrlNormalizer.normalize("https://www.example.com/my%20article")
        )
    }

    @Test
    fun `non-ascii path still normalizes`() {
        val normalized = UrlNormalizer.normalize("https://example.com/Übersicht")
        assertNotNull(normalized)
        assertEquals(
            normalized,
            UrlNormalizer.normalize("https://example.com/%C3%9Cbersicht")
        )
    }

    @Test
    fun `lone percent sign does not break normalization`() {
        assertEquals(
            UrlNormalizer.normalize("https://example.com/100% done"),
            UrlNormalizer.normalize("https://example.com/100%25%20done")
        )
    }

    @Test
    fun `valid percent encodings are preserved`() {
        // URI.getPath() returns the decoded form, so %2F normalizes to "/".
        // Both sides decode identically, so equivalence is unaffected.
        assertEquals(
            "example.com/a/b",
            UrlNormalizer.normalize("https://example.com/a%2Fb")
        )
    }
}
