package com.appcodecraft.linkzary.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class UrlMetadata(
    val title: String,
    val favicon: String?,
    val previewImageUrl: String?
)

@Singleton
class UrlMetadataExtractor @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    suspend fun extractMetadata(url: String): UrlMetadata = withContext(Dispatchers.IO) {
        try {
            val connection = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .followRedirects(true)

            val doc = connection.get()

            val title = doc.title().takeIf { it.isNotBlank() } ?: extractDomainFromUrl(url)
            val favicon = extractFavicon(doc, url)
            val previewImage = extractPreviewImage(doc, url)

            UrlMetadata(title, favicon, previewImage)
        } catch (e: Exception) {
            // Fallback to domain name if extraction fails
            UrlMetadata(extractDomainFromUrl(url), null, null)
        }
    }

    private fun extractPreviewImage(doc: org.jsoup.nodes.Document, baseUrl: String): String? {
        return try {
            // Try Open Graph image first (most common)
            val ogImage = doc.select("meta[property=og:image]").first()?.attr("content")
            if (!ogImage.isNullOrBlank()) {
                return makeAbsoluteUrl(ogImage, baseUrl)
            }

            // Try secure OG image
            val ogImageSecure = doc.select("meta[property=og:image:secure_url]").first()?.attr("content")
            if (!ogImageSecure.isNullOrBlank()) {
                return makeAbsoluteUrl(ogImageSecure, baseUrl)
            }

            // Try Twitter card image
            val twitterImage = doc.select("meta[name=twitter:image]").first()?.attr("content")
            if (!twitterImage.isNullOrBlank()) {
                return makeAbsoluteUrl(twitterImage, baseUrl)
            }

            // Try generic meta image
            val metaImage = doc.select("meta[name=image]").first()?.attr("content")
            if (!metaImage.isNullOrBlank()) {
                return makeAbsoluteUrl(metaImage, baseUrl)
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    private fun makeAbsoluteUrl(imageUrl: String, baseUrl: String): String {
        return try {
            when {
                imageUrl.startsWith("https://") -> imageUrl
                imageUrl.startsWith("http://") -> imageUrl.replace("http://", "https://")
                imageUrl.startsWith("//") -> "https:$imageUrl"
                imageUrl.startsWith("data:") -> imageUrl
                else -> {
                    val url = URL(baseUrl)
                    val path = if (imageUrl.startsWith("/")) imageUrl else "/$imageUrl"
                    "https://${url.host}$path"
                }
            }
        } catch (e: Exception) {
            imageUrl
        }
    }

    private fun extractFavicon(doc: org.jsoup.nodes.Document, baseUrl: String): String? {
        return try {
            val faviconLink = doc.select("link[rel~=(?i)^(shortcut )?icon]").first()
            if (faviconLink != null) {
                val href = faviconLink.attr("href")
                when {
                    href.startsWith("http://") || href.startsWith("https://") -> href
                    href.startsWith("//") -> "https:$href"
                    else -> {
                        val url = URL(baseUrl)
                        val path = if (href.startsWith("/")) href else "/$href"
                        "https://${url.host}$path"
                    }
                }
            } else {
                val url = URL(baseUrl)
                "https://${url.host}/favicon.ico"
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