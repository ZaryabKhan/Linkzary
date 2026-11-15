package com.appcodecraft.linkzary.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.ui.theme.CookieShape

/**
 * The one FAB (spec v2 §2.3, audit D7): 64dp cookie-scallop on
 * primaryContainer at rest; morphs into a labeled pill (label + 250ms spring)
 * when [isScrolled]. Call sites differ only in label + action; screens add
 * 96dp bottom content padding so the FAB never covers card text.
 */
@Composable
fun LinkzaryFab(
    label: String,
    onClick: () -> Unit,
    isScrolled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .animateContentSize(animationSpec = spring(stiffness = 400f)),
        shape = CookieShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 6.dp,
        onClick = onClick
    ) {
        AnimatedContent(
            targetState = isScrolled,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "fab-morph"
        ) { scrolled ->
            if (scrolled) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Unspecified
                    )
                }
            } else {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = label,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
