package com.strangerhelp.app.data.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
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
    }

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore.removeAll { existing -> cookies.any { it.name == existing.name } }
            cookieStore.addAll(cookies)
            // Persist
            val set = cookieStore.map { "${it.name}|${it.value}|${it.path}" }.toSet()
            prefs.edit().putStringSet("cookies", set).apply()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> = cookieStore
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Origin", "https://strangerhelp.com")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    val api: StrangerHelpApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StrangerHelpApi::class.java)

    fun clearSession() {
        cookieStore.clear()
        prefs.edit().remove("cookies").apply()
    }

    fun hasSession(): Boolean = cookieStore.any { it.name == "session" }
}
