package com.appcodecraft.linkzary.util

import java.net.URI
import java.net.URLDecoder

/**
 * Normalizes URLs so that duplicates are detected reliably.
 *
 * Users save the same page from different browsers/devices where the URL may
 * differ in scheme (http/https), a leading "www.", fragment (#section),
 * tracking query parameters, or a trailing slash. Saved-link detection used an
 * exact string match, which caused "already saved" links to be saved twice.
 */
object UrlNormalizer {

    /** Query parameters commonly used for tracking and safe to ignore. */
    private val IGNORED_QUERY_PARAMS = setOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "gclid", "fbclid", "mc_cid", "mc_eid", "igshid", "si", "ref", "ref_src"
    )

    /**
     * Returns a canonical form of [url] for duplicate comparison.
     * Returns null when [url] is blank or cannot be parsed.
     */
    fun normalize(url: String): String? {
        if (url.isBlank()) return null

        return try {
            // Treat scheme-less URLs as https so "example.com" matches "https://example.com"
            val withScheme = if (!url.contains("://")) "https://$url" else url
            // Share-sheet URLs often contain raw spaces or non-ASCII characters
            // that java.net.URI rejects outright; percent-encode them so such
            // URLs still normalize instead of falling back to exact matching.
            val uri = URI(encodeIllegalUriCharacters(withScheme))

            val host = uri.host?.lowercase()?.removePrefix("www.") ?: return null
            if (host.isEmpty()) return null

            val path = uri.path.orEmpty().trimEnd('/')
            val query = normalizedQuery(uri.rawQuery)

            buildString {
                append(host)
                if (path.isNotEmpty()) append(path)
                if (query.isNotEmpty()) append("?$query")
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Decodes, drops tracking params, sorts and rejoins query parameters. */
    private fun normalizedQuery(rawQuery: String?): String {
        if (rawQuery.isNullOrBlank()) return ""

        return rawQuery.split("&")
            .mapNotNull { pair ->
                val eq = pair.indexOf('=')
                if (eq == -1) return@mapNotNull null
                val name = decode(pair.substring(0, eq))
                val value = decode(pair.substring(eq + 1))
                if (name in IGNORED_QUERY_PARAMS) null else "$name=$value"
            }
            .sorted()
            .joinToString("&")
    }

    private fun decode(value: String): String = try {
        URLDecoder.decode(value, "UTF-8")
    } catch (e: Exception) {
        value
    }

    /**
     * Percent-encodes characters java.net.URI treats as illegal (raw spaces,
     * control chars, non-ASCII, one escape per UTF-8 byte). Already-valid
     * URLs are returned unchanged.
     */
    private fun encodeIllegalUriCharacters(url: String): String {
        // A '%' that doesn't start a valid percent-encoding breaks URI parsing.
        val escapedPercent = url.replace(Regex("%(?![0-9A-Fa-f]{2})"), "%25")
        return buildString {
            escapedPercent.forEach { c ->
                when {
                    c.code in 0x21..0x7E && c !in ILLEGAL_ASCII -> append(c)
                    else ->
                        // Multi-byte chars must be escaped per UTF-8 byte so
                        // "Übersicht" matches the pre-encoded "%C3%9Cbersicht".
                        c.toString().toByteArray(Charsets.UTF_8).forEach { b ->
                            append('%')
                            append((b.toInt() and 0xFF).toString(16).padStart(2, '0').uppercase())
                        }
                }
            }
        }
    }

    private val ILLEGAL_ASCII = setOf('"', '<', '>', '\\', '^', '`', '{', '}', '|')

    /**
     * True when [a] and [b] point at the same page after normalization.
     */
    fun areEquivalent(a: String, b: String): Boolean {
        val na = normalize(a) ?: return a == b
        val nb = normalize(b) ?: return a == b
        return na == nb
    }
}
