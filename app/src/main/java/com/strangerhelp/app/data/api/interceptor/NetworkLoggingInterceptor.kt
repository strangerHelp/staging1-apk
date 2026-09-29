package com.strangerhelp.app.data.api.interceptor

import com.strangerhelp.app.utils.AppLogger
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Comprehensive OkHttp Interceptor that logs detailed network request metadata,
 * timing, response status, headers, and response bodies without consuming the stream.
 */
class NetworkLoggingInterceptor(
    private val maxBodyPeekBytes: Long = 8192L // 8 KB safe preview
) : Interceptor {

    companion object {
        private const val TAG = "NetworkLogger"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method
        val startNs = System.nanoTime()

        // 1. Log Request Overview
        val requestHeadersSummary = formatHeaders(request.headers, isRequest = true)
        val requestBodySize = request.body?.contentLength()?.let { len ->
            if (len != -1L) "$len-byte" else "unknown-length"
        } ?: "no"

        AppLogger.d(TAG, "--> $method $url ($requestBodySize body)\nHeaders: $requestHeadersSummary")

        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
            if (e is java.net.UnknownHostException || e is java.net.ConnectException) {
                AppLogger.w(
                    TAG,
                    "<-- HTTP OFFLINE: $method $url (${e.javaClass.simpleName}: ${e.message}) after ${tookMs}ms"
                )
            } else {
                AppLogger.w(
                    TAG,
                    "<-- HTTP FAILED: $method $url (${e.javaClass.simpleName}: ${e.message}) after ${tookMs}ms"
                )
            }
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        val code = response.code
        val message = response.message
        val protocol = response.protocol
        val responseHeaders = formatHeaders(response.headers, isRequest = false)

        // 2. Safely peek response body without consuming stream
        val responseBody = response.body
        val bodyPreview = if (responseBody != null) {
            try {
                val peekSource = response.peekBody(maxBodyPeekBytes)
                val bodyStr = peekSource.string()
                if (bodyStr.length > 2000) {
                    bodyStr.take(2000) + "... [truncated, total ${bodyStr.length} chars]"
                } else {
                    bodyStr
                }
            } catch (e: Exception) {
                "[Unable to peek response body: ${e.message}]"
            }
        } else {
            "[No body]"
        }

        val logMessage = buildString {
            append("<-- $code $message $url (${tookMs}ms, protocol=$protocol)\n")
            append("Response Headers: $responseHeaders\n")
            append("Response Body: $bodyPreview")
        }

        // Differentiate log levels based on HTTP status
        when {
            code in 200..299 -> {
                AppLogger.d(TAG, logMessage)
            }
            code in 400..499 -> {
                AppLogger.w(TAG, "[Client Error $code]\n$logMessage")
            }
            code >= 500 -> {
                AppLogger.e(TAG, "[Server Error $code]\n$logMessage")
            }
            else -> {
                AppLogger.i(TAG, logMessage)
            }
        }

        return response
    }

    private fun formatHeaders(headers: Headers, isRequest: Boolean): String {
        if (headers.size == 0) return "{none}"
        val builder = StringBuilder("{")
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            val value = headers.value(i)
            if (i > 0) builder.append(", ")
            if (isRequest && (name.equals("Authorization", ignoreCase = true) || name.equals("Cookie", ignoreCase = true))) {
                // Sanitize sensitive token details but show existence
                val masked = if (value.contains(";")) {
                    val cookieNames = value.split(";").mapNotNull { it.substringBefore("=").trim().takeIf { s -> s.isNotEmpty() } }
                    "cookies present: [${cookieNames.joinToString(", ")}]"
                } else {
                    "***masked***"
                }
                builder.append("$name: $masked")
            } else {
                builder.append("$name: $value")
            }
        }
        builder.append("}")
        return builder.toString()
    }
}
