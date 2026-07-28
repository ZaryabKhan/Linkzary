package com.appcodecraft.linkzary.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appcodecraft.linkzary.data.repository.CollectionRepository
import com.appcodecraft.linkzary.data.repository.LinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class StatsUiState(
    val totalLinks: Int = 0,
    val unreadCount: Int = 0,
    val readCount: Int = 0,
    val archivedCount: Int = 0,
    val pinnedCount: Int = 0,
    val offlineCount: Int = 0,
    val brokenLinksCount: Int = 0,
    val topDomains: List<DomainCount> = emptyList(),
    val topTags: List<TagCount> = emptyList(),
    val collectionCounts: List<CollectionCount> = emptyList(),
    val linksByMonth: List<MonthCount> = emptyList(),
    val avgReadingProgress: Float = 0f,
    val isLoading: Boolean = true
)

data class DomainCount(val domain: String, val count: Int)
data class TagCount(val tag: String, val count: Int)
data class CollectionCount(
    val collectionId: Long?,
    val collectionName: String,
    val count: Int,
    val color: String?
)
data class MonthCount(
    val monthLabel: String,
    val count: Int,
    val year: Int,
    val month: Int
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val linkRepository: LinkRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        linkRepository.getAllLinks(),
        collectionRepository.getAllCollections()
    ) { links, collections ->
        if (links.isEmpty() && collections.isEmpty()) {
            StatsUiState(isLoading = false)
        } else {
            val totalLinks = links.size
            
            val unreadCount = links.count { it.readStatus == "UNREAD" }
            val readCount = links.count { it.readStatus == "READ" }
            val archivedCount = links.count { it.readStatus == "ARCHIVED" }
            val pinnedCount = links.count { it.isPinned }
            val offlineCount = links.count { it.isOfflineAvailable }
            val brokenLinksCount = links.count { it.isAlive == false }
            
            val avgReadingProgress = if (totalLinks > 0) {
                links.map { it.readingProgress }.average().toFloat()
            } else {
                0f
            }
            
            val domainCounts = links
                .mapNotNull { extractDomain(it.url) }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedByDescending { it.second }
                .take(10)
                .map { DomainCount(it.first, it.second) }
            
            val tagCounts = links
                .flatMap { it.tags.split(",").map { tag -> tag.trim() }.filter { it.isNotEmpty() } }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedByDescending { it.second }
                .take(10)
                .map { TagCount(it.first, it.second) }
            
            val collectionMap = collections.associate { it.id to it }
            val collectionCounts = links
                .filter { it.collectionId != null }
                .groupBy { it.collectionId!! }
                .map { (id, linkList) ->
                    val collection = collectionMap[id]
                    CollectionCount(
                        collectionId = id,
                        collectionName = collection?.name ?: "Unknown",
                        count = linkList.size,
                        color = collection?.color
                    )
                }
                .sortedByDescending { it.count }
            
            val monthCounts = links
                .map { it.saveDate }
                .groupBy { date ->
                    val calendar = Calendar.getInstance().apply { time = date }
                    MonthCount(
                        monthLabel = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(date),
                        count = 0,
                        year = calendar.get(Calendar.YEAR),
                        month = calendar.get(Calendar.MONTH)
                    )
                }
                .map { (key, list) -> key.copy(count = list.size) }
                .sortedWith(compareByDescending<MonthCount> { it.year }.thenByDescending { it.month })
            
            StatsUiState(
                totalLinks = totalLinks,
                unreadCount = unreadCount,
                readCount = readCount,
                archivedCount = archivedCount,
                pinnedCount = pinnedCount,
                offlineCount = offlineCount,
                brokenLinksCount = brokenLinksCount,
                topDomains = domainCounts,
                topTags = tagCounts,
                collectionCounts = collectionCounts,
                linksByMonth = monthCounts,
                avgReadingProgress = avgReadingProgress,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(isLoading = true)
    )

    private fun extractDomain(url: String): String? {
        return try {
            val parsedUrl = URL(url)
            parsedUrl.host
        } catch (e: Exception) {
            null
        }
    }
}