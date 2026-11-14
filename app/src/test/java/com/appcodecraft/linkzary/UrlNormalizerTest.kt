package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.util.UrlNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
