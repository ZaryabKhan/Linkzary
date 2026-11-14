package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.ui.component.formatRelativeSaveDate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Tests for the card footer relative-time formatter (design audit #3).
 * Uses an explicit [now] so tests are timezone- and DST-independent.
 */
class RelativeTimeTest {

    private fun dateAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date {
        val cal = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        cal.set(year, month - 1, day, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun nowAt(hour: Int, minute: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    @Test
    fun `less than a minute ago is Just now`() {
        val now = nowAt(12, 30)
        assertEquals("Just now", formatRelativeSaveDate(Date(now.time - 30_000), now))
    }

    @Test
    fun `future timestamp is Just now`() {
        val now = nowAt(12, 30)
        assertEquals("Just now", formatRelativeSaveDate(Date(now.time + 120_000), now))
    }

    @Test
    fun `minutes under an hour use m ago`() {
        val now = nowAt(12, 30)
        assertEquals("5m ago", formatRelativeSaveDate(Date(now.time - 5 * 60_000), now))
        assertEquals("59m ago", formatRelativeSaveDate(Date(now.time - 59 * 60_000), now))
    }

    @Test
    fun `same calendar day uses h ago`() {
        val now = nowAt(23, 0)
        val saved = dateAt(now.year + 1900, now.month + 1, now.date, 21, 0)
        assertEquals("2h ago", formatRelativeSaveDate(saved, now))
    }

    @Test
    fun `previous calendar day is Yesterday even under 24h`() {
        val now = nowAt(1, 0)
        // Saved 23:00 yesterday: under 24h ago but a different calendar day
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DATE, -1)
        yesterday.set(Calendar.HOUR_OF_DAY, 23)
        yesterday.set(Calendar.MINUTE, 0)
        yesterday.set(Calendar.SECOND, 0)
        assertEquals("Yesterday", formatRelativeSaveDate(yesterday.time, now))
    }

    @Test
    fun `over 24h but same calendar day still uses h ago`() {
        val now = nowAt(20, 0)
        // Saved 00:30 today: over 24h? No matter - same day, hour-based label
        val saved = dateAt(now.year + 1900, now.month + 1, now.date, 0, 30)
        assertEquals("19h ago", formatRelativeSaveDate(saved, now))
    }

    @Test
    fun `two to six days use d ago`() {
        val now = dateAt(2026, 11, 14, 10, 0)
        assertEquals("2d ago", formatRelativeSaveDate(dateAt(2026, 11, 12, 10, 0), now))
        assertEquals("6d ago", formatRelativeSaveDate(dateAt(2026, 11, 8, 10, 0), now))
    }

    @Test
    fun `a week or older falls back to absolute date`() {
        val now = dateAt(2026, 11, 14, 10, 0)
        val formatted = formatRelativeSaveDate(dateAt(2026, 11, 7, 10, 0), now)
        assertEquals("7 Nov 2026", formatted)
    }

    @Test
    fun `day boundary at midnight does not shift a day`() {
        // Saved 22:00 on Nov 12, now 00:01 Nov 13 -> calendar-day diff of 1
        // (over an hour old, so the minute branch doesn't win)
        val now = dateAt(2026, 11, 13, 0, 1)
        assertEquals("Yesterday", formatRelativeSaveDate(dateAt(2026, 11, 12, 22, 0), now))
    }
}
