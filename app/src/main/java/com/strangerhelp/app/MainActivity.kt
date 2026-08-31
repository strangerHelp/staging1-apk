package com.strangerhelp.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.User
import com.strangerhelp.app.navigation.AppNavigation
import com.strangerhelp.app.ui.screens.auth.LoginScreen
import com.strangerhelp.app.ui.screens.LandingScreen
import com.strangerhelp.app.ui.theme.StrangerHelpTheme
import com.strangerhelp.app.utils.AppLogger
import kotlinx.coroutines.launch
import coil.ImageLoader
import coil.decode.ImageSource
import coil.decode.DataSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayInputStream
import okio.buffer
import okio.source
import coil.Coil

class DataUriFetcher(
    private val data: Uri,
    private val options: Options
) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val base64 = data.toString().substringAfter("base64,")
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        val mimeType = data.toString().substringAfter("data:").substringBefore(";")

        return SourceResult(
            source = ImageSource(
                ByteArrayInputStream(bytes).source().buffer(),
                options.context
            ),
            mimeType = mimeType,
            dataSource = coil.decode.DataSource.MEMORY
        )
    }

    class Factory : Fetcher.Factory<Uri> {
        override fun create(
            data: Uri,
            options: Options,
            loader: ImageLoader
        ): Fetcher? {
            return if (data.scheme == "data") {
                DataUriFetcher(data, options)
            } else null
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(DataUriFetcher.Factory())
            }
            .build()
        Coil.setImageLoader(imageLoader)

        try {
            ApiClient.init(this)
        } catch (e: Exception) {
            AppLogger.e("MainActivity", "Failed to initialize ApiClient during app launch", e)
        }
        
        setContent {
            StrangerHelpTheme {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var currentUser by remember { mutableStateOf<User?>(null) }
                    var isLoading by remember { mutableStateOf(true) }
                    val scope = rememberCoroutineScope() // Note: globalExceptionHandler could be added if passing context was easier in compose without breaking structure

                    // Check if already logged in
                    LaunchedEffect(Unit) {
                        try {
                            val res = ApiClient.api.getMe()
                            if (res.isSuccessful) currentUser = res.body()?.user
                        } catch (_: Exception) {}
                        isLoading = false
                    }

                    when {
                        isLoading -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        currentUser == null -> {
                            com.strangerhelp.app.navigation.AuthNavigation(
                                onLoginSuccess = { user ->
                                    currentUser = user
                                }
                            )
                        }
                        else -> {
                            AppNavigation(
                                user = currentUser!!,
                                onLogout = {
                                    scope.launch {
                                        try { ApiClient.api.logout() } catch (_: Exception) {}
                                        ApiClient.clearSession()
                                        currentUser = null
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
