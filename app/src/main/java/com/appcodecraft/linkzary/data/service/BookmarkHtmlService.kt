package com.appcodecraft.linkzary.data.service

import android.content.Context
import android.net.Uri
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.util.UrlNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of parsing a browser bookmarks HTML file (Netscape format).
 */
data class HtmlImportResult(
    val links: List<SavedLink>,
    /** Folder name -> links found inside it, used to create collections. */
    val folders: Map<String, List<SavedLink>>,
    val skippedCount: Int
)

/**
 * Imports and exports bookmarks in the standard Netscape bookmark HTML format
 * used by Chrome, Firefox, Safari and Edge. This lets users bring bookmarks
 * from any browser into Linkzary and hand their links back out again.
 */
@Singleton
class BookmarkHtmlService @Inject constructor() {

    /**
     * Parses a bookmarks HTML file into links and per-folder link lists.
     * Links that duplicate an already-saved [existingLinks] URL are dropped.
     */
    suspend fun parseBookmarksHtml(
        context: Context,
        uri: Uri,
        existingLinks: List<SavedLink>
    ): HtmlImportResult = withContext(Dispatchers.IO) {
        val content = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw java.io.IOException("Cannot open file")
        parseBookmarksHtml(content, existingLinks)
    }

    /**
     * Pure parser: works on the raw file content so it is testable on the JVM.
     */
    fun parseBookmarksHtml(
        content: String,
        existingLinks: List<SavedLink>
    ): HtmlImportResult {
        val links = mutableListOf<SavedLink>()
        val folders = linkedMapOf<String, MutableList<SavedLink>>()
        var skipped = 0

        val anchorRegex = Regex(
            "<a\\s+[^>]*href\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>(.*?)</a>",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val h3Regex = Regex("<h3[^>]*>(.*?)</h3>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val dlCloseRegex = Regex("</dl>", RegexOption.IGNORE_CASE)
        val attrRegex = Regex("(add_date|tags)\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE)

        val existingNormalized = existingLinks.mapNotNull { UrlNormalizer.normalize(it.url) }.toSet()

        // Walk the file linearly: before each <a> tag, remember the most recent
        // <h3> as the folder for following links, until the folder's </dl> closes.
        var position = 0
        var currentFolder: String? = null

        while (position < content.length) {
            val anchorMatch = anchorRegex.find(content, position)
            val h3Match = h3Regex.find(content, position)
            val dlCloseMatch = dlCloseRegex.find(content, position)

            when {
                anchorMatch != null && (h3Match == null || anchorMatch.range.first < h3Match.range.first) && (dlCloseMatch == null || anchorMatch.range.first < dlCloseMatch.range.first) -> {
                    val attrs = attrRegex.findAll(anchorMatch.value.substringBefore(">"))
                        .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                    val href = anchorMatch.groupValues[1].trim()
                    val title = anchorMatch.groupValues[2]
                        .replace(Regex("<[^>]*>"), "")
                        .trim()
                        .ifBlank { href }

                    if (href.startsWith("http")) {
                        if (existingNormalized.contains(UrlNormalizer.normalize(href))) {
                            skipped++
                        } else {
                            val addDate = attrs["add_date"]?.toLongOrNull()?.let { Date(it * 1000) }
                            val link = SavedLink(
                                title = title.take(200),
                                url = href,
                                note = "",
                                tags = attrs["tags"]?.take(200) ?: "",
                                saveDate = addDate ?: Date()
                            )
                            links.add(link)
                            val folder = currentFolder
                            if (folder != null) {
                                folders.getOrPut(folder) { mutableListOf() }.add(link)
                            }
                        }
                    }
                    position = anchorMatch.range.last + 1
                }
                h3Match != null && (dlCloseMatch == null || h3Match.range.first < dlCloseMatch.range.first) -> {
                    currentFolder = h3Match.groupValues[1].trim()
                    position = h3Match.range.last + 1
                }
                dlCloseMatch != null -> {
                    // Leaving the folder: later links are unfiled
                    currentFolder = null
                    position = dlCloseMatch.range.last + 1
                }
                else -> position = content.length
            }
        }

        if (links.isEmpty()) {
            throw java.io.IOException("No bookmarks found in the HTML file")
        }

        return HtmlImportResult(links = links, folders = folders, skippedCount = skipped)
    }

    /**
     * Exports [links] grouped by [collections] as a Netscape bookmarks HTML
     * file that every browser can import.
     */
    suspend fun exportToHtml(
        context: Context,
        uri: Uri,
        links: List<SavedLink>,
        collections: List<Collection>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val html = buildBookmarksHtml(links, collections)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(html.toByteArray(Charsets.UTF_8))
                stream.flush()
            } ?: return@withContext false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Pure HTML builder so it is testable on the JVM.
     */
    fun buildBookmarksHtml(links: List<SavedLink>, collections: List<Collection>): String {
        val timestamp = System.currentTimeMillis() / 1000
        val sb = StringBuilder()
        sb.appendLine("<!DOCTYPE NETSCAPE-Bookmark-file-1>")
        sb.appendLine("<!-- This is an automatically generated file. It will be read and overwritten. DO NOT EDIT! -->")
        sb.appendLine("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">")
        sb.appendLine("<TITLE>Bookmarks</TITLE>")
        sb.appendLine("<H1>Bookmarks</H1>")
        sb.appendLine("<DL><p>")

        // Links with a collection go into a folder per collection; the rest at root.
        val uncategorized = links.filter { it.collectionId == null }
        for (collection in collections.filter { c -> links.any { it.collectionId == c.id } }) {
            sb.appendLine("    <DT><H3 ADD_DATE=\"$timestamp\" LAST_MODIFIED=\"$timestamp\">${escapeHtml(collection.name)}</H3>")
            sb.appendLine("    <DL><p>")
            links.filter { it.collectionId == collection.id }.forEach { link ->
                sb.appendLine("        ${bookmarkLine(link, indent = "    ")}")
            }
            sb.appendLine("    </DL><p>")
        }
        uncategorized.forEach { link ->
            sb.appendLine("    ${bookmarkLine(link, indent = "")}")
        }

        sb.appendLine("</DL><p>")
        return sb.toString()
    }

    private fun bookmarkLine(link: SavedLink, indent: String): String {
        val addDate = link.saveDate.time / 1000
        val tagAttr = if (link.tags.isNotBlank()) " TAGS=\"${escapeHtml(link.tags)}\"" else ""
        return "<DT><A HREF=\"${escapeHtml(link.url)}\" ADD_DATE=\"$addDate\"$tagAttr>${escapeHtml(link.title)}</A>"
    }

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
