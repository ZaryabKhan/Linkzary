package com.appcodecraft.linkzary.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class UrlMetadata(
    val title: String,
    val favicon: String?
)

@Singleton
class UrlMetadataExtractor @Inject constructor() {

    suspend fun extractMetadata(url: String): UrlMetadata = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get()

            val title = doc.title().takeIf { it.isNotBlank() } ?: extractDomainFromUrl(url)
            val favicon = extractFavicon(doc, url)

            UrlMetadata(title, favicon)
        } catch (e: Exception) {
            // Fallback to domain name if extraction fails
            UrlMetadata(extractDomainFromUrl(url), null)
        }
    }

    private fun extractFavicon(doc: org.jsoup.nodes.Document, baseUrl: String): String? {
        return try {
            // Try to find favicon link
            val faviconLink = doc.select("link[rel~=(?i)^(shortcut )?icon]").first()
            if (faviconLink != null) {
                val href = faviconLink.attr("href")
                if (href.startsWith("http")) {
                    href
                } else {
                    val url = URL(baseUrl)
                    "${url.protocol}://${url.host}$href"
                }
            } else {
                // Fallback to default favicon location
                val url = URL(baseUrl)
                "${url.protocol}://${url.host}/favicon.ico"
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractDomainFromUrl(url: String): String {
        return try {
            URL(url).host.removePrefix("www.")
        } catch (e: Exception) {
            url
        }
    }
}