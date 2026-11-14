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
            val uri = URI(withScheme)

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
     * True when [a] and [b] point at the same page after normalization.
     */
    fun areEquivalent(a: String, b: String): Boolean {
        val na = normalize(a) ?: return a == b
        val nb = normalize(b) ?: return a == b
        return na == nb
    }
}
