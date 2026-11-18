package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.repository.LinkRepository
import com.appcodecraft.linkzary.util.UrlMetadataExtractor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * LZ-1: saved links must be recognized as already saved even when the
 * share-sheet URL differs in scheme, www, encoding, or tracking params,
 * and inserts must not create duplicates.
 */
class LinkRepositoryDedupTest {

    private lateinit var dao: SavedLinkDao
    private lateinit var repository: LinkRepository

    private fun link(url: String, id: Long = 1) = SavedLink(title = "t", url = url, id = id)

    @Before
    fun setup() {
        dao = mock()
        repository = LinkRepository(dao, mock<UrlMetadataExtractor>())
    }

    @Test
    fun `equivalent url variant is detected as already saved`() {
        runBlocking {
            whenever(dao.getAllLinksOnce()).doReturn(listOf(link("https://example.com/article", id = 7)))

            val found = repository.getEquivalentLink("http://www.example.com/article/?utm_source=x")

            assertEquals(7L, found?.id)
        }
    }

    @Test
    fun `raw-space url matches percent-encoded saved url`() {
        runBlocking {
            whenever(dao.getAllLinksOnce()).doReturn(listOf(link("https://example.com/my%20article")))

            val found = repository.getEquivalentLink("https://example.com/my article")

            assertTrue(found != null)
        }
    }

    @Test
    fun `unparseable url falls back to exact match`() {
        runBlocking {
            // Malformed IPv6 authority: URI() cannot parse it, so detection
            // must fall back to an exact URL match instead of returning nothing.
            val url = "http://::1/bad"
            whenever(dao.getLinkByUrl(url)).doReturn(link(url, id = 3))

            val found = repository.getEquivalentLink(url)

            assertEquals(3L, found?.id)
        }
    }

    @Test
    fun `insertLinkIfNew skips insert when equivalent exists`() {
        runBlocking {
            whenever(dao.getAllLinksOnce()).doReturn(listOf(link("https://example.com/article")))

            val inserted = repository.insertLinkIfNew(link("http://www.example.com/article/"))

            assertFalse(inserted)
            verify(dao, never()).insertLink(any())
        }
    }

    @Test
    fun `insertLinkIfNew inserts when nothing equivalent exists`() {
        runBlocking {
            whenever(dao.getAllLinksOnce()).doReturn(emptyList())
            whenever(dao.insertLink(any())).doReturn(1L)

            val inserted = repository.insertLinkIfNew(link("https://other.org/page"))

            assertTrue(inserted)
            verify(dao).insertLink(any())
        }
    }

    @Test
    fun `similar but distinct urls are not treated as duplicates`() {
        runBlocking {
            whenever(dao.getAllLinksOnce()).doReturn(listOf(link("https://example.com/a")))

            val found = repository.getEquivalentLink("https://example.com/b")

            assertFalse(found != null)
        }
    }
}
