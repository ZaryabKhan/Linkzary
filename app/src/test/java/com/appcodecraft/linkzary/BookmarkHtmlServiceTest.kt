package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.service.BookmarkHtmlService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkHtmlServiceTest {

    private val service = BookmarkHtmlService()

    private val sampleHtml = """
        <!DOCTYPE NETSCAPE-Bookmark-file-1>
        <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
        <TITLE>Bookmarks</TITLE>
        <H1>Bookmarks</H1>
        <DL><p>
            <DT><H3>Reading</H3>
            <DL><p>
                <DT><A HREF="https://news.example.com/item/1" ADD_DATE="1700000000" TAGS="tech">News item</A>
                <DT><A HREF="https://blog.example.org/post" ADD_DATE="1700000100">Blog post</A>
            </DL><p>
            <DT><A HREF="https://example.com/loose" ADD_DATE="1700000200">Loose bookmark</A>
        </DL><p>
    """.trimIndent()

    @Test
    fun `parses links, folders and duplicate skips`() {
        val existing = listOf(
            SavedLink(title = "Already saved", url = "https://news.example.com/item/1")
        )
        val result = service.parseBookmarksHtml(sampleHtml, existing)

        assertEquals(2, result.links.size)
        assertEquals("https://blog.example.org/post", result.links[0].url)
        assertEquals(1, result.skippedCount)
        assertEquals(setOf("Reading"), result.folders.keys)
        assertEquals(1, result.folders["Reading"]?.size)
    }

    @Test
    fun `throws on file without bookmarks`() {
        val exception = runCatching {
            service.parseBookmarksHtml(
                "<html><body><p>nothing here</p></body></html>",
                emptyList()
            )
        }.exceptionOrNull()
        assertTrue(exception?.message?.contains("No bookmarks") == true)
    }

    @Test
    fun `export contains netscape structure and folders`() {
        val links = listOf(
            SavedLink(title = "In folder", url = "https://a.example.com", collectionId = 7L),
            SavedLink(title = "Loose", url = "https://b.example.com")
        )
        val collection = com.appcodecraft.linkzary.data.entity.Collection(
            id = 7L, name = "Work", color = "#123456"
        )
        val html = service.buildBookmarksHtml(links, listOf(collection))
        assertTrue(html.contains("NETSCAPE-Bookmark-file-1"))
        assertTrue(html.contains(">Work</H3>"))
        assertTrue(html.contains("HREF=\"https://a.example.com\""))
        assertTrue(html.contains("HREF=\"https://b.example.com\""))
    }

    @Test
    fun `export output parses back through the importer`() {
        val links = listOf(
            SavedLink(title = "In folder", url = "https://a.example.com", collectionId = 7L)
        )
        val collection = com.appcodecraft.linkzary.data.entity.Collection(
            id = 7L, name = "Work", color = "#123456"
        )
        val html = service.buildBookmarksHtml(links, listOf(collection))
        val roundTrip = service.parseBookmarksHtml(html, emptyList())
        assertEquals(1, roundTrip.links.size)
        assertEquals("https://a.example.com", roundTrip.links[0].url)
        assertEquals("Work", roundTrip.folders.keys.first())
    }
}
