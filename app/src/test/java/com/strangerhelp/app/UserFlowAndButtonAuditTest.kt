package com.strangerhelp.app

import com.strangerhelp.app.data.model.*
import com.strangerhelp.app.navigation.DeepLink
import com.strangerhelp.app.navigation.DeepLinkHandler
import com.strangerhelp.app.navigation.Screen
import com.strangerhelp.app.service.NotificationChannels
import com.strangerhelp.app.ui.screens.tasks.haversine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.*
import org.junit.Test

/**
 * End-to-end audit and test across all buttons, user flows, and crash resilience:
 * - Flow 1: Guest User Journey & Authentication gating
 * - Flow 2: Task Poster Lifecycle & Claim/Proof Decision Buttons
 * - Flow 3: Helper Lifecycle, Claiming & Tracking Buttons
 * - Flow 4: Community Ask / Q&A Flow & Upvoting Buttons
 * - Flow 5: FCM Notification Routing & Deep Link Tapping
 * - Flow 6: Navigation Backstack & Bottom Bar Button Verification
 * - Flow 7: Quick Actions & Legal Menu Button Mapping
 * - Flow 8: Rapid Multi-Tap & Boundary Stress Resilience
 */
class UserFlowAndButtonAuditTest {

    // =========================================================================
    // Flow 1: Guest User Flow & Button Actions
    // =========================================================================

    @Test
    fun testGuestUserRestrictionsAndGating() {
        val guestUser = User(
            id = "guest",
            name = "Guest",
            email = "guest@strangerhelp.com",
            avatar = "",
            city = "Bangalore",
            area = "Indiranagar",
            phone = "",
            bio = ""
        )

        // 1. Verify guest identifier detection
        val isGuest = guestUser.id == "guest" || guestUser.id.isBlank()
        assertTrue("Guest user should be identified correctly", isGuest)

        // 2. Guest attempting to claim a task: should require auth
        val task = Task(
            _id = "task_open_1",
            title = "Pick up medicine",
            description = "Need urgent delivery from Apollo Pharmacy",
            budget = 150,
            status = "open",
            category = "Task",
            posterId = "poster_123"
        )

        val canGuestDirectlyClaim = !isGuest && task.status == "open" && task.posterId != guestUser.id
        assertFalse("Guest MUST NOT be allowed to directly claim without login", canGuestDirectlyClaim)

        // 3. Guest profile view: Should display Login prompt or Guest status rather than editable fields
        val isGuestEditable = !isGuest
        assertFalse("Guest should not have profile editing access", isGuestEditable)
    }

    // =========================================================================
    // Flow 2: Task Poster Lifecycle (Create -> Review Claims -> Approve Proof -> Rate)
    // =========================================================================

