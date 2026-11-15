package com.appcodecraft.linkzary.ui.component

/**
 * v5.1 chip-crop fix v2 — MEASURED footer allocation for link-card footers,
 * replacing the v4.1 weighted split (which starved both the collection chip
 * and the timestamp on half-width grid cards: "Te…", "15h a…").
 *
 * Pure Kotlin (no Android/compose imports) so the allocation rules are
 * unit-testable on the JVM. Widths are plain dp floats; the compose call site
 * measures real text with a [androidx.compose.ui.text.TextMeasurer] and feeds
 * the numbers in here.
 *
 * Rules (build order v5.1 #1):
 *  1. The chip may only ellipsize when its natural width (padding included)
 *     exceeds ALL remaining footer space after the timestamp at its natural
 *     width + the 32dp kebab.
 *  2. In grid (half-width cards, [allowYield] = true) the timestamp drops to
 *     the most informative date form that still lets the chip render whole —
 *     trying candidates most-informative-first — and if none fits, the
 *     timestamp hides and then the unread dot yields, so THE NAME WINS SPACE.
 *     The chip itself is the last thing ever truncated, and only after every
 *     yield is exhausted.
 */
data class TimestampOption(
    val label: String,
    /** Natural measured width of [label] in dp (single line, no ellipsis). */
    val widthDp: Float
)

data class CardFooterPlan(
    /** Width to give the chip (its padding included). */
    val chipWidthDp: Float,
    /** Chosen timestamp label, or null when hidden. */
    val timestampLabel: String?,
    val timestampWidthDp: Float,
    val showUnreadDot: Boolean,
    /** True when the chip had to ellipsize despite every yield. */
    val chipTruncated: Boolean
)

/**
 * Geometry: [dot 6dp + 6dp gap] [timestamp] [gap] [chip] [gap] [kebab].
 * So a visible timestamp costs 2 gaps, a hidden one still needs the
 * dot/chip and chip/kebab gaps.
 */
fun planCardFooter(
    availableWidthDp: Float,
    chipNaturalWidthDp: Float,
    /** Most informative first; the allocator walks down this list. */
    timestampOptions: List<TimestampOption>,
    kebabWidthDp: Float = 32f,
    /** One inter-element gap (8dp in the card). */
    gapDp: Float = 8f,
    /** Unread dot plus its trailing gap (6 + 6). */
    dotWidthDp: Float = 12f,
    /** v4.1 floor kept for the pathological narrow case. */
    minChipWidthDp: Float = 48f,
    /** Grid cards: the timestamp may hide and the dot may yield. */
    allowYield: Boolean = false
): CardFooterPlan {
    val chipNatural = chipNaturalWidthDp.coerceAtLeast(0f)

    fun planWithTimestamp(option: TimestampOption, dot: Boolean): CardFooterPlan =
        CardFooterPlan(
            chipWidthDp = chipNatural,
            timestampLabel = option.label,
            timestampWidthDp = option.widthDp,
            showUnreadDot = dot,
            chipTruncated = false
        )

    // Pass 1 — chip whole alongside the most informative timestamp that fits.
    val dotAndGaps = dotWidthDp + 2 * gapDp + kebabWidthDp
    for (option in timestampOptions) {
        if (chipNatural + option.widthDp + dotAndGaps <= availableWidthDp) {
            return planWithTimestamp(option, dot = true)
        }
    }

    if (allowYield) {
        // Pass 2 — timestamp hides, dot stays.
        if (chipNatural + dotWidthDp + gapDp + kebabWidthDp <= availableWidthDp) {
            return CardFooterPlan(chipNatural, null, 0f, showUnreadDot = true, chipTruncated = false)
        }
        // Pass 3 — the unread dot yields too; the name now owns the footer.
        val bareSpace = availableWidthDp - gapDp - kebabWidthDp
        if (chipNatural <= bareSpace) {
            return CardFooterPlan(chipNatural, null, 0f, showUnreadDot = false, chipTruncated = false)
        }
        // Everything yielded and it still can't fit — truncate the chip last.
        return CardFooterPlan(
            chipWidthDp = bareSpace.coerceAtLeast(minChipWidthDp),
            timestampLabel = null,
            timestampWidthDp = 0f,
            showUnreadDot = false,
            chipTruncated = true
        )
    }

    // List mode: timestamp stays at natural width (rule #1); the chip
    // ellipsizes into whatever remains — never below the floor.
    val fallback = timestampOptions.firstOrNull()
    val chipWidth = if (fallback == null) {
        (availableWidthDp - gapDp - kebabWidthDp).coerceAtLeast(minChipWidthDp)
    } else {
        (availableWidthDp - dotAndGaps - fallback.widthDp).coerceAtLeast(minChipWidthDp)
    }
    return CardFooterPlan(
        chipWidthDp = chipWidth,
        timestampLabel = fallback?.label,
        timestampWidthDp = fallback?.widthDp ?: 0f,
        showUnreadDot = true,
        chipTruncated = chipWidth < chipNatural
    )
}
