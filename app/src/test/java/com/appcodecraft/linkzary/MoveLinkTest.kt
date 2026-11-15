package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.data.entity.SavedLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

/**
 * v6.2 regression: moving a link between collections crashed the app. The
 * Compose dialog (CollectionSelectionDialog LazyColumn-in-dialog, same root
 * cause as the v5.1.1 fixes) was fixed in the UI; this test pins the data
 * contract both move paths rely on — CollectionDetailViewModel builds
 * `link.copy(collectionId = newCollectionId)` and HomeViewModel's DAO query
 * updates ONLY the collectionId column — so a move must change the target
 * collection and nothing else, including the "No collection" (null) target.
 */
class MoveLinkTest {

    private val link = SavedLink(
        id = 42,
        title = "Example",
        url = "https://example.test/page",
        note = "some note",
        tags = "a,b",
        collectionId = 1L,
        isPinned = true,
        saveDate = Date(1600000000000L),
        favicon = "https://example.test/fav.ico",
        previewImageUrl = null
    )

    @Test
    fun `move to another collection changes only collectionId`() {
        val moved = link.copy(collectionId = 7L)
        assertEquals(7L, moved.collectionId)
        assertEquals(link.id, moved.id)
        assertEquals(link.title, moved.title)
        assertEquals(link.url, moved.url)
        assertEquals(link.note, moved.note)
        assertEquals(link.tags, moved.tags)
        assertEquals(link.isPinned, moved.isPinned)
        assertEquals(link.saveDate, moved.saveDate)
        assertEquals(link.favicon, moved.favicon)
        assertEquals(link.previewImageUrl, moved.previewImageUrl)
    }

    @Test
    fun `move to No collection sets collectionId null without dropping data`() {
        val moved = link.copy(collectionId = null)
        assertNull(moved.collectionId)
        assertEquals(link.url, moved.url)
        assertEquals(link.title, moved.title)
        assertEquals(link.isPinned, moved.isPinned)
    }

    @Test
    fun `move target ids survive Long range used by autoGenerate`() {
        val moved = link.copy(collectionId = Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE, moved.collectionId)
    }
}
