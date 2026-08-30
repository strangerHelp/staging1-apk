package com.strangerhelp.app.ui.screens.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.strangerhelp.app.ui.theme.StrangerHelpTheme

class OAuthWebViewActivity : ComponentActivity() {
    private val viewModel: OAuthViewModel by viewModels()

    @SuppressLint("SetJavaScriptEnabled")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure cookies are clear for a fresh login if desired, but here we just rely on existing session logic
        
        setContent {
            StrangerHelpTheme {
                val oauthState by viewModel.oauthState.collectAsState()
                var webViewIsLoading by remember { mutableStateOf(true) }

                LaunchedEffect(oauthState) {
                    when (oauthState) {
                        is OAuthState.Success -> {
                            val userJson = Gson().toJson((oauthState as OAuthState.Success).user)
                            val intent = Intent().apply {
                                putExtra("userJson", userJson)
                            }
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                        is OAuthState.Error -> {
                            // Can show error or fallback
                        }
                        else -> {}
                    }
                }

                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        TopAppBar(
                            title = { Text("Google Sign In") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                if (webViewIsLoading || oauthState is OAuthState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp).padding(end = 16.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                            super.onPageStarted(view, url, favicon)
                                            webViewIsLoading = true
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            webViewIsLoading = false
                                            viewModel.processCookiesAndVerify(url)
                                        }
                                    }
                                    loadUrl("https://strangerhelp.com/api/auth/google")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
