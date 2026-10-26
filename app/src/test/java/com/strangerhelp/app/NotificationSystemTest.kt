package com.strangerhelp.app

import android.net.Uri
import com.strangerhelp.app.navigation.DeepLink
import com.strangerhelp.app.navigation.DeepLinkHandler
import com.strangerhelp.app.service.NotificationChannels
import com.strangerhelp.app.ui.screens.notifications.PRESET_MOCK_PAYLOADS
import com.strangerhelp.app.ui.screens.notifications.extractConversationIdFromLink
import com.strangerhelp.app.ui.screens.notifications.extractTaskIdFromLink
import com.strangerhelp.app.ui.screens.notifications.getNotificationIcon
import com.strangerhelp.app.util.NotificationBuilder
import org.junit.Assert.*
import org.junit.Test

class NotificationSystemTest {

    @Test
    fun testAllNotificationChannelsDeclaredAndConfigured() {
        assertEquals("strangerhelp_default", NotificationChannels.DEFAULT)
        assertEquals("strangerhelp_tasks", NotificationChannels.TASKS)
        assertEquals("strangerhelp_messages", NotificationChannels.MESSAGES)
        assertEquals("strangerhelp_proof", NotificationChannels.PROOF)
        assertEquals("strangerhelp_promotions", NotificationChannels.PROMOTIONS)
    }

    @Test
    fun testNotificationChannelsResolveCorrectChannelForEveryPayloadType() {
        // 1. Task lifecycle & claim types -> TASKS channel
        val taskTypes = listOf(
            "claim_request",
            "claim_approved",
            "claim_rejected",
            "task_completed",
            "task_claimed",
            "claim_received",
            "CLAIM_REQUEST",
            "  claim_approved  "
        )
        for (type in taskTypes) {
            assertEquals(
                "Type '$type' must route to TASKS channel",
                NotificationChannels.TASKS,
                NotificationChannels.resolve(type)
            )
        }

        // 2. Chat & messaging types -> MESSAGES channel
        val messageTypes = listOf(
            "new_message",
            "chat_message",
            "message",
            "NEW_MESSAGE"
        )
        for (type in messageTypes) {
            assertEquals(
                "Type '$type' must route to MESSAGES channel",
                NotificationChannels.MESSAGES,
                NotificationChannels.resolve(type)
            )
        }

        // 3. Proof submission & review types -> PROOF channel
        val proofTypes = listOf(
            "proof_submitted",
            "proof_accepted",
            "proof_rejected",
            "review_received",
            "rating_received"
        )
        for (type in proofTypes) {
            assertEquals(
                "Type '$type' must route to PROOF channel",
                NotificationChannels.PROOF,
                NotificationChannels.resolve(type)
            )
        }

        // 4. Marketing & promotions -> PROMOTIONS channel
        val promoTypes = listOf("promotion", "promo", "tips", "PROMOTION")
        for (type in promoTypes) {
            assertEquals(
                "Type '$type' must route to PROMOTIONS channel",
                NotificationChannels.PROMOTIONS,
                NotificationChannels.resolve(type)
            )
        }

        // 5. Ask question answers -> TASKS channel
        val askTypes = listOf("ask_answer", "question_answered", "ask_reply")
        for (type in askTypes) {
            assertEquals(
                "Type '$type' must route to TASKS channel",
                NotificationChannels.TASKS,
                NotificationChannels.resolve(type)
            )
        }

        // 6. Unknown & fallback types -> DEFAULT channel
        val defaultTypes = listOf("general", "system_alert", "unknown_123", "", "   ")
        for (type in defaultTypes) {
            assertEquals(
                "Type '$type' must route to DEFAULT channel",
                NotificationChannels.DEFAULT,
                NotificationChannels.resolve(type)
            )
        }
    }

