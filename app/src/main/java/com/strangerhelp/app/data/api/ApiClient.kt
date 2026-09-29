package com.strangerhelp.app.data.api

import android.content.Context
import android.content.SharedPreferences
import com.strangerhelp.app.data.api.interceptor.NetworkLoggingInterceptor
import com.strangerhelp.app.data.api.interceptor.RetryWithExponentialBackoffInterceptor
import com.strangerhelp.app.utils.AppLogger
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "ApiClient"
    private const val BASE_URL = "https://strangerhelp.com/"
    private const val PREFS_NAME = "strangerhelp_cookies"

    private lateinit var prefs: SharedPreferences
    private var cookieStore = mutableListOf<Cookie>()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Load saved cookies
        val saved = prefs.getStringSet("cookies", emptySet()) ?: emptySet()
        saved.forEach { raw ->
            val parts = raw.split("|")
            if (parts.size == 3) {
                val url = HttpUrl.Builder().scheme("https").host("strangerhelp.com").build()
                Cookie.parse(url, "${parts[0]}=${parts[1]}; Path=${parts[2]}; Secure; HttpOnly")?.let {
                    cookieStore.add(it)
                }
            }
        }
        AppLogger.d(TAG, "Initialized with ${cookieStore.size} cached session cookies.")
    }

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            if (cookies.isEmpty()) return
            cookieStore.removeAll { existing -> cookies.any { it.name == existing.name } }
            cookieStore.addAll(cookies)
            // Persist
            if (::prefs.isInitialized) {
                val set = cookieStore.map { "${it.name}|${it.value}|${it.path}" }.toSet()
                prefs.edit().putStringSet("cookies", set).apply()
                AppLogger.d(TAG, "Saved ${cookies.size} cookies from ${url.host}")
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore
    }

    /**
     * Enhanced OkHttpClient configured with:
     * 1. 30s connection, read, and write timeouts for stability on variable mobile networks
     * 2. HeaderInterceptor ensuring Origin, Referer, User-Agent, and Accept headers
     * 3. RetryWithExponentialBackoffInterceptor for automatic retry on network drops and 5xx/429 errors
     * 4. NetworkLoggingInterceptor for full request/response diagnostics, timing, and error body inspection
     * 5. HttpLoggingInterceptor for verbose low-level debug logs
     */
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .dns(FallbackDns())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .retryOnConnectionFailure(true)
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Origin", "https://strangerhelp.com")
                .header("Referer", "https://strangerhelp.com/")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) StrangerHelp-Android/1.0")
                .header("Accept", "application/json, text/plain, */*")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(
            RetryWithExponentialBackoffInterceptor(
                maxRetries = 3,
                initialDelayMs = 1000L,
                maxDelayMs = 8000L,
                backoffMultiplier = 2.0,
                jitterFactor = 0.2
            )
        )
        .addInterceptor(NetworkLoggingInterceptor(maxBodyPeekBytes = 8192L))
        .addInterceptor(HttpLoggingInterceptor { message ->
            AppLogger.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    /**
     * Custom lenient Gson converter preventing crashes from malformed JSON or type coercion mismatches
     */
    val gson = GsonFactory.createLenientGson()

    /**
     * Retrofit instance configured with custom OkHttpClient and Lenient GsonConverterFactory
     */
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: StrangerHelpApi = retrofit.create(StrangerHelpApi::class.java)

    fun clearSession() {
        cookieStore.clear()
        if (::prefs.isInitialized) {
            prefs.edit().remove("cookies").apply()
        }
        AppLogger.i(TAG, "Cleared session cookies.")
    }

    fun injectCookies(cookieString: String) {
        val url = HttpUrl.Builder().scheme("https").host("strangerhelp.com").build()
        val pairs = cookieString.split(";")
        for (pair in pairs) {
            val parts = pair.trim().split("=", limit = 2)
            if (parts.size == 2) {
                val name = parts[0].trim()
                val value = parts[1].trim()
                val cookie = Cookie.parse(url, "$name=$value; Path=/; Secure; HttpOnly")
                if (cookie != null) {
                    cookieStore.removeAll { it.name == cookie.name }
                    cookieStore.add(cookie)
                }
            }
        }
        if (::prefs.isInitialized) {
            val set = cookieStore.map { "${it.name}|${it.value}|${it.path}" }.toSet()
            prefs.edit().putStringSet("cookies", set).apply()
        }
        AppLogger.i(TAG, "Injected ${cookieStore.size} cookies.")
    }

    fun hasSession(): Boolean = cookieStore.any { it.name == "session" }
}