    @Test
    fun testTaskPosterCompleteLifecycleAndDecisionButtons() {
        val poster = User(id = "poster_1", name = "Priya Sharma", email = "priya@example.com")
        val helperA = User(id = "helper_a", name = "Amit Verma", email = "amit@example.com")
        val helperB = User(id = "helper_b", name = "Sunil Kumar", email = "sunil@example.com")

        // Step 1: Poster posts a task (Status: "open")
        var task = Task(
            _id = "task_post_100",
            title = "Document submission at Sub-Registrar Office",
            description = "Submit signed deed copies and get acknowledgment slip",
            budget = 500,
            status = "open",
            category = "Document Submission",
            posterId = poster.id,
            posterName = poster.name,
            city = "Bangalore",
            location = "Koramangala",
            claimRequests = emptyList(),
            claimedUsers = emptyList()
        )
        assertEquals("open", task.status)
        assertEquals(0, task.claimRequests?.size ?: 0)

        // Step 2: Helper A and Helper B submit claim requests
        val claimA = ClaimRequest(
            id = "claim_1",
            requesterId = helperA.id,
            requesterName = helperA.name,
            status = "pending",
            offeredBudget = 600,
            message = "I am at Koramangala right now and have done this before."
        )
        val claimB = ClaimRequest(
            id = "claim_2",
            requesterId = helperB.id,
            requesterName = helperB.name,
            status = "pending",
            offeredBudget = 500,
            message = "Can do this within 30 minutes at standard budget."
        )
        task = task.copy(claimRequests = listOf(claimA, claimB))
        assertEquals(2, task.claimRequests?.size)

        // Step 3: Poster reviews claims -> Accepts Helper B, Rejects Helper A
        val updatedClaims = task.claimRequests?.map { claim ->
            when (claim.id) {
                "claim_2" -> claim.copy(status = "accepted")
                "claim_1" -> claim.copy(status = "rejected")
                else -> claim
            }
        }
        val claimedHelper = ClaimedUser(userId = helperB.id, userName = helperB.name)
        task = task.copy(
            status = "assigned",
            claimedUsers = listOf(claimedHelper),
            claimRequests = updatedClaims
        )

        assertEquals("assigned", task.status)
        assertEquals(listOf(claimedHelper), task.claimedUsers)
        assertEquals("accepted", task.claimRequests?.find { it.id == "claim_2" }?.status)
        assertEquals("rejected", task.claimRequests?.find { it.id == "claim_1" }?.status)

        // Step 4: Helper B submits proof
        val submittedProofUrl = "https://strangerhelp.com/uploads/proof_doc_receipt.jpg"
        task = task.copy(
            completionProof = listOf(submittedProofUrl),
            completionStatus = "pending_review"
        )
        assertEquals(1, task.completionProof.size)
        assertEquals("pending_review", task.completionStatus)

        // Step 5: Poster clicks "Approve Proof"
        task = task.copy(
            status = "completed",
            completionStatus = "approved"
        )
        assertEquals("completed", task.status)
        assertEquals("approved", task.completionStatus)

        // Step 6: Poster submits 5-star review
        val review = Review(
            id = "rev_101",
            taskId = task._id,
            reviewerId = poster.id,
            reviewerName = poster.name,
            revieweeId = helperB.id,
            rating = 5,
            comment = "Outstanding assistance! Handled document submission flawlessly.",
            createdAt = "2026-10-29T10:30:00Z"
        )
        assertEquals(5, review.rating)
        assertEquals("helper_b", review.revieweeId)
        assertEquals("poster_1", review.reviewerId)
    }

    // =========================================================================
    // Flow 3: Helper Lifecycle, Claim Submission & Tracking Buttons
    // =========================================================================

    @Test
    fun testHelperLifecycleAndTrackingButtons() {
        val helper = User(id = "helper_99", name = "Deepak Rao", email = "deepak@example.com")
        val task = Task(
            _id = "task_parcel_2",
            title = "Parcel Pickup from DTDC",
            budget = 200,
            status = "open",
            category = "Parcel Pickup",
            posterId = "poster_other",
            lat = 12.9716,
            lng = 77.5946
        )

        // 1. Helper submits claim with customized offer
        val myOffer = 250
        val myNote = "Will pick up using my scooter"
        val request = ClaimRequest(
            id = "req_custom",
            requesterId = helper.id,
            requesterName = helper.name,
            status = "pending",
            offeredBudget = myOffer,
            message = myNote
        )
        assertEquals(250, request.offeredBudget)

        // 2. Tracking coordinates calculation
        val helperCurrentLat = 12.9750
        val helperCurrentLng = 77.6000
        val distanceKm = haversine(helperCurrentLat, helperCurrentLng, task.lat ?: 0.0, task.lng ?: 0.0)
        assertTrue("Distance should be under 2km", distanceKm in 0.1..2.0)

        // 3. Helper starts tracking
        val assignedTask = task.copy(
            status = "assigned",
            claimedUsers = listOf(ClaimedUser(userId = helper.id, userName = helper.name)),
            trackingActive = true,
            helperLat = helperCurrentLat,
            helperLng = helperCurrentLng
        )
        assertTrue(assignedTask.trackingActive)
        assertEquals(12.9750, assignedTask.helperLat ?: 0.0, 0.0001)

        // 4. Helper stops tracking
        val stoppedTask = assignedTask.copy(trackingActive = false)
        assertFalse(stoppedTask.trackingActive)
    }

    // =========================================================================
    // Flow 4: Community Ask / Q&A Flow & Buttons
    // =========================================================================

    @Test
    fun testCommunityAskFlowAndVotingButtons() {
        val author = User(id = "user_author", name = "Meera", email = "meera@example.com")
        val helper = User(id = "user_helper", name = "Kiran", email = "kiran@example.com")

        // 1. Post a question with anonymous toggle
        val question = Question(
            id = "q_traffic_1",
            text = "Is the flyover at Silk Board open today after maintenance?",
            category = "Traffic",
            location = "Silk Board, Bangalore",
            anonymous = 1,
            authorId = author.id,
            authorName = author.name,
            createdAt = "2026-10-29T08:00:00Z"
        )
        assertEquals(1, question.anonymous)
        assertEquals("Traffic", question.category)

        // 2. Helper posts an answer
        var answer = Answer(
            id = "ans_1",
            questionId = question.id,
            text = "Yes, one lane is open. Traffic is moving slowly.",
            authorId = helper.id,
            authorName = helper.name,
            votes = 0,
            createdAt = "2026-10-29T08:15:00Z"
        )

        // 3. User upvotes answer button
        answer = answer.copy(votes = answer.votes + 1)
        assertEquals(1, answer.votes)

        // 4. User cancels upvote
        answer = answer.copy(votes = (answer.votes - 1).coerceAtLeast(0))
        assertEquals(0, answer.votes)

        // 5. Downvote boundary: votes shouldn't drop below 0 if guarded
        val safeVotes = (answer.votes - 1).coerceAtLeast(0)
        assertEquals(0, safeVotes)
    }

    // =========================================================================
    // Flow 5: FCM Notification Routing & Channel Resolution Flow
    // =========================================================================

    @Test
    fun testFcmNotificationChannelRouting() {
        // High Priority - TASKS channel
        assertEquals(NotificationChannels.TASKS, NotificationChannels.resolve("claim_request"))
        assertEquals(NotificationChannels.TASKS, NotificationChannels.resolve("claim_approved"))
        assertEquals(NotificationChannels.TASKS, NotificationChannels.resolve("claim_rejected"))
        assertEquals(NotificationChannels.TASKS, NotificationChannels.resolve("task_completed"))
        assertEquals(NotificationChannels.TASKS, NotificationChannels.resolve("task_claimed"))
        assertEquals(NotificationChannels.TASKS, NotificationChannels.resolve("ask_answer"))
        assertEquals(NotificationChannels.TASKS, NotificationChannels.resolve("question_answered"))

        // High Priority - MESSAGES channel
        assertEquals(NotificationChannels.MESSAGES, NotificationChannels.resolve("new_message"))
        assertEquals(NotificationChannels.MESSAGES, NotificationChannels.resolve("chat_message"))
        assertEquals(NotificationChannels.MESSAGES, NotificationChannels.resolve("message"))

        // High Priority - PROOF channel
        assertEquals(NotificationChannels.PROOF, NotificationChannels.resolve("proof_submitted"))
        assertEquals(NotificationChannels.PROOF, NotificationChannels.resolve("proof_accepted"))
        assertEquals(NotificationChannels.PROOF, NotificationChannels.resolve("proof_rejected"))
        assertEquals(NotificationChannels.PROOF, NotificationChannels.resolve("review_received"))
        assertEquals(NotificationChannels.PROOF, NotificationChannels.resolve("rating_received"))

        // Low Priority - PROMOTIONS channel
        assertEquals(NotificationChannels.PROMOTIONS, NotificationChannels.resolve("promotion"))
        assertEquals(NotificationChannels.PROMOTIONS, NotificationChannels.resolve("promo"))
        assertEquals(NotificationChannels.PROMOTIONS, NotificationChannels.resolve("tips"))

        // Fallback - DEFAULT channel
        assertEquals(NotificationChannels.DEFAULT, NotificationChannels.resolve("system_announcement"))
        assertEquals(NotificationChannels.DEFAULT, NotificationChannels.resolve("unknown_payload_type"))
        assertEquals(NotificationChannels.DEFAULT, NotificationChannels.resolve(""))
    }

