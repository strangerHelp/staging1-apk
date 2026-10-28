package com.strangerhelp.app

import com.strangerhelp.app.navigation.DeepLink
import com.strangerhelp.app.navigation.DeepLinkHandler
import com.strangerhelp.app.service.NotificationChannels
import com.strangerhelp.app.util.NotificationBuilder
import org.junit.Assert.*
import org.junit.Test

class NotificationCrashTest {

    @Test
    fun testEmptyAndNullStringsInDeepLinkHandlerDoNotCrash() {
        // Blank inputs
        assertEquals(DeepLink.Home, DeepLinkHandler.parse(""))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("   "))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("/"))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("///"))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("????"))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("#anchor"))

        // Handler link trigger with empty string should safely no-op
        DeepLinkHandler.handleLink("")
        DeepLinkHandler.handleLink("   ")
    }

    @Test
    fun testMassiveOversizedStringsDoNotThrowOOMOrCrash() {
        val hugeString = "A".repeat(100_000)

        // Normalization should handle massive strings safely
        val normalized = NotificationBuilder.normalizeDeepLinkUrl(hugeString)
        assertTrue(normalized.startsWith("https://strangerhelp.com/"))

        // DeepLink parsing should handle huge strings safely
        val parsed = DeepLinkHandler.parse(hugeString)
        assertNotNull(parsed)
        assertEquals(DeepLink.Home, parsed)

        // Huge task ID parsing
        val hugeTaskId = "task_" + "9".repeat(5000)
        val taskParsed = DeepLinkHandler.parse("/tasks/$hugeTaskId")
        assertTrue(taskParsed is DeepLink.TaskDetail)
        assertEquals(hugeTaskId, (taskParsed as DeepLink.TaskDetail).taskId)
    }

    @Test
    fun testMalformedAndAdversarialDeepLinkInputs() {
        val adversarialLinks = listOf(
            "://??/\\///",
            "javascript:alert(1)",
            "/../../../../etc/passwd",
            "tasks?taskId=",
            "strangerhelp://///",
            "strangerhelp://",
            "data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==",
            "' OR '1'='1",
            "https://strangerhelp.com/tasks/%00%FF%FE",
            "https://strangerhelp.com/tasks/\"quoted_id\"",
            "https://strangerhelp.com/tasks/'single_quoted_id'",
            "https://strangerhelp.com/tasks/   spaced_id   "
        )

        for (link in adversarialLinks) {
            try {
                val parsed = DeepLinkHandler.parse(link)
                assertNotNull("Result for link '$link' must not be null", parsed)
            } catch (t: Throwable) {
                fail("Adversarial link '$link' caused unexpected crash: ${t.message}")
            }
        }
    }

    @Test
    fun testNotificationChannelsResolveUnknownAndCorruptedTypesDoNotCrash() {
        val weirdTypes = listOf(
            "",
            "   ",
            "null",
            "undefined",
            "12345",
            "!@#\$%^&*()",
            "CLAIM_REQUEST_UNKNOWN_SUBTYPE",
            "\u0000\uFFFF\uD83D\uDE00"
        )

        for (type in weirdTypes) {
            val channel = NotificationChannels.resolve(type)
            assertEquals(
                "Unknown or malformed type '$type' must safely default to strangerhelp_default",
                NotificationChannels.DEFAULT,
                channel
            )
        }
    }

    @Test
    fun testNotificationBuilderNormalizeUrlVariations() {
        assertEquals("https://strangerhelp.com/tasks", NotificationBuilder.normalizeDeepLinkUrl("/tasks"))
        assertEquals("https://strangerhelp.com/tasks", NotificationBuilder.normalizeDeepLinkUrl("tasks"))
        assertEquals("https://strangerhelp.com/", NotificationBuilder.normalizeDeepLinkUrl(""))
        assertEquals("https://strangerhelp.com/tasks", NotificationBuilder.normalizeDeepLinkUrl("https://strangerhelp.com/tasks"))
        assertEquals("http://localhost:3000/tasks", NotificationBuilder.normalizeDeepLinkUrl("http://localhost:3000/tasks"))
    }

    @Test
    fun testUnicodeAndEmojiSafeFormatting() {
        val emojiPayload = "🚀🎉⭐🔔❤️‍🔥"
        val parsed = DeepLinkHandler.parse("/tasks/$emojiPayload")
        assertTrue(parsed is DeepLink.TaskDetail)
        assertEquals(emojiPayload, (parsed as DeepLink.TaskDetail).taskId)
    }

    @Test
    fun testQuotedAndSpacedTaskIdsCleanedGracefully() {
        val quotedTask = DeepLinkHandler.parse("/tasks/\"8559f42316dd3a3f3d3080ba\"")
        assertTrue(quotedTask is DeepLink.TaskDetail)
        assertEquals("8559f42316dd3a3f3d3080ba", (quotedTask as DeepLink.TaskDetail).taskId)

        val spacedTask = DeepLinkHandler.parse("/tasks/   8559f42316dd3a3f3d3080ba   ")
        assertTrue(spacedTask is DeepLink.TaskDetail)
        assertEquals("8559f42316dd3a3f3d3080ba", (spacedTask as DeepLink.TaskDetail).taskId)
    }
}
