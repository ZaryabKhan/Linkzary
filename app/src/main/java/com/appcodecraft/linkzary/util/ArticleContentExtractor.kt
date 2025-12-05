package com.appcodecraft.linkzary.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleContentExtractor @Inject constructor() {

    suspend fun extractContent(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(10000)
                .get()

            // Remove unwanted elements
            removeUnwantedElements(doc)

            // Try to find the main content
            val article = findArticleContent(doc)

            if (article != null) {
                // Clean and format the content
                cleanContent(article)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun removeUnwantedElements(doc: Document) {
        val unwantedSelectors = listOf(
            "script", "style", "noscript", "iframe", "header", "footer", "nav", "aside",
            ".ad", ".ads", ".advertisement", ".social-share", ".comments", ".related-posts",
            ".cookie-consent", ".newsletter-signup"
        )
        for (selector in unwantedSelectors) {
            doc.select(selector).remove()
        }
    }

    private fun findArticleContent(doc: Document): Element? {
        // Strategy 1: Look for <article> tag
        val articleTag = doc.select("article").first()
        if (articleTag != null && articleTag.text().length > 200) {
            return articleTag
        }

        // Strategy 2: Look for common main content containers
        val potentialContainers = listOf(
            "main",
            "#content", ".content",
            "#main", ".main",
            "#article", ".article",
            ".post-content", ".entry-content"
        )

        for (selector in potentialContainers) {
            val element = doc.select(selector).first()
            if (element != null && element.text().length > 200) {
                return element
            }
        }

        // Strategy 3: Find the block element with the most text
        return doc.body().allElements
            .filter { it.tagName() in listOf("div", "section") }
            .maxByOrNull { it.text().length }
            ?.takeIf { it.text().length > 200 }
    }

    private fun cleanContent(element: Element): String {
        // Use standard whitelist to allow basic formatting but strip everything else
        val cleanHtml = Jsoup.clean(
            element.html(),
            Safelist.basic()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6", "img", "figure", "figcaption")
                .addAttributes("img", "src", "alt")
        )
        return cleanHtml
    }
}
