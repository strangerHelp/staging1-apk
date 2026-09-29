package com.strangerhelp.app.data.api.interceptor

import com.strangerhelp.app.utils.AppLogger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * OkHttp Interceptor that automatically retries failed API requests using exponential backoff
 * and jitter. Retries on transient I/O exceptions (e.g. socket timeouts, connection drops)
 * and retryable HTTP server errors (429, 500, 502, 503, 504).
 */
class RetryWithExponentialBackoffInterceptor(
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 1000L,
    private val maxDelayMs: Long = 10000L,
    private val backoffMultiplier: Double = 2.0,
    private val jitterFactor: Double = 0.2
) : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
        private val RETRYABLE_STATUS_CODES = setOf(429, 500, 502, 503, 504)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var lastException: IOException? = null

        // Check if request body cannot be retried (one-shot stream)
        val isOneShot = request.body?.isOneShot() == true

        while (true) {
            try {
                val response = chain.proceed(request)

                // If response was successful or client-side error (not retryable), or if request is one-shot
                if (response.isSuccessful || !RETRYABLE_STATUS_CODES.contains(response.code) || isOneShot || attempt >= maxRetries) {
                    if (!response.isSuccessful && RETRYABLE_STATUS_CODES.contains(response.code) && attempt >= maxRetries) {
                        AppLogger.w(TAG, "Exhausted all $maxRetries retries for ${request.method} ${request.url} (Final Status: ${response.code})")
                    }
                    return response
                }

                // If we got a retryable status code and have retries remaining:
                attempt++
                val retryAfterHeader = response.header("Retry-After")
                val delayMs = calculateDelay(attempt, retryAfterHeader)

                AppLogger.w(
                    TAG,
                    "Retryable HTTP ${response.code} for ${request.method} ${request.url}. " +
                            "Attempt $attempt of $maxRetries. Backing off for ${delayMs}ms before retry..."
                )

                // Important: close the response body to avoid leaking socket connections
                response.close()

                try {
                    Thread.sleep(delayMs)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Interrupted during exponential backoff retry", ie)
                }

            } catch (e: IOException) {
                lastException = e

                val isDnsFailure = e is java.net.UnknownHostException
                if (isOneShot || isDnsFailure || attempt >= maxRetries) {
                    if (isDnsFailure) {
                        AppLogger.w(
                            TAG,
                            "Host unresolvable (offline) for ${request.method} ${request.url}: ${e.message}"
                        )
                    } else {
                        AppLogger.w(
                            TAG,
                            "Request failed after $attempt retries for ${request.method} ${request.url}: ${e.message}"
                        )
                    }
                    throw e
                }

                attempt++
                val delayMs = calculateDelay(attempt, null)

                AppLogger.w(
                    TAG,
                    "I/O failure (${e.javaClass.simpleName}: ${e.message}) on ${request.method} ${request.url}. " +
                            "Attempt $attempt of $maxRetries. Backing off for ${delayMs}ms before retry..."
                )

                try {
                    Thread.sleep(delayMs)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw IOException("Interrupted during exponential backoff retry", ie)
                }
            }
        }
    }

    private fun calculateDelay(attempt: Int, retryAfterHeader: String?): Long {
        // If server specified Retry-After in seconds
        if (!retryAfterHeader.isNullOrBlank()) {
            val seconds = retryAfterHeader.trim().toLongOrNull()
            if (seconds != null && seconds > 0) {
                val headerMs = TimeUnit.SECONDS.toMillis(seconds)
                return min(headerMs, maxDelayMs)
            }
        }

        // Exponential backoff: initialDelayMs * (multiplier ^ (attempt - 1))
        val exp = backoffMultiplier.pow((attempt - 1).toDouble())
        val calculatedDelay = (initialDelayMs * exp).toLong()
        val cappedDelay = min(calculatedDelay, maxDelayMs)

        // Apply random jitter (+/- jitterFactor)
        val jitterRange = (cappedDelay * jitterFactor).toLong().coerceAtLeast(1L)
        val jitter = if (jitterRange > 0) Random.nextLong(-jitterRange, jitterRange + 1) else 0L
        val minDelay = min(10L, maxDelayMs)
        val finalDelay = (cappedDelay + jitter).coerceIn(minDelay, maxDelayMs)

        return finalDelay
    }
}