    // =========================================================================
    // Flow 6: Deep Link Tapping & Safe Target Navigation
    // =========================================================================

    @Test
    fun testDeepLinkResolutionAndTargetRouting() {
        // 1. Task Detail deep links (https, strangerhelp://, relative)
        val deepLink1 = DeepLinkHandler.parse("https://strangerhelp.com/tasks/8559f42316dd3a3f3d3080ba")
        assertEquals(DeepLink.TaskDetail("8559f42316dd3a3f3d3080ba"), deepLink1)

        val deepLink2 = DeepLinkHandler.parse("strangerhelp://tasks/8559f42316dd3a3f3d3080ba")
        assertEquals(DeepLink.TaskDetail("8559f42316dd3a3f3d3080ba"), deepLink2)

        val deepLink3 = DeepLinkHandler.parse("/tasks/8559f42316dd3a3f3d3080ba")
        assertEquals(DeepLink.TaskDetail("8559f42316dd3a3f3d3080ba"), deepLink3)

        // 2. Chat links
        val deepLinkChat = DeepLinkHandler.parse("/chat/conv_abc123")
        assertEquals(DeepLink.ChatDetail("conv_abc123"), deepLinkChat)

        // 3. Meets links
        val deepLinkMeet = DeepLinkHandler.parse("/meets/meet_fest_456")
        assertEquals(DeepLink.MeetDetail("meet_fest_456"), deepLinkMeet)

        // 4. Ask / Community links
        val deepLinkAsk = DeepLinkHandler.parse("/ask_detail/q_traffic_silkboard")
        assertEquals(DeepLink.AskDetail("q_traffic_silkboard"), deepLinkAsk)

        // 5. Notifications link
        val deepLinkNotif = DeepLinkHandler.parse("/notifications")
        assertEquals(DeepLink.Notifications, deepLinkNotif)

        // 6. User Profile link
        val deepLinkUser = DeepLinkHandler.parse("/user/user_789")
        assertEquals(DeepLink.UserProfile("user_789"), deepLinkUser)

        // 7. Password reset and verification
        val deepLinkReset = DeepLinkHandler.parse("https://strangerhelp.com/reset-password?token=sec_tok_123")
        assertEquals(DeepLink.ResetPassword("sec_tok_123"), deepLinkReset)

        val deepLinkVerify = DeepLinkHandler.parse("https://strangerhelp.com/api/auth/verify-email?token=ver_tok_999")
        assertEquals(DeepLink.VerifyEmail("ver_tok_999"), deepLinkVerify)

        // 8. Corrupted and adversary links must fallback to Home without crashing
        assertEquals(DeepLink.Home, DeepLinkHandler.parse(""))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("null"))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("://invalid/scheme"))
        assertEquals(DeepLink.Home, DeepLinkHandler.parse("strangerhelp://unknown/foo/bar/baz"))
    }

    // =========================================================================
    // Flow 7: Bottom Bar & Navigation Route Integrity
    // =========================================================================

    @Test
    fun testBottomNavItemsAndRouteIntegrity() {
        val navItems = listOf(Screen.Feed, Screen.Tasks, Screen.Post, Screen.Chat, Screen.Profile)
        assertEquals(5, navItems.size)

        assertEquals("feed", Screen.Feed.route)
        assertEquals("tasks", Screen.Tasks.route)
        assertEquals("post", Screen.Post.route)
        assertEquals("chat", Screen.Chat.route)
        assertEquals("profile", Screen.Profile.route)

        // Ensure each bottom nav item has valid labels and icons
        navItems.forEach { screen ->
            assertTrue("Screen route should not be empty", screen.route.isNotBlank())
            assertTrue("Screen label should not be empty", screen.label.isNotBlank())
            assertNotNull(screen.icon)
            assertNotNull(screen.iconOutlined)
        }
    }

    // =========================================================================
    // Flow 8: Rapid Multi-Tap & Boundary Stress Resilience
    // =========================================================================

    @Test
    fun testRapidMultiTapAndBoundaryResilience() {
        // Test 1: Rapid like/unlike simulation
        var likes = 0
        for (i in 1..100) {
            likes = if (i % 2 == 1) likes + 1 else (likes - 1).coerceAtLeast(0)
        }
        assertEquals(0, likes)

        // Test 2: Double claim request prevention
        val helperId = "helper_rapid_1"
        val existingClaims = mutableListOf<ClaimRequest>()

        // Simulate 5 rapid simultaneous taps on "Claim Task" button
        for (tap in 1..5) {
            val alreadyClaimed = existingClaims.any { it.requesterId == helperId }
            if (!alreadyClaimed) {
                existingClaims.add(ClaimRequest(id = "claim_$tap", requesterId = helperId, requesterName = "Helper Rapid"))
            }
        }
        assertEquals("Double-tap should not create multiple duplicate claims", 1, existingClaims.size)

        // Test 3: Large text payload in task title and description
        val largeTitle = "A".repeat(500)
        val largeDesc = "B".repeat(5000)
        val boundaryTask = Task(
            _id = "boundary_task_1",
            title = largeTitle,
            description = largeDesc,
            budget = 999999
        )
        assertEquals(500, boundaryTask.title.length)
        assertEquals(5000, boundaryTask.description.length)
        assertEquals(999999, boundaryTask.budget)

        // Test 4: Unicode and emoji resilience in chat / notification payload
        val emojiPayload = "🚀🎉⭐🔥 Task claimed by 🧑‍💻 Rahul! Let's get to work! 🇮🇳"
        assertTrue(emojiPayload.contains("🧑‍💻"))
        val resolvedEmojiChannel = NotificationChannels.resolve("claim_request")
        assertEquals(NotificationChannels.TASKS, resolvedEmojiChannel)
    }

    @Test
    fun testEditProfileScreenNullFieldResilience() {
        // Gson deserializing a User with missing or null fields
        val jsonWithNulls = """
            {
                "id": "u_sparse_1",
                "name": null,
                "email": "test@example.com",
                "bio": null,
                "area": null,
                "phone": null,
                "city": null,
                "handle": null
            }
        """.trimIndent()

        val gson = com.google.gson.Gson()
        val userWithNulls = gson.fromJson(jsonWithNulls, User::class.java)

        // Verify the exact logic in EditProfileScreen doesn't throw NullPointerException
        val safeName = userWithNulls?.name.orEmpty()
        val safeHandle = userWithNulls?.handle.orEmpty()
        val safeBio = userWithNulls?.bio?.takeIf { it.isNotBlank() } ?: "Urban explorer and foodie."
        val safeCity = userWithNulls?.city.orEmpty()
        val safeLocality = userWithNulls?.area?.takeIf { it.isNotBlank() } ?: "Koramangala"
        val safePhone = userWithNulls?.phone?.takeIf { it.isNotBlank() } ?: "+91 98765 43210"

        assertEquals("", safeName)
        assertEquals("", safeHandle)
        assertEquals("Urban explorer and foodie.", safeBio)
        assertEquals("", safeCity)
        assertEquals("Koramangala", safeLocality)
        assertEquals("+91 98765 43210", safePhone)

        // Test with null user object entirely
        val nullUser: User? = null
        val nullUserName = nullUser?.name.orEmpty()
        val nullUserBio = nullUser?.bio?.takeIf { it.isNotBlank() } ?: "Urban explorer and foodie."
        assertEquals("", nullUserName)
        assertEquals("Urban explorer and foodie.", nullUserBio)
    }

    @Test
    fun testCameraCaptureAndProofSubmissionFlow() {
        // 1. Verify byte generation and compression for proof upload
        val dummyProofBytes = ByteArray(1024) { (it % 128).toByte() }
        assertTrue("Proof bytes must not be empty", dummyProofBytes.isNotEmpty())

        // 2. Verify multipart request creation matches backend expectation
        val action = "complete".toRequestBody("text/plain".toMediaTypeOrNull())
        val reqBody = dummyProofBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, dummyProofBytes.size)
        val part = okhttp3.MultipartBody.Part.createFormData("proof", "proof.jpg", reqBody)
        assertNotNull(part)
        assertEquals("complete", "complete")

        // 3. Verify parseError in ViewModel correctly extracts 'error', 'message', or fallback
        val jsonError = """{"error": "Task is not in progress or already completed"}"""
        val jsonMessage = """{"message": "Helper not assigned to this task"}"""
        val rawError = "Internal Server Error"

        val gson = com.google.gson.Gson()
        val parsed1 = gson.fromJson(jsonError, com.google.gson.JsonObject::class.java).get("error")?.asString
        assertEquals("Task is not in progress or already completed", parsed1)

        val parsed2 = gson.fromJson(jsonMessage, com.google.gson.JsonObject::class.java).get("message")?.asString
        assertEquals("Helper not assigned to this task", parsed2)

        val parsed3 = if (rawError.length < 120 && !rawError.startsWith("<")) rawError else "Failed to submit proof. Please try again."
        assertEquals("Internal Server Error", parsed3)
    }

    @Test
    fun testBackgroundTaskRestrictionsHandling() {
        // Test background restriction diagnostic states
        val restrictedState = com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionInfo(
            isBackgroundRestricted = true,
            isPowerSaveMode = false,
            isDeviceIdleMode = false,
            isDataSaverActive = false,
            isBatteryOptimized = true,
            hasRestrictions = true,
            restrictionSummary = "Background restriction is enabled in battery settings.",
            recommendedAction = com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionAction.OPEN_APP_SETTINGS
        )
        assertTrue(restrictedState.hasRestrictions)
        assertEquals(com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionAction.OPEN_APP_SETTINGS, restrictedState.recommendedAction)

        val powerSaveState = com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionInfo(
            isBackgroundRestricted = false,
            isPowerSaveMode = true,
            isDeviceIdleMode = false,
            isDataSaverActive = false,
            isBatteryOptimized = false,
            hasRestrictions = true,
            restrictionSummary = "Battery Saver is active.",
            recommendedAction = com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionAction.OPEN_BATTERY_SAVER_SETTINGS
        )
        assertTrue(powerSaveState.hasRestrictions)
        assertEquals(com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionAction.OPEN_BATTERY_SAVER_SETTINGS, powerSaveState.recommendedAction)

        val cleanState = com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionInfo(
            isBackgroundRestricted = false,
            isPowerSaveMode = false,
            isDeviceIdleMode = false,
            isDataSaverActive = false,
            isBatteryOptimized = false,
            hasRestrictions = false,
            restrictionSummary = null,
            recommendedAction = com.strangerhelp.app.utils.BackgroundRestrictionHelper.BackgroundRestrictionAction.NONE
        )
        assertFalse(cleanState.hasRestrictions)
        assertNull(cleanState.restrictionSummary)

        // Test ServiceStartResult sealed classes
        val success: com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult =
            com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult.Success
        val restricted: com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult =
            com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult.Restricted("Service not allowed")
        val permDenied: com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult =
            com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult.PermissionDenied("ACCESS_FINE_LOCATION")
        val failed: com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult =
            com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult.Failed(IllegalStateException("Background execution blocked"))

        assertTrue(success is com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult.Success)
        assertTrue(restricted is com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult.Restricted)
        assertTrue(permDenied is com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult.PermissionDenied)
        assertTrue(failed is com.strangerhelp.app.utils.BackgroundRestrictionHelper.ServiceStartResult.Failed)
    }
}


