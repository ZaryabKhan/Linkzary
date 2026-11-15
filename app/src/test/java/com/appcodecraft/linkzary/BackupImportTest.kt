package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.data.service.ImportExportService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * v5.1.1 crash-fix regression: real-world backups (100+ links) crashed the
 * import preview. This file pins the parsing layer — the exact shape the
 * user's backup uses: space-separated 'yyyy-MM-dd HH:mm:ss' timestamps,
 * collections WITHOUT the newer icon/color fields, and null previewImageUrl
 * on every link. Synthetic data only; never commit a user file.
 */
class BackupImportTest {

    private val service = ImportExportService()

    /** Backup JSON in the observed legacy shape (7 collections, 120 links). */
    private fun legacyBackup(links: Int = 120): String {
        val sb = StringBuilder()
        sb.append("""{"version":"1.0","exportDate":"2026-11-15 17:00:00","collections":[""")
        val collections = listOf("AI & Research", "Design", "Development", "Learning", "Media", "Productivity", "Reading")
        sb.append(collections.indices.joinToString(",") { i ->
            """{"id":${i + 1},"name":"${collections[i]}","description":"Dummy ${collections[i]} websites","createdAt":"2026-11-15 17:00:00","updatedAt":"2026-11-15 17:00:00"}"""
        })
        sb.append("""],"links":[""")
        sb.append((0 until links).joinToString(",") { i ->
            """{"id":${i + 1},"url":"https://example$i.test/page$i","title":"Example Site $i","description":null,"collectionId":${(i % 7) + 1},"collectionName":"${collections[i % 7]}","isPinned":false,"createdAt":"2026-11-15 17:00:00","updatedAt":"2026-11-15 17:00:00","faviconUrl":null,"previewImageUrl":null}"""
        })
        sb.append("]}")
        return sb.toString()
    }

    @Test
    fun `legacy backup with 100+ links parses with defaults applied`() {
        val data = service.parseBackupJson(legacyBackup())
        assertEquals(7, data.collections.size)
        assertEquals(120, data.links.size)
        // Collections had no icon/color in this shape — defaults must fill in.
        data.collections.forEach {
            assertEquals("Folder", it.icon)
            assertEquals("#6366F1", it.color)
        }
        // Null favicon/preview must survive as null, not throw.
        assertTrue(data.links.all { it.previewImageUrl == null && it.faviconUrl == null })
        // Space-separated timestamps parse instead of falling back to now().
        val parsed = service.parseExportDate("2026-11-15 17:00:00")
        assertEquals(2026 - 1900, parsed.year)
        assertEquals(10, parsed.month) // 0-based November
        assertEquals(15, parsed.date)
    }

    @Test
    fun `parseExportDate falls back without throwing on garbage`() {
        // Must not throw; returns the fallback "now" date.
        val fallback = service.parseExportDate("not a date at all")
        assertTrue(!fallback.before(Date(0)))
    }

    @Test
    fun `backup json round trip`() {
        val collections = listOf(
            Collection(id = 1, name = "Reading List", color = "#10B981", icon = "Book", createdDate = Date()),
            Collection(id = 2, name = "Sports", color = "#EF4444", icon = "Sports", createdDate = Date())
        )
        val links = (0 until 60).map { i ->
            SavedLink(
                id = i.toLong(),
                url = "https://roundtrip.test/$i",
                title = "Round Trip $i",
                note = "",
                collectionId = if (i % 2 == 0) 1L else 2L,
                saveDate = Date()
            )
        }
        val json = service.buildBackupJson(links, collections)
        val restored = service.parseBackupJson(json)
        assertEquals(2, restored.collections.size)
        assertEquals(60, restored.links.size)
        assertEquals("#10B981", restored.collections[0].color)
        assertEquals("Book", restored.collections[0].icon)
        assertEquals("https://roundtrip.test/0", restored.links[0].url)
    }
}
