package com.appcodecraft.linkzary

import com.appcodecraft.linkzary.ui.component.TimestampOption
import com.appcodecraft.linkzary.ui.component.planCardFooter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v5.1 chip-crop fix v2 — pins the measured footer-allocation rules.
 *
 * Widths are MODELED dp values (default font scale), not live font metrics:
 * a half-width grid card on a 412dp screen is ~176dp wide; minus the card's
 * 2×16dp content padding the footer row has ~144dp. "Test Collection" in
 * labelSmall SemiBold + 2×10dp chip padding measures ≈103dp. The compose call
 * site feeds real TextMeasurer numbers into the same function.
 */
class FooterAllocationTest {

    private val halfCardFooter = 144f
    private val testCollectionChip = 103f
    private val dateOnly = TimestampOption("15 Nov", 34f)
    private val compact = TimestampOption("15h", 20f)

    /** THE acceptance case: "Test Collection" renders UNTRUNCATED in a
     *  half-width grid card at default font scale — timestamp hides, unread
     *  dot yields, the name wins the space. */
    @Test
    fun gridCard_testCollection_rendersUntruncated() {
        val plan = planCardFooter(
            availableWidthDp = halfCardFooter,
            chipNaturalWidthDp = testCollectionChip,
            timestampOptions = listOf(dateOnly, compact),
            allowYield = true
        )
        assertFalse(plan.chipTruncated)
        assertEquals(testCollectionChip, plan.chipWidthDp, 0.01f)
    }

    /** Grid, roomier card: date-only timestamp wins over compact (most
     *  informative first) and the dot stays. */
    @Test
    fun gridCard_widerCard_keepsDateOnlyTimestampAndDot() {
        val plan = planCardFooter(
            availableWidthDp = 200f,
            chipNaturalWidthDp = testCollectionChip,
            timestampOptions = listOf(dateOnly, compact),
            allowYield = true
        )
        assertFalse(plan.chipTruncated)
        assertEquals("15 Nov", plan.timestampLabel)
        assertTrue(plan.showUnreadDot)
    }

    /** Grid escalation ladder: date-only → compact → hidden, never a
     *  cropped name while any yield remains. */
    @Test
    fun gridCard_timestampEscalatesDownBeforeChipEverTruncates() {
        // 190dp: date-only (needs 197) fails; compact (needs 183) fits.
        val compactPlan = planCardFooter(
            availableWidthDp = 190f,
            chipNaturalWidthDp = testCollectionChip,
            timestampOptions = listOf(dateOnly, compact),
            allowYield = true
        )
        assertFalse(compactPlan.chipTruncated)
        assertEquals("15h", compactPlan.timestampLabel)
        assertTrue(compactPlan.showUnreadDot)

        // 160dp: even compact fails; the timestamp hides, the dot stays,
        // and the name is still whole.
        val hiddenPlan = planCardFooter(
            availableWidthDp = 160f,
            chipNaturalWidthDp = testCollectionChip,
            timestampOptions = listOf(dateOnly, compact),
            allowYield = true
        )
        assertFalse(hiddenPlan.chipTruncated)
        assertNull(hiddenPlan.timestampLabel)
        assertTrue(hiddenPlan.showUnreadDot)
    }

    /** List mode (rule #1): timestamp stays at natural width; the chip may
     *  ellipsize only when its natural width exceeds ALL remaining space
     *  after timestamp + 32dp kebab. */
    @Test
    fun listMode_chipEllipsizesOnlyWhenItExceedsAllRemainingSpace() {
        // Remaining after dot+gaps+kebab+timestamp = 144-12-16-32-20 = 64 < 103.
        val plan = planCardFooter(
            availableWidthDp = halfCardFooter,
            chipNaturalWidthDp = testCollectionChip,
            timestampOptions = listOf(compact),
            allowYield = false
        )
        assertTrue(plan.chipTruncated)
        assertEquals("15h", plan.timestampLabel)
        assertTrue(plan.showUnreadDot)
        assertEquals(64f, plan.chipWidthDp, 0.01f)

        // Same card with a short name: no ellipsis, timestamp untouched.
        val shortName = planCardFooter(
            availableWidthDp = halfCardFooter,
            chipNaturalWidthDp = 60f,
            timestampOptions = listOf(compact),
            allowYield = false
        )
        assertFalse(shortName.chipTruncated)
        assertEquals(60f, shortName.chipWidthDp, 0.01f)
    }

    /** Large font scale (1.3×): the name grows to ~134dp. After every yield
     *  it still can't fit whole in a half-width card, so — and only so — the
     *  chip truncates, with timestamp hidden and dot yielded. */
    @Test
    fun largeFontScale_truncatesOnlyAfterEveryYield() {
        val plan = planCardFooter(
            availableWidthDp = halfCardFooter,
            chipNaturalWidthDp = 134f,
            timestampOptions = listOf(dateOnly, compact),
            allowYield = true
        )
        assertTrue(plan.chipTruncated)
        assertNull(plan.timestampLabel)
        assertFalse(plan.showUnreadDot)
        assertEquals(104f, plan.chipWidthDp, 0.01f) // 144 - 8 gap - 32 kebab

        // Same scale in list mode: timestamp keeps its natural width, chip
        // takes the rest above the 48dp floor.
        val listPlan = planCardFooter(
            availableWidthDp = 400f,
            chipNaturalWidthDp = 134f,
            timestampOptions = listOf(compact),
            allowYield = false
        )
        assertFalse(listPlan.chipTruncated)
        assertEquals("15h", listPlan.timestampLabel)
    }

    /** Pathological narrow card: the 48dp floor keeps the chip tappable. */
    @Test
    fun tinyCard_chipNeverDropsBelowFloor() {
        val plan = planCardFooter(
            availableWidthDp = 60f,
            chipNaturalWidthDp = 103f,
            timestampOptions = listOf(compact),
            allowYield = true
        )
        assertTrue(plan.chipTruncated)
        assertEquals(48f, plan.chipWidthDp, 0.01f)
    }
}