    @Test
    fun testDeepLinkHandlerParsingForAllRoutes() {
        // 1. Task Detail deep links
        val taskUris = listOf(
            "https://strangerhelp.com/tasks/8559f42316dd3a3f3d3080ba",
            "https://strangerhelp.com/task/8559f42316dd3a3f3d3080ba",
            "https://strangerhelp.com/task_detail/8559f42316dd3a3f3d3080ba",
            "https://strangerhelp.com/tasks/8559f42316dd3a3f3d3080ba?source=fcm",
            "strangerhelp://tasks/8559f42316dd3a3f3d3080ba",
            "/tasks/8559f42316dd3a3f3d3080ba"
        )
        for (raw in taskUris) {
            val parsed = DeepLinkHandler.parse(raw)
            assertTrue("Expected TaskDetail for '$raw', got $parsed", parsed is DeepLink.TaskDetail)
            assertEquals("8559f42316dd3a3f3d3080ba", (parsed as DeepLink.TaskDetail).taskId)
        }

        // 2. Tasks list deep links (browse tasks without ID)
        val tasksListUris = listOf(
            "https://strangerhelp.com/tasks",
            "https://strangerhelp.com/tasks/",
            "https://strangerhelp.com/tasks?category=Plumbing",
            "/tasks"
        )
        for (raw in tasksListUris) {
            val parsed = DeepLinkHandler.parse(raw)
            assertEquals("Expected TasksList for '$raw'", DeepLink.TasksList, parsed)
        }

        // 3. Chat Detail deep links
        val chatUris = listOf(
            "https://strangerhelp.com/chat/conv_abc_123",
            "https://strangerhelp.com/messages/conv_abc_123",
            "strangerhelp://chat/conv_abc_123",
            "/chat/conv_abc_123"
        )
        for (raw in chatUris) {
            val parsed = DeepLinkHandler.parse(raw)
            assertTrue("Expected ChatDetail for '$raw', got $parsed", parsed is DeepLink.ChatDetail)
            assertEquals("conv_abc_123", (parsed as DeepLink.ChatDetail).conversationId)
        }

        // 4. Chat list deep links
        val chatListUris = listOf(
            "https://strangerhelp.com/chat",
            "https://strangerhelp.com/messages",
            "/chat"
        )
        for (raw in chatListUris) {
            val parsed = DeepLinkHandler.parse(raw)
            assertEquals("Expected ChatList for '$raw'", DeepLink.ChatList, parsed)
        }

        // 5. Ask Community questions
        val parsedAskDetail = DeepLinkHandler.parse("https://strangerhelp.com/ask_detail/q_999")
        assertTrue(parsedAskDetail is DeepLink.AskDetail)
        assertEquals("q_999", (parsedAskDetail as DeepLink.AskDetail).questionId)

        assertEquals(DeepLink.AskList, DeepLinkHandler.parse("https://strangerhelp.com/ask"))

        // 6. User Profile deep links
        val parsedUser = DeepLinkHandler.parse("https://strangerhelp.com/user/usr_555")
        assertTrue(parsedUser is DeepLink.UserProfile)
        assertEquals("usr_555", (parsedUser as DeepLink.UserProfile).userId)

        // 7. Meet Detail deep links
        val parsedMeet = DeepLinkHandler.parse("https://strangerhelp.com/meets/meet_777")
        assertTrue(parsedMeet is DeepLink.MeetDetail)
        assertEquals("meet_777", (parsedMeet as DeepLink.MeetDetail).meetId)

        // 8. Notifications screen deep link
        assertEquals(DeepLink.Notifications, DeepLinkHandler.parse("https://strangerhelp.com/notifications"))

        // 9. Reset Password deep links
        val parsedReset = DeepLinkHandler.parse("https://strangerhelp.com/reset-password?token=tok_secret_999")
        assertTrue(parsedReset is DeepLink.ResetPassword)
        assertEquals("tok_secret_999", (parsedReset as DeepLink.ResetPassword).token)

        // 10. Verify Email deep links
        val parsedVerify = DeepLinkHandler.parse("https://strangerhelp.com/verify-email?token=tok_email_111")
        assertTrue(parsedVerify is DeepLink.VerifyEmail)
        assertEquals("tok_email_111", (parsedVerify as DeepLink.VerifyEmail).token)

        val parsedApiVerify = DeepLinkHandler.parse("https://strangerhelp.com/api/auth/verify-email?token=tok_email_222")
        assertTrue(parsedApiVerify is DeepLink.VerifyEmail)
        assertEquals("tok_email_222", (parsedApiVerify as DeepLink.VerifyEmail).token)

        // 11. Empty or Root URL -> DeepLink.Home
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("https://strangerhelp.com/"))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("https://strangerhelp.com"))
    }

    @Test
    fun testDeepLinkUrlNormalization() {
        assertEquals("https://strangerhelp.com/tasks/123", NotificationBuilder.normalizeDeepLinkUrl("/tasks/123"))
        assertEquals("https://strangerhelp.com/tasks/123", NotificationBuilder.normalizeDeepLinkUrl("tasks/123"))
        assertEquals("https://strangerhelp.com/chat/456", NotificationBuilder.normalizeDeepLinkUrl("https://strangerhelp.com/chat/456"))
        assertEquals("https://strangerhelp.com/profile", NotificationBuilder.normalizeDeepLinkUrl("/profile"))
    }

    @Test
    fun testPresetMockPayloadsIntegrity() {
        assertTrue("Preset mock payloads must not be empty", PRESET_MOCK_PAYLOADS.isNotEmpty())
        assertEquals(11, PRESET_MOCK_PAYLOADS.size)

        for (payload in PRESET_MOCK_PAYLOADS) {
            assertTrue("Payload ID must not be blank", payload.id.isNotBlank())
            assertTrue("Payload type must not be blank", payload.type.isNotBlank())
            assertTrue("Payload title must not be blank", payload.title.isNotBlank())
            assertTrue("Payload body must not be blank", payload.body.isNotBlank())
            assertTrue("Payload link must not be blank", payload.link.isNotBlank())
            assertTrue("Payload iconEmoji must not be blank", payload.iconEmoji.isNotBlank())
            assertTrue("Payload description must not be blank", payload.description.isNotBlank())

            // Expected channel must match NotificationChannels.resolve(payload.type)
            val resolvedChannel = NotificationChannels.resolve(payload.type)
            assertEquals(
                "Payload ${payload.id} (${payload.type}) expected channel mismatch",
                payload.expectedChannel,
                resolvedChannel
            )

            // Deep link must parse successfully
            val parsed = DeepLinkHandler.parse(payload.link)
            assertNotNull("Deep link '${payload.link}' must parse", parsed)
            assertNotEquals("Deep link '${payload.link}' should not fallback to Home", DeepLink.Home, parsed)
        }
    }

    @Test
    fun testExtractTaskIdFromLinkAndConversationId() {
        // Standard task links
        assertEquals("8559f42316dd3a3f3d3080ba", extractTaskIdFromLink("/tasks/8559f42316dd3a3f3d3080ba"))
        assertEquals("8559f42316dd3a3f3d3080ba", extractTaskIdFromLink("https://strangerhelp.com/tasks/8559f42316dd3a3f3d3080ba"))
        assertEquals("8559f42316dd3a3f3d3080ba", extractTaskIdFromLink("tasks/8559f42316dd3a3f3d3080ba"))

        // Query param
        assertEquals("xyz123", extractTaskIdFromLink("/tasks?taskId=xyz123"))

        // Invalid or non-task link
        assertNull(extractTaskIdFromLink("/chat/conv_123"))
        assertNull(extractTaskIdFromLink(""))

        // Conversation extraction
        assertEquals("conv_123", extractConversationIdFromLink("/chat/conv_123"))
        assertEquals("conv_456", extractConversationIdFromLink("https://strangerhelp.com/messages/conv_456"))
        assertNull(extractConversationIdFromLink("/tasks/8559f42316dd3a3f3d3080ba"))
        assertNull(extractConversationIdFromLink(""))
    }

    @Test
    fun testNotificationIconMappingForAllTypes() {
        assertEquals("💬", getNotificationIcon("new_message"))
        assertEquals("📋", getNotificationIcon("claim_request"))
        assertEquals("✅", getNotificationIcon("claim_approved"))
        assertEquals("❌", getNotificationIcon("claim_rejected"))
        assertEquals("📸", getNotificationIcon("proof_submitted"))
        assertEquals("✅", getNotificationIcon("proof_accepted"))
        assertEquals("❌", getNotificationIcon("proof_rejected"))
        assertEquals("🎉", getNotificationIcon("task_completed"))
        assertEquals("⭐", getNotificationIcon("review_received"))
        assertEquals("🔔", getNotificationIcon("promotion"))
        assertEquals("🔔", getNotificationIcon("unknown"))
    }
}
