package com.strangerhelp.app.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    url: String,
    navController: NavController
) {
    val decodedUrl = URLDecoder.decode(url, "UTF-8")
    val title = when {
        decodedUrl.contains("terms") -> "Terms of Service"
        decodedUrl.contains("privacy") -> "Privacy Policy"
        decodedUrl.contains("cookies") -> "Cookie Policy"
        decodedUrl.contains("disclaimer") -> "Disclaimer"
        decodedUrl.contains("guidelines") -> "Community Guidelines"
        else -> "StrangerHelp"
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                    webViewClient = WebViewClient()
                    loadUrl(decodedUrl)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
