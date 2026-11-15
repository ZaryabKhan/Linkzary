package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.service.ImportExportService
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

/**
 * Bug build 2026-11-15: user-chosen collection icons/colors were dropped on
 * backup export + restore (import recreated every collection with defaults).
 * These tests pin the round-trip: export carries icon+color, and the JSON
 * parser reads them back — including legacy backups that lack the fields.
 */
class CollectionIconBackupTest {

    private val service = ImportExportService()

    @Test
    fun `backup json carries collection icon and color`() {
        val collections = listOf(
            Collection(
                id = 1,
                name = "Reading List",
                color = "#6366F1",
                icon = "Book",
                createdDate = Date()
            ),
            Collection(
                id = 2,
                name = "Sports",
                color = "#10B981",
                icon = "Sports",
                createdDate = Date()
            )
        )
        val links = listOf(
            SavedLink(
                id = 10,
                title = "Example",
                url = "https://example.com",
                collectionId = 1,
                saveDate = Date()
            )
        )

        val json = service.buildBackupJson(links, collections)
        val parsed = service.parseBackupJson(json)

        assertEquals(2, parsed.collections.size)
        val first = parsed.collections.first { it.id == 1L }
        assertEquals("Book", first.icon)
        assertEquals("#6366F1", first.color)
        val second = parsed.collections.first { it.id == 2L }
        assertEquals("Sports", second.icon)
        assertEquals("#10B981", second.color)
    }

    @Test
    fun `legacy backup without icon and color fields still parses`() {
        val legacyJson = """
            {
              "version": "1.0",
              "exportDate": "2026-11-15 00:00:00",
              "collections": [
                {
                  "id": 1,
                  "name": "Old Collection",
                  "description": "",
                  "createdAt": "2026-11-15 00:00:00",
                  "updatedAt": "2026-11-15 00:00:00"
                }
              ],
              "links": []
            }
        """.trimIndent()

        val parsed = service.parseBackupJson(legacyJson)

        assertEquals(1, parsed.collections.size)
        assertEquals("Folder", parsed.collections[0].icon)
        assertEquals("#6366F1", parsed.collections[0].color)
    }
}
