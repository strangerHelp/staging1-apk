package com.strangerhelp.app

import com.google.gson.annotations.SerializedName
import com.strangerhelp.app.data.api.GsonFactory
import com.strangerhelp.app.data.api.interceptor.NetworkLoggingInterceptor
import com.strangerhelp.app.data.api.interceptor.RetryWithExponentialBackoffInterceptor
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class NetworkLayerTest {

    data class SampleTaskDto(
        val id: String = "",
        val budget: Int = 0,
        val urgent: Int = 0,
        val verified: Boolean = false,
        val distance: Double? = null
    )

    @Test
    fun testLenientGsonConvertsStringNumbersAndBooleans() {
        val gson = GsonFactory.createLenientGson()

        // JSON where budget is a string, urgent is a string, verified is "1", distance is string
        val json = """
            {
                "id": "t1",
                "budget": "2500",
                "urgent": "1",
                "verified": "true",
                "distance": "14.75"
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, SampleTaskDto::class.java)
        assertEquals("t1", parsed.id)
        assertEquals(2500, parsed.budget)
        assertEquals(1, parsed.urgent)
        assertTrue(parsed.verified)
        assertEquals(14.75, parsed.distance ?: 0.0, 0.001)
    }

    @Test
    fun testRealTaskDetailJsonParsing() {
        val file = java.io.File("/app/applet/test_task.json").takeIf { it.exists() }
            ?: java.io.File("test_task.json")
        val json = file.readText()
        val gson = GsonFactory.createLenientGson()
        val task = gson.fromJson(json, com.strangerhelp.app.data.model.Task::class.java)
        assertEquals("35bb8c64a66c29a366634b56", task._id)
        assertEquals("Get task done", task.title)
    }

    @Test
    fun testLenientGsonHandlesNullsAndEmptyStringsGracefully() {
        val gson = GsonFactory.createLenientGson()

        val json = """
            {
                "id": "t2",
                "budget": "",
                "urgent": null,
                "verified": 0,
                "distance": "null"
            }
        """.trimIndent()

        val parsed = gson.fromJson(json, SampleTaskDto::class.java)
        assertEquals("t2", parsed.id)
        assertEquals(0, parsed.budget)
        assertEquals(0, parsed.urgent)
        assertFalse(parsed.verified)
        assertNull(parsed.distance)
    }

    @Test
    fun testRetryInterceptorRetriesOn502AndSucceeds() {
        val attempts = AtomicInteger(0)
        val interceptor = RetryWithExponentialBackoffInterceptor(
            maxRetries = 2,
            initialDelayMs = 10L, // Fast for unit tests
            maxDelayMs = 50L
        )

        val fakeChain = object : FakeChain(
            Request.Builder().url("https://strangerhelp.com/api/tasks").build()
        ) {
            override fun proceed(request: Request): Response {
                val count = attempts.incrementAndGet()
                return if (count == 1) {
                    // Fail first attempt with 502 Bad Gateway
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(502)
                        .message("Bad Gateway")
                        .body("Bad Gateway".toResponseBody("text/plain".toMediaType()))
                        .build()
                } else {
                    // Succeed second attempt with 200 OK
                    Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("[{\"_id\":\"123\"}]".toResponseBody("application/json".toMediaType()))
                        .build()
                }
            }
        }

        val response = interceptor.intercept(fakeChain)
        assertEquals(200, response.code)
        assertEquals(2, attempts.get())
        assertEquals("[{\"_id\":\"123\"}]", response.body?.string())
    }

    @Test
    fun testRetryInterceptorDoesNotRetryClientErrors() {
        val attempts = AtomicInteger(0)
        val interceptor = RetryWithExponentialBackoffInterceptor(
            maxRetries = 3,
            initialDelayMs = 10L,
            maxDelayMs = 50L
        )

        val fakeChain = object : FakeChain(
            Request.Builder().url("https://strangerhelp.com/api/tasks/not_found").build()
        ) {
            override fun proceed(request: Request): Response {
                attempts.incrementAndGet()
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(404)
                    .message("Not Found")
                    .body("{\"error\":\"Not found\"}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }

        val response = interceptor.intercept(fakeChain)
        assertEquals(404, response.code)
        assertEquals(1, attempts.get()) // No retry on 404
    }

    @Test
    fun testRetryInterceptorRetriesOnIOException() {
        val attempts = AtomicInteger(0)
        val interceptor = RetryWithExponentialBackoffInterceptor(
            maxRetries = 2,
            initialDelayMs = 10L,
            maxDelayMs = 50L
        )

        val fakeChain = object : FakeChain(
            Request.Builder().url("https://strangerhelp.com/api/tasks").build()
        ) {
            override fun proceed(request: Request): Response {
                val count = attempts.incrementAndGet()
                if (count == 1) {
                    throw IOException("Connection reset by peer")
                }
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("[]".toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }

        val response = interceptor.intercept(fakeChain)
        assertEquals(200, response.code)
        assertEquals(2, attempts.get())
    }

    @Test
    fun testRetryInterceptorDoesNotRetryUnknownHostException() {
        val attempts = AtomicInteger(0)
        val interceptor = RetryWithExponentialBackoffInterceptor(
            maxRetries = 3,
            initialDelayMs = 10L,
            maxDelayMs = 50L
        )

        val fakeChain = object : FakeChain(
            Request.Builder().url("https://strangerhelp.com/api/tasks/test").build()
        ) {
            override fun proceed(request: Request): Response {
                attempts.incrementAndGet()
                throw java.net.UnknownHostException("Unable to resolve host strangerhelp.com")
            }
        }

        try {
            interceptor.intercept(fakeChain)
            fail("Expected UnknownHostException")
        } catch (e: java.net.UnknownHostException) {
            // Expected
        }
        assertEquals(1, attempts.get()) // Exactly 1 attempt, no endless retries on DNS failure
    }

    @Test
    fun testNetworkLoggingInterceptorPreservesResponseBody() {
        val loggingInterceptor = NetworkLoggingInterceptor(maxBodyPeekBytes = 1024L)
        val bodyContent = "[{\"_id\":\"task_abc\",\"title\":\"Test Task\"}]"

        val fakeChain = object : FakeChain(
            Request.Builder().url("https://strangerhelp.com/api/tasks").build()
        ) {
            override fun proceed(request: Request): Response {
                return Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .header("Content-Type", "application/json")
                    .body(bodyContent.toResponseBody("application/json".toMediaType()))
                    .build()
            }
        }

        val response = loggingInterceptor.intercept(fakeChain)
        assertEquals(200, response.code)
        // Ensure body wasn't consumed and can still be read downstream by Retrofit
        val readString = response.body?.string()
        assertEquals(bodyContent, readString)
    }

    @Test
    fun testFallbackDnsResolvesStrangerHelp() {
        val dns = com.strangerhelp.app.data.api.FallbackDns()
        val addresses = dns.lookup("strangerhelp.com")
        assertNotNull(addresses)
        assertTrue(addresses.isNotEmpty())
        assertTrue(addresses.any { it.hostAddress == "104.21.23.236" || it.hostAddress == "172.67.214.40" })
    }

    open class FakeChain(private val currentRequest: Request) : Interceptor.Chain {
        override fun request(): Request = currentRequest
        override fun proceed(request: Request): Response = throw UnsupportedOperationException()
        override fun connection(): Connection? = null
        override fun call(): Call = throw UnsupportedOperationException()
        override fun connectTimeoutMillis(): Int = 1000
        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        override fun readTimeoutMillis(): Int = 1000
        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
        override fun writeTimeoutMillis(): Int = 1000
        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    }
}
