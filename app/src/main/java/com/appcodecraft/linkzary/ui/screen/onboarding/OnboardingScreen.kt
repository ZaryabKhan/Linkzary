package com.appcodecraft.linkzary.ui.screen.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.R
import kotlinx.coroutines.launch

/**
 * Swipe-based onboarding (spec §7): five full-screen pages in a pager,
 * shown on first launch and once per new app version (the gate lives in
 * OnboardingGate + UserPreferencesManager). Persistent Skip top-right,
 * pill progress dots, 56dp Next pill bottom-right; page 5 is the quiet
 * donate page (outlined support button + "Maybe later").
 */
private const val PAGE_COUNT = 5

/** Accent reserved for "magic moments" (sparkles / sync arrows), spec §7. */
private val AccentCyan = androidx.compose.ui.graphics.Color(0xFF22D3EE)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    onSupport: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == PAGE_COUNT - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> OnboardingPage(
                    title = stringResource(R.string.onboarding_1_title),
                    body = stringResource(R.string.onboarding_1_body),
                    chips = listOf(
                        stringResource(R.string.onboarding_1_chip1),
                        stringResource(R.string.onboarding_1_chip2)
                    ),
                    illustration = { OnboardingIllustrationSave() }
                )
                1 -> OnboardingPage(
                    title = stringResource(R.string.onboarding_2_title),
                    body = stringResource(R.string.onboarding_2_body),
                    chips = listOf(
                        stringResource(R.string.onboarding_2_chip1),
                        stringResource(R.string.onboarding_2_chip2),
                        stringResource(R.string.onboarding_2_chip3)
                    ),
                    illustration = { OnboardingIllustrationCollections() }
                )
                2 -> OnboardingPage(
                    title = stringResource(R.string.onboarding_3_title),
                    body = stringResource(R.string.onboarding_3_body),
                    chips = listOf(
                        stringResource(R.string.onboarding_3_chip1),
                        stringResource(R.string.onboarding_3_chip2)
                    ),
                    illustration = { OnboardingIllustrationBackup() }
                )
                3 -> OnboardingPage(
                    title = stringResource(R.string.onboarding_4_title),
                    body = stringResource(R.string.onboarding_4_body),
                    chips = listOf(
                        stringResource(R.string.onboarding_4_chip1),
                        stringResource(R.string.onboarding_4_chip2)
                    ),
                    illustration = { OnboardingIllustrationReminders() }
                )
                else -> OnboardingSupportPage(onFinish = onFinish, onSupport = onSupport)
            }
        }

        // Persistent Skip, top-right (48dp touch target)
        TextButton(
            onClick = onFinish,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 8.dp)
        ) {
            Text(
                stringResource(R.string.onboarding_skip),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bottom bar: pill dots left/center, Next pill bottom-right
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OnboardingDots(
                currentPage = pagerState.currentPage,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                shape = androidx.compose.foundation.shape.CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.height(56.dp)
            ) {
                if (isLastPage) {
                    Text(
                        stringResource(R.string.onboarding_get_started),
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.onboarding_next)
                    )
                }
            }
        }
    }
}

/** One content page: illustration on primaryFixed tint, title, copy, chips. */
@Composable
private fun OnboardingPage(
    title: String,
    body: String,
    chips: List<String>,
    illustration: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.24f),
                    shape = MaterialTheme.shapes.extraLarge
                ),
            contentAlignment = Alignment.Center
        ) {
            illustration()
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            chips.forEach { chip ->
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = chip,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/** Page 5: the quiet support page — no nag, one outlined pill + a text link. */
@Composable
private fun OnboardingSupportPage(onFinish: () -> Unit, onSupport: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryFixed.copy(alpha = 0.24f),
                    shape = MaterialTheme.shapes.extraLarge
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.onboarding_5_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quiet copy: bodyMedium, onSurfaceVariant (spec §7 page 5)
        Text(
            text = stringResource(R.string.onboarding_5_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onSupport,
            shape = androidx.compose.foundation.shape.CircleShape,
            modifier = Modifier.height(48.dp)
        ) {
            Text(
                stringResource(R.string.onboarding_support),
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(onClick = onFinish) {
            Text(
                stringResource(R.string.onboarding_maybe_later),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Progress dots: active dot stretches into a 20dp pill with a spring. */
@Composable
private fun OnboardingDots(
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(PAGE_COUNT) { index ->
            val width by animateDpAsState(
                targetValue = if (index == currentPage) 20.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "dotWidth"
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .background(
                        color = if (index == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

// region Illustrations — flat geometric, brand indigo + cyan accent (spec §7)

@Composable
private fun IllustrationTile(
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    shape: androidx.compose.ui.graphics.Shape,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(72.dp),
        shape = shape,
        color = tint
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/** Page 1: link flying from a share tile into a bookmark tile. */
@Composable
private fun OnboardingIllustrationSave() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IllustrationTile(
            icon = Icons.Default.Share,
            tint = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        // accent "flying" dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AccentCyan, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        IllustrationTile(
            icon = Icons.Default.Bookmark,
            tint = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

/** Page 2: stacked tonal containers in varied shapes (mini shape party). */
@Composable
private fun OnboardingIllustrationCollections() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IllustrationTile(
            icon = Icons.Default.Folder,
            tint = MaterialTheme.colorScheme.primary,
            shape = com.appcodecraft.linkzary.ui.theme.CookieShape
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                        RoundedCornerShape(20.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        com.appcodecraft.linkzary.ui.theme.CloverShape
                    )
            )
        }
    }
}

/** Page 3: Drive backup — cloud tile with accent sync arrows. */
@Composable
private fun OnboardingIllustrationBackup() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IllustrationTile(
            icon = Icons.Default.CloudSync,
            tint = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(28.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        // accent sync arrows row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 6.dp)
                        .background(AccentCyan, androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

/** Page 4: reminders + merge — bell tile with a cyan sparkle. */
@Composable
private fun OnboardingIllustrationReminders() {
    Box(contentAlignment = Alignment.TopEnd) {
        IllustrationTile(
            icon = Icons.Default.Notifications,
            tint = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(24.dp)
        )
        // sparkle accent
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(AccentCyan, androidx.compose.foundation.shape.CircleShape)
        )
    }
}

// endregion
