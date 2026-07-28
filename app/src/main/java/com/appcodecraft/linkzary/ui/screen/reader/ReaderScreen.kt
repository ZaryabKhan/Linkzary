package com.appcodecraft.linkzary.ui.screen.reader

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.appcodecraft.linkzary.data.entity.SavedLink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    link: SavedLink,
    onBackClick: () -> Unit
) {
    var fontSize by remember { mutableFloatStateOf(18f) }
    
    // Sepia-like background for reader mode
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val backgroundColor = if (!isDark) {
        Color(0xFFFBF0D9) 
    } else {
        MaterialTheme.colorScheme.background
    }
    
    val contentColor = if (!isDark) {
        Color(0xFF5F4B32)
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = link.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor
                )
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Font size slider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A",
                    fontSize = 14.sp,
                    color = contentColor
                )
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 14f..30f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
                Text(
                    text = "A",
                    fontSize = 24.sp,
                    color = contentColor
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (link.textContent.isNullOrBlank()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No offline content available for this link.",
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // We render HTML content using a simple Text composable for now, 
                    // or a WebView for better formatting. 
                    // Since specific HTML formatting was kept (h1, img etc), Text might be messy.
                    // WebView is safer for HTML.
                    
                    val css = """
                        <style>
                            body {
                                background-color: #${String.format("%06X", 0xFFFFFF and backgroundColor.toArgb())};
                                color: #${String.format("%06X", 0xFFFFFF and contentColor.toArgb())};
                                font-size: ${fontSize}px;
                                line-height: 1.6;
                                padding: 16px;
                                font-family: sans-serif;
                            }
                            img {
                                max-width: 100%;
                                height: auto;
                                border-radius: 8px;
                                margin: 16px 0;
                            }
                            h1, h2, h3 {
                                font-family: serif;
                                font-weight: bold;
                            }
                        </style>
                    """.trimIndent()
                    
                    val htmlContent = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                        $css
                        </head>
                        <body>
                        <h1>${link.title}</h1>
                        ${link.textContent}
                        </body>
                        </html>
                    """.trimIndent()

                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                webViewClient = WebViewClient()
                                settings.defaultTextEncodingName = "utf-8"
                                settings.setSupportZoom(false)
                                settings.builtInZoomControls = false
                                setBackgroundColor(0x00000000)
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ReaderScreenWrapper(
    linkId: Long,
    onBackClick: () -> Unit,
    viewModel: ReaderViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val link by viewModel.link.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    androidx.compose.runtime.LaunchedEffect(linkId) {
        viewModel.loadLink(linkId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    } else {
        link?.let { savedLink ->
            ReaderScreen(link = savedLink, onBackClick = onBackClick)
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Link not found")
            }
        }
    }
}
