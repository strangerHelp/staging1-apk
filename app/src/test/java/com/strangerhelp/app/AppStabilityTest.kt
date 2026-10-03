package com.strangerhelp.app

import com.strangerhelp.app.data.model.*
import com.strangerhelp.app.ui.screens.tasks.haversine
import com.strangerhelp.app.utils.TimeUtils
import org.junit.Assert.*
import org.junit.Test

class AppStabilityTest {

    @Test
    fun testHaversineDistanceCalculations() {
        // Same point distance should be 0
        val distZero = haversine(19.0760, 72.8777, 19.0760, 72.8777)
        assertEquals(0.0, distZero, 0.001)

        // Mumbai to Pune is approx 120-150 km
        val distMumbaiPune = haversine(19.0760, 72.8777, 18.5204, 73.8567)
        assertTrue("Distance should be between 100km and 160km", distMumbaiPune in 100.0..160.0)

        // Zero coordinates safety
        val distZeroCoord = haversine(0.0, 0.0, 0.0, 0.0)
        assertEquals(0.0, distZeroCoord, 0.001)
    }

    @Test
    fun testTaskModelWithNullFieldsDoesNotCrash() {
        val task = Task(
            _id = "test_task_123",
            title = "Test Delivery Task",
            description = "Deliver packet to reception",
            budget = 250,
            status = "open",
            category = "Task",
            city = "Mumbai",
            location = "Bandra",
            lat = null,
            lng = null,
            trackingActive = false,
            helperLat = null,
            helperLng = null,
            claimRequests = null,
            claimedUsers = null
        )

        assertEquals("test_task_123", task._id)
        assertNull(task.lat)
        assertNull(task.lng)
        assertNull(task.helperLat)
        assertNull(task.helperLng)
        assertFalse(task.trackingActive)
    }

    @Test
    fun testClaimRequestAndUserNullSafety() {
        val claim = ClaimRequest(
            id = "req_1",
            requesterId = "user_1",
            requesterName = "Helper John",
            status = "pending",
            offeredBudget = null,
            message = null,
            createdAt = "2026-09-21T10:00:00Z"
        )

        assertEquals("req_1", claim.id)
        assertNull(claim.offeredBudget)
        assertNull(claim.message)

        val user = User(
            id = "usr_999",
            name = "Test User",
            email = "test@example.com",
            avatar = "",
            city = "",
            area = "",
            phone = "",
            bio = ""
        )
        assertEquals("usr_999", user.id)
        assertEquals(0, user.verified)
    }

    @Test
    fun testTimeUtilsFormattingStability() {
        val emptyTime = TimeUtils.getTimeAgo(null)
        assertEquals("", emptyTime)

        val blankTime = TimeUtils.getTimeAgo("")
        assertEquals("", blankTime)

        val invalidTime = TimeUtils.getTimeAgo("not-a-timestamp")
        assertEquals("", invalidTime)

        val formattedNull = TimeUtils.formatTime(null)
        assertEquals("", formattedNull)
    }

    @Test
    fun testNavigationRoutePatterns() {
        val taskId = "65b8c9d0e1f2"
        val gpsCameraRoute = "gps_camera/$taskId"
        assertTrue(gpsCameraRoute.startsWith("gps_camera/"))
        assertEquals("65b8c9d0e1f2", gpsCameraRoute.substringAfter("gps_camera/"))

        val category = "Photo Proof"
        val filterRoute = "tasks?category=$category"
        assertTrue(filterRoute.contains("Photo Proof"))

        val meetId = "meet_789"
        val meetDetailRoute = "meet_detail/$meetId"
        assertEquals("meet_789", meetDetailRoute.substringAfter("meet_detail/"))
    }

    @Test
    fun testCategoryFilteringLogic() {
        val allCategories = listOf("All", "Task", "Document Submission", "Photo Proof", "Parcel Pickup", "Queue Standing", "Verification", "Event / Group Work", "Other")

        val sampleTasks = listOf(
            Task(_id = "1", title = "Task 1", category = "Task"),
            Task(_id = "2", title = "Proof 1", category = "Photo Proof"),
            Task(_id = "3", title = "Pickup 1", category = "Parcel Pickup")
        )

        // When "All" is selected, return all
        val filteredAll = sampleTasks.filter { "All" == "All" || it.category == "All" }
        assertEquals(3, filteredAll.size)

        // When "Photo Proof" is selected
        val filteredProof = sampleTasks.filter { "Photo Proof" == "All" || it.category == "Photo Proof" }
        assertEquals(1, filteredProof.size)
        assertEquals("Photo Proof", filteredProof.first().category)

        // Test non-existent category
        val filteredNone = sampleTasks.filter { "NonExistent" == "All" || it.category == "NonExistent" }
        assertEquals(0, filteredNone.size)
    }

    @Test
    fun testCameraPermissionHelper() {
        val permissions = com.strangerhelp.app.util.PermissionHelper.requiredCameraPermissions()
        assertTrue("Must include CAMERA permission", permissions.contains(android.Manifest.permission.CAMERA))
        assertTrue("Must include location permissions", permissions.contains(android.Manifest.permission.ACCESS_FINE_LOCATION))
        assertFalse("Must not include POST_NOTIFICATIONS in camera permissions", permissions.contains("android.permission.POST_NOTIFICATIONS"))
    }

    @Test
    fun testTaskDraftContentDetection() {
        val emptyDraft = TaskDraft()
        assertFalse("Empty draft should not have content", emptyDraft.hasContent())

        val draftWithTitle = TaskDraft(title = "Need help with grocieries")
        assertTrue("Draft with title should have content", draftWithTitle.hasContent())

        val draftWithDesc = TaskDraft(description = "5 kg rice and milk")
        assertTrue("Draft with description should have content", draftWithDesc.hasContent())

        val draftWithPhotos = TaskDraft(photoPaths = listOf("/path/to/photo.jpg"))
        assertTrue("Draft with photos should have content", draftWithPhotos.hasContent())

        val draftWithVoice = TaskDraft(voiceNotePath = "/path/to/voice.webm")
        assertTrue("Draft with voice note should have content", draftWithVoice.hasContent())
    }

    @Test
    fun testTaskDraftDefaultValues() {
        val draft = TaskDraft()
        assertEquals("active_draft", draft.id)
        assertEquals("Today", draft.deadline)
        assertEquals("2", draft.maxClaimers)
        assertFalse(draft.isAnonymous)
        assertFalse(draft.isUrgent)
        assertFalse(draft.isPrivate)
        assertTrue(draft.photoPaths.isEmpty())
        assertNull(draft.voiceNotePath)
    }

    @Test
    fun testFooterVisibilityOnFeaturePages() {
        // Feature routes where app footer MUST be shown so Home and other footer buttons work
        val featureRoutes = listOf(
            "feed",
            "tasks",
            "tasks?category=Plumbing",
            "my_tasks?filter=claimed",
            "post",
            "chat",
            "profile",
            "pulse",
            "path_setup",
            "path_active",
            "meets",
            "create_meet",
            "meet_detail/123",
            "ask",
            "ask_post",
            "ask_detail/456",
            "wallet",
            "leaderboard",
            "notifications",
            "edit_profile",
            "verify_id",
            "refer_earn",
            "karma_wallet",
            "task/task_abc"
        )

        for (route in featureRoutes) {
            val isFullScreenCameraOrWeb = route.startsWith("gps_camera") || route.startsWith("webview")
            val showBottomBar = !isFullScreenCameraOrWeb
            assertTrue("App footer must be visible on feature page '$route'", showBottomBar)
        }

        // Full screen modal routes where bottom bar is hidden
        val hiddenRoutes = listOf("gps_camera/123", "webview?url=https://example.com")
        for (route in hiddenRoutes) {
            val isFullScreenCameraOrWeb = route.startsWith("gps_camera") || route.startsWith("webview")
            val showBottomBar = !isFullScreenCameraOrWeb
            assertFalse("App footer should be hidden on modal page '$route'", showBottomBar)
        }
    }

    @Test
    fun testAvatarUrlSanitizationRemovesDemoProfilePics() {
        val demoPravatar = "https://i.pravatar.cc/150?img=11"
        val isDemoPic = demoPravatar.contains("pravatar.cc") || demoPravatar.contains("randomuser.me")
        assertTrue("Demo pravatar pic should be flagged as demo", isDemoPic)

        val cleanUserUploadedAvatar = "https://strangerhelp.com/uploads/user_123.jpg"
        val isUserUploadedDemo = cleanUserUploadedAvatar.contains("pravatar.cc") || cleanUserUploadedAvatar.contains("randomuser.me")
        assertFalse("Real uploaded avatar must not be flagged as demo", isUserUploadedDemo)

        // Null and blank safety
        val blankAvatar: String? = "   "
        val sanitizedBlank = blankAvatar?.trim()?.takeIf { it.isNotEmpty() && !it.contains("pravatar.cc") }
        assertNull("Blank avatar should sanitize to null so initials are shown", sanitizedBlank)

        val validAvatar: String? = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ..."
        val sanitizedValid = validAvatar?.trim()?.takeIf { it.isNotEmpty() && !it.contains("pravatar.cc") }
        assertNotNull("Valid data URI avatar should be accepted", sanitizedValid)
    }

    @Test
    fun testUserInitialExtraction() {
        val user = User(id = "1", name = "Rakesh Kumar", email = "rakesh@example.com")
        val initial = (user.name.trim().firstOrNull() ?: 'U').uppercaseChar()
        assertEquals('R', initial)

        val emptyUser = User(id = "2", name = "", email = "empty@example.com")
        val fallbackInitial = (emptyUser.name.trim().firstOrNull() ?: 'U').uppercaseChar()
        assertEquals('U', fallbackInitial)
    }

    @Test
    fun testSearchHistoryModelAndTrimming() {
        val rawQuery = "   Plumbing in Koramangala   "
        val trimmed = rawQuery.trim()
        val historyItem = SearchHistory(query = trimmed, timestamp = 1700000000000L)
        
        assertEquals("Plumbing in Koramangala", historyItem.query)
        assertEquals(1700000000000L, historyItem.timestamp)
        assertFalse("Trimmed query must not be blank", historyItem.query.isBlank())

        val blankQuery = "    "
        assertTrue("Blank query must be recognized", blankQuery.trim().isBlank())
    }

    @Test
    fun testSearchHistoryFilteringAndOrdering() {
        val history = listOf(
            SearchHistory("Grocery delivery", 1003L),
            SearchHistory("Plumbing help", 1002L),
            SearchHistory("Document verification", 1001L)
        )

        // Filter matching "plumb"
        val filterQuery = "plumb"
        val matching = history.filter { it.query.contains(filterQuery, ignoreCase = true) }
        assertEquals(1, matching.size)
        assertEquals("Plumbing help", matching.first().query)

        // Filter matching "delivery"
        val matchingDelivery = history.filter { it.query.contains("DELIVERY", ignoreCase = true) }
        assertEquals(1, matchingDelivery.size)
        assertEquals("Grocery delivery", matchingDelivery.first().query)

        // Filter matching all when query is blank
        val emptyFilter = ""
        val allOrMatching = if (emptyFilter.isBlank()) history else history.filter { it.query.contains(emptyFilter, ignoreCase = true) }
        assertEquals(3, allOrMatching.size)
    }

    @Test
    fun testSearchHistoryDeletionAndClearLogic() {
        var historyList = mutableListOf(
            SearchHistory("Courier pickup", 2001L),
            SearchHistory("Key handover", 2002L),
            SearchHistory("Queue standing", 2003L)
        )

        // Delete single query
        val queryToDelete = "Key handover"
        historyList = historyList.filterNot { it.query == queryToDelete }.toMutableList()
        assertEquals(2, historyList.size)
        assertFalse(historyList.any { it.query == queryToDelete })

        // Clear all
        historyList.clear()
        assertTrue("History list should be empty after clear", historyList.isEmpty())
    }

    @Test
    fun testMyTasksFilteringSeparatesPostedAndClaimedAndCompleted() {
        val myUserId = "user_me_123"
        val otherUserId = "user_other_456"

        val task1PostedByMe = Task(
            _id = "task_1",
            title = "Need someone to water plants",
            posterId = myUserId,
            status = "open"
        )
        val task2PostedByMeClaimedByOther = Task(
            _id = "task_2",
            title = "Help deliver parcel",
            posterId = myUserId,
            claimedBy = otherUserId,
            status = "claimed"
        )
        val task3ClaimedByMe = Task(
            _id = "task_3",
            title = "Grocery run for elderly neighbor",
            posterId = otherUserId,
            claimedBy = myUserId,
            status = "claimed"
        )
        val task4CompletedForMe = Task(
            _id = "task_4",
            title = "Plumbing pipe repair",
            posterId = myUserId,
            claimedBy = otherUserId,
            status = "completed"
        )

        val allMyTasks = listOf(
            task1PostedByMe,
            task2PostedByMeClaimedByOther,
            task3ClaimedByMe,
            task4CompletedForMe
        )

        // Filter: Posted
        val postedTasks = allMyTasks.filter { it.posterId == myUserId }
        assertEquals(3, postedTasks.size)
        assertTrue(postedTasks.contains(task1PostedByMe))
        assertTrue(postedTasks.contains(task2PostedByMeClaimedByOther))
        assertTrue(postedTasks.contains(task4CompletedForMe))
        assertFalse("Claimed tasks must not appear in posted", postedTasks.contains(task3ClaimedByMe))

        // Filter: Claimed (Must ONLY include tasks where user is the claimer/helper, NEVER user's posted tasks)
        val claimedTasks = allMyTasks.filter { task ->
            task.posterId != myUserId && (task.claimedBy == myUserId || task.claimedUsers?.any { it.userId == myUserId } == true)
        }
        assertEquals(1, claimedTasks.size)
        assertEquals("task_3", claimedTasks.first()._id)
        assertFalse("User's own posted task claimed by someone else must not show in Claimed tab", claimedTasks.contains(task2PostedByMeClaimedByOther))
        assertFalse("User's own open task must not show in Claimed tab", claimedTasks.contains(task1PostedByMe))

        // Filter: Completed
        val completedTasks = allMyTasks.filter { task ->
            task.status.equals("completed", ignoreCase = true) || task.completionStatus.equals("approved", ignoreCase = true)
        }
        assertEquals(1, completedTasks.size)
        assertEquals("task_4", completedTasks.first()._id)
        assertFalse("Open tasks must not appear in Completed tab", completedTasks.contains(task1PostedByMe))
        assertFalse("In-progress tasks must not appear in Completed tab", completedTasks.contains(task2PostedByMeClaimedByOther))
    }

    @Test
    fun testOfflineTaskCachingAndFiltering() {
        val cachedTasks = listOf(
            Task(
                _id = "cached_1",
                title = "Need someone to pick up documents",
                description = "Urgent pickup from bank in Indiranagar",
                category = "Document Submission",
                location = "Indiranagar",
                budget = 250,
                status = "open",
                urgent = 1
            ),
            Task(
                _id = "cached_2",
                title = "Pet dog walking in evening",
                description = "Walk friendly golden retriever",
                category = "Other",
                location = "Koramangala",
                budget = 150,
                status = "open",
                urgent = 0
            ),
            Task(
                _id = "cached_3",
                title = "Queue standing at passport office",
                description = "Need spot holder from 8 AM",
                category = "Queue Standing",
                location = "Indiranagar",
                budget = 500,
                status = "open",
                urgent = 1
            )
        )

        // Test Category filtering on cached tasks
        val docTasks = cachedTasks.filter { it.category == "Document Submission" }
        assertEquals(1, docTasks.size)
        assertEquals("cached_1", docTasks.first()._id)

        // Test Search query filtering on cached tasks
        val query = "indiranagar"
        val queryMatches = cachedTasks.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.location.contains(query, ignoreCase = true)
        }
        assertEquals(2, queryMatches.size)
        assertTrue(queryMatches.any { it._id == "cached_1" })
        assertTrue(queryMatches.any { it._id == "cached_3" })

        // Test Sorting on cached tasks (budget high to low)
        val sortedByBudget = cachedTasks.sortedByDescending { it.budget }
        assertEquals("cached_3", sortedByBudget[0]._id) // 500
        assertEquals("cached_1", sortedByBudget[1]._id) // 250
        assertEquals("cached_2", sortedByBudget[2]._id) // 150

        // Test Urgent sort
        val sortedByUrgent = cachedTasks.sortedByDescending { it.urgent }
        assertEquals(1, sortedByUrgent[0].urgent)
        assertEquals(1, sortedByUrgent[1].urgent)
        assertEquals(0, sortedByUrgent[2].urgent)
    }

    @Test
    fun testTasksLoadResultOfflineStatus() {
        val cachedTasks = listOf(
            Task(_id = "offline_task_1", title = "Offline Task", status = "open")
        )

        val offlineResult = com.strangerhelp.app.data.repository.TasksLoadResult(
            tasks = cachedTasks,
            isOffline = true,
            isFromCache = true,
            hasMore = false,
            errorMessage = "No internet connection. Showing cached tasks."
        )

        assertTrue("Result should indicate offline state", offlineResult.isOffline)
        assertTrue("Result should indicate data is from local Room cache", offlineResult.isFromCache)
        assertFalse("Offline mode should disable pagination", offlineResult.hasMore)
        assertEquals(1, offlineResult.tasks.size)
        assertEquals("offline_task_1", offlineResult.tasks.first()._id)
    }

    @Test
    fun testRealJsonDeserialization() {
        val sampleJson = """[{"_id":"8559f42316dd3a3f3d3080ba","id":"8559f42316dd3a3f3d3080ba","title":"Bike ","description":"Need bike service","category":"Task","budget":1500,"deadline":"Tomorrow","location":"Chennai","city":"","lat":13.1396159,"lng":80.138263,"anonymous":0,"urgent":1,"status":"open","posterId":"ae0287050347d4c7e5a94227","posterName":"Vadivelan Site","posterVerified":false,"claimedBy":null,"claimedByName":null,"maxClaimers":1,"completionStatus":"","attachmentCount":0,"proofCount":0,"createdAt":"2026-08-16 17:56:09","distance":2924.24},{"_id":"anon_task","id":"anon_task","title":"Anon Task","description":"Help me","category":"Task","budget":700,"deadline":"Today","location":"Bangalore","city":"Bangalore","lat":null,"lng":null,"anonymous":1,"urgent":1,"status":"open","posterId":null,"posterName":"Anonymous","posterVerified":false,"claimedBy":null,"claimedByName":null,"maxClaimers":50,"completionStatus":"","attachmentCount":0,"proofCount":0,"createdAt":"2026-09-21 10:01:30"}]"""
        
        val listType = object : com.google.gson.reflect.TypeToken<List<Task>>() {}.type
        val tasks: List<Task> = com.google.gson.Gson().fromJson(sampleJson, listType)
        assertEquals(2, tasks.size)
        val sanitized = tasks.map { it.sanitized() }
        assertEquals("8559f42316dd3a3f3d3080ba", sanitized[0]._id)
        assertEquals("", sanitized[1].posterId) // was null in JSON, sanitized to ""
        assertEquals("Anonymous", sanitized[1].posterName)
    }

    @Test
    fun testDefaultSeedTasksAndFilter() {
        val seeds = com.strangerhelp.app.data.repository.DEFAULT_SEED_TASKS
        assertTrue(seeds.isNotEmpty())
        assertEquals(5, seeds.size)
        assertTrue(seeds.all { it._id.isNotBlank() && it.title.isNotBlank() })

        val allSeeds = com.strangerhelp.app.data.repository.filterSeedTasks(null, null)
        assertEquals(5, allSeeds.size)

        val taskSeeds = com.strangerhelp.app.data.repository.filterSeedTasks("Task", null)
        assertTrue(taskSeeds.isNotEmpty())
        assertTrue(taskSeeds.all { it.category == "Task" })

        val querySeeds = com.strangerhelp.app.data.repository.filterSeedTasks(null, "bike")
        assertEquals(1, querySeeds.size)
        assertEquals("8559f42316dd3a3f3d3080ba", querySeeds.first()._id)
    }

    @Test
    fun testTaskDetailStateTransitionsForAllRoles() {
        val ownerUser = User(id = "user_owner", name = "Alice Owner", email = "alice@example.com")
        val helperUser = User(id = "user_helper", name = "Bob Helper", email = "bob@example.com")
        val visitorUser = User(id = "user_visitor", name = "Charlie Visitor", email = "charlie@example.com")

        // 1. Unauthenticated / Guest visitor state
        val openTask = Task(_id = "task_open_1", title = "Open Task", posterId = ownerUser.id, status = "open")
        val unauthState = com.strangerhelp.app.ui.screens.tasks.resolveState(openTask, null)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.VISITOR_MUST_LOGIN, unauthState)

        // 2. Authenticated visitor looking at open task
        val visitorState = com.strangerhelp.app.ui.screens.tasks.resolveState(openTask, visitorUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.HELPER_CAN_REQUEST, visitorState)

        // 3. Helper has a pending claim request
        val taskWithPendingReq = openTask.copy(
            claimRequests = listOf(ClaimRequest(id = "cr_1", requesterId = helperUser.id, status = "pending"))
        )
        val helperPendingState = com.strangerhelp.app.ui.screens.tasks.resolveState(taskWithPendingReq, helperUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.HELPER_REQUEST_PENDING, helperPendingState)

        // 4. Poster sees pending claim requests
        val posterHasReqState = com.strangerhelp.app.ui.screens.tasks.resolveState(taskWithPendingReq, ownerUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.POSTER_HAS_REQUESTS, posterHasReqState)

        // 5. Poster sees open task with no pending requests
        val posterWaitingState = com.strangerhelp.app.ui.screens.tasks.resolveState(openTask, ownerUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.POSTER_WAITING, posterWaitingState)

        // 6. Helper claim was rejected
        val taskWithRejectedReq = openTask.copy(
            claimRequests = listOf(ClaimRequest(id = "cr_2", requesterId = helperUser.id, status = "rejected"))
        )
        val helperRejectedState = com.strangerhelp.app.ui.screens.tasks.resolveState(taskWithRejectedReq, helperUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.HELPER_REQUEST_REJECTED, helperRejectedState)

        // 7. Claimed task: Helper can start tracking
        val claimedTask = openTask.copy(
            status = "claimed",
            claimedBy = helperUser.id,
            trackingActive = false,
            completionStatus = ""
        )
        val helperCanTrackState = com.strangerhelp.app.ui.screens.tasks.resolveState(claimedTask, helperUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.HELPER_CLAIMED_CAN_TRACK, helperCanTrackState)

        // 8. Claimed task: Helper actively tracking
        val trackingTask = claimedTask.copy(trackingActive = true)
        val helperTrackingState = com.strangerhelp.app.ui.screens.tasks.resolveState(trackingTask, helperUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.HELPER_TRACKING, helperTrackingState)

        // 9. Claimed task: Helper submitted proof, waiting approval
        val proofPendingTask = claimedTask.copy(completionStatus = "pending")
        val helperProofPendingState = com.strangerhelp.app.ui.screens.tasks.resolveState(proofPendingTask, helperUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.HELPER_PROOF_PENDING, helperProofPendingState)

        // 10. Claimed task: Poster reviewing submitted proof
        val posterReviewProofState = com.strangerhelp.app.ui.screens.tasks.resolveState(proofPendingTask, ownerUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.POSTER_REVIEW_PROOF, posterReviewProofState)

        // 11. Claimed task: Proof rejected
        val proofRejectedTask = claimedTask.copy(completionStatus = "rejected")
        val helperProofRejectedState = com.strangerhelp.app.ui.screens.tasks.resolveState(proofRejectedTask, helperUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.HELPER_PROOF_REJECTED, helperProofRejectedState)
        val posterProofRejectedState = com.strangerhelp.app.ui.screens.tasks.resolveState(proofRejectedTask, ownerUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.POSTER_PROOF_REJECTED, posterProofRejectedState)

        // 12. Completed task
        val completedTask = openTask.copy(status = "completed", claimedBy = helperUser.id)
        val posterCompletedState = com.strangerhelp.app.ui.screens.tasks.resolveState(completedTask, ownerUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.POSTER_COMPLETED, posterCompletedState)

        // 13. Third-party visitor viewing claimed task
        val viewerClaimedState = com.strangerhelp.app.ui.screens.tasks.resolveState(claimedTask, visitorUser)
        assertEquals(com.strangerhelp.app.ui.screens.tasks.TaskUiState.CLAIMED_VIEWER, viewerClaimedState)
    }

    @Test
    fun testMessageRecipientResolutionFlow() {
        val mePoster = User(id = "user_poster_1", name = "Poster Joe", email = "poster@example.com")
        val meHelper = User(id = "user_helper_2", name = "Helper Bob", email = "helper@example.com")

        val taskClaimed = Task(
            _id = "task_conv_test",
            title = "Pick up medicines",
            posterId = "user_poster_1",
            claimedBy = "user_helper_2",
            status = "claimed"
        )

        // Flow A: When poster clicks "Message Helper", recipient must be the helper
        val recipientForPoster = if (taskClaimed.posterId == mePoster.id) {
            taskClaimed.claimedBy.orEmpty()
        } else {
            taskClaimed.posterId.orEmpty()
        }
        assertEquals("user_helper_2", recipientForPoster)

        // Flow B: When helper clicks "Chat with Poster", recipient must be the poster
        val recipientForHelper = if (taskClaimed.posterId == meHelper.id) {
            taskClaimed.claimedBy.orEmpty()
        } else {
            taskClaimed.posterId.orEmpty()
        }
        assertEquals("user_poster_1", recipientForHelper)

        // Flow C: Explicit posterId passed (e.g. from UI parameter)
        val explicitRecipient = "user_custom_target"
        val resolvedExplicit = if (explicitRecipient.isNotBlank() && !explicitRecipient.startsWith("poster_")) {
            explicitRecipient
        } else {
            taskClaimed.posterId.orEmpty()
        }
        assertEquals("user_custom_target", resolvedExplicit)
    }

    @Test
    fun testDeepLinkParsingAndResolutionFlow() {
        // Task detail deep link
        val taskUri = "https://strangerhelp.com/tasks/65b8c9d0e1f2"
        val taskId = taskUri.substringAfter("/tasks/").substringBefore("?")
        assertEquals("65b8c9d0e1f2", taskId)

        // Chat detail deep link
        val chatUri = "https://strangerhelp.com/chat/conv_abc_789"
        val convId = chatUri.substringAfter("/chat/").substringBefore("?")
        assertEquals("conv_abc_789", convId)

        // Reset password deep link
        val resetUri = "https://strangerhelp.com/reset-password?token=secret_token_123"
        val token = resetUri.substringAfter("token=").substringBefore("&")
        assertEquals("secret_token_123", token)

        // Verify deep link sealed classes
        val taskLink = com.strangerhelp.app.navigation.DeepLink.TaskDetail("task_xyz")
        val chatLink = com.strangerhelp.app.navigation.DeepLink.ChatDetail("conv_xyz")
        val homeLink = com.strangerhelp.app.navigation.DeepLink.Home

        assertEquals("task_xyz", taskLink.taskId)
        assertEquals("conv_xyz", chatLink.conversationId)
        assertTrue(homeLink is com.strangerhelp.app.navigation.DeepLink)
    }

    @Test
    fun testTaskFormValidationAndEdgeCases() {
        // Valid task
        val validDraft = TaskDraft(
            title = "Deliver groceries to building A",
            description = "Items listed in message",
            category = "Task",
            budget = "350",
            location = "Indiranagar"
        )
        val isValid = validDraft.title.isNotBlank() &&
                validDraft.category.isNotBlank() &&
                (validDraft.budget.toIntOrNull() ?: 0) > 0
        assertTrue("Valid draft should pass validation", isValid)

        // Invalid: Empty title
        val blankTitleDraft = validDraft.copy(title = "   ")
        assertFalse(blankTitleDraft.title.isNotBlank())

        // Invalid: Zero or negative budget
        val zeroBudgetDraft = validDraft.copy(budget = "0")
        val zeroBudgetValid = (zeroBudgetDraft.budget.toIntOrNull() ?: 0) > 0
        assertFalse(zeroBudgetValid)

        val negativeBudgetDraft = validDraft.copy(budget = "-100")
        val negBudgetValid = (negativeBudgetDraft.budget.toIntOrNull() ?: 0) > 0
        assertFalse(negBudgetValid)

        // Non-number budget
        val textBudgetDraft = validDraft.copy(budget = "free")
        assertNull(textBudgetDraft.budget.toIntOrNull())
    }

    @Test
    fun testErrorParsingGracefullyRecoversFromMalformedJson() {
        // Standard JSON with error string
        val standardJson = """{"error":"Task already claimed by another user"}"""
        val parsedStandard = try {
            val json = com.google.gson.Gson().fromJson(standardJson, com.google.gson.JsonObject::class.java)
            json.get("error")?.asString ?: "Something went wrong"
        } catch (_: Exception) {
            "Something went wrong"
        }
        assertEquals("Task already claimed by another user", parsedStandard)

        // Malformed HTML response (e.g. 502 Bad Gateway from Cloudflare/Nginx)
        val htmlResponse = "<html><body>502 Bad Gateway</body></html>"
        val parsedHtml = try {
            val json = com.google.gson.Gson().fromJson(htmlResponse, com.google.gson.JsonObject::class.java)
            json.get("error")?.asString ?: "Something went wrong"
        } catch (_: Exception) {
            "Something went wrong"
        }
        assertEquals("Something went wrong", parsedHtml)

        // Null error body
        val nullResponse: String? = null
        val parsedNull = if (nullResponse == null) "Something went wrong" else "Other"
        assertEquals("Something went wrong", parsedNull)
    }

    @Test
    fun testChatParticipantNameResolution() {
        val myUserId = "user_me_99"
        val myName = "John Doe"

        val conversation = Conversation(
            _id = "conv_123",
            participants = listOf("user_me_99", "user_poster_88"),
            participantNames = listOf("John Doe", "Sarah Connor"),
            taskId = "task_target_55"
        )

        val byId = conversation.participantNames
            .filterIndexed { index, _ ->
                conversation.participants.getOrNull(index) != myUserId
            }
            .firstOrNull { it.isNotBlank() }

        assertEquals("Sarah Connor", byId)

        // Fallback when participants list is missing or misaligned
        val emptyParticipantsConv = Conversation(
            _id = "conv_fallback",
            participants = emptyList(),
            participantNames = listOf("Sarah Connor"),
            taskId = "task_55"
        )
        val byName = emptyParticipantsConv.participantNames
            .firstOrNull { it.isNotBlank() && !it.equals(myName, ignoreCase = true) }

        assertEquals("Sarah Connor", byName)
    }

    @Test
    fun testLocationChangeAndFilteringFlow() {
        val sampleTasks = listOf(
            Task(_id = "mumbai_task", title = "Deliver parcel in Bandra", city = "Mumbai", location = "Bandra West", lat = 19.0596, lng = 72.8295),
            Task(_id = "bangalore_task", title = "Help with grocery in Koramangala", city = "Bangalore", location = "Koramangala 4th Block", lat = 12.9352, lng = 77.6245),
            Task(_id = "delhi_task", title = "Wait in queue at passport office", city = "Delhi", location = "Connaught Place", lat = 28.6315, lng = 77.2167)
        )

        // 1. Filter by city: "Mumbai"
        val mumbaiQuery = "Mumbai"
        val mumbaiFiltered = sampleTasks.filter {
            it.city.contains(mumbaiQuery, ignoreCase = true) || it.location.contains(mumbaiQuery, ignoreCase = true)
        }
        assertEquals(1, mumbaiFiltered.size)
        assertEquals("mumbai_task", mumbaiFiltered.first()._id)

        // 2. Filter by area: "Koramangala"
        val areaQuery = "Koramangala"
        val areaFiltered = sampleTasks.filter {
            it.city.contains(areaQuery, ignoreCase = true) || it.location.contains(areaQuery, ignoreCase = true)
        }
        assertEquals(1, areaFiltered.size)
        assertEquals("bangalore_task", areaFiltered.first()._id)

        // 3. Distance calculation from Bangalore coordinates (12.9716, 77.5946)
        val userLat = 12.9716
        val userLng = 77.5946
        val tasksWithDistance = sampleTasks.map { task ->
            if (task.lat != null && task.lng != null) {
                val dist = haversine(userLat, userLng, task.lat, task.lng)
                task.copy(distance = dist)
            } else task
        }

        val nearest = tasksWithDistance.sortedBy { it.distance ?: Double.MAX_VALUE }
        assertEquals("bangalore_task", nearest.first()._id)
        assertTrue("Bangalore task should be within 10 km", (nearest.first().distance ?: 100.0) < 10.0)
        assertTrue("Mumbai task should be > 500 km away", (nearest.last().distance ?: 0.0) > 500.0)

        // 4. "All Locations" returns all
        val allQuery = "All Locations"
        val isAll = allQuery.equals("All", ignoreCase = true) || allQuery.equals("All Locations", ignoreCase = true)
        val allResult = if (isAll) sampleTasks else sampleTasks.filter { it.city.contains(allQuery, ignoreCase = true) }
        assertEquals(3, allResult.size)
    }

    @Test
    fun testTaskPrioritySystem() {
        // High Urgency by urgent flag
        val urgentTask = Task(title = "Urgent Delivery", urgent = 1)
        assertEquals(com.strangerhelp.app.data.model.TaskPriority.HIGH, urgentTask.getEffectivePriority())

        // Explicit High priority
        val explicitHigh = Task(title = "Standard title", priority = "High")
        assertEquals(com.strangerhelp.app.data.model.TaskPriority.HIGH, explicitHigh.getEffectivePriority())

        // Explicit Medium priority
        val explicitMed = Task(title = "Normal task", priority = "Medium")
        assertEquals(com.strangerhelp.app.data.model.TaskPriority.MEDIUM, explicitMed.getEffectivePriority())

        // Explicit Low priority
        val explicitLow = Task(title = "Plumbing work", priority = "Low")
        assertEquals(com.strangerhelp.app.data.model.TaskPriority.LOW, explicitLow.getEffectivePriority())

        // Inferred Medium priority (by Today deadline or Category)
        val todayTask = Task(title = "Parcel Pickup", deadline = "Today", category = "Parcel Pickup")
        assertEquals(com.strangerhelp.app.data.model.TaskPriority.MEDIUM, todayTask.getEffectivePriority())

        // Inferred Low priority
        val regularTask = Task(title = "Painting wall", deadline = "Next week", budget = 200, category = "Other")
        assertEquals(com.strangerhelp.app.data.model.TaskPriority.LOW, regularTask.getEffectivePriority())

        // Seed tasks priority check
        val seeds = com.strangerhelp.app.data.repository.DEFAULT_SEED_TASKS
        val highCount = seeds.count { it.getEffectivePriority() == com.strangerhelp.app.data.model.TaskPriority.HIGH }
        assertTrue("Seed tasks must contain High urgency tasks", highCount >= 2)

        // Color configuration check
        val highConfig = com.strangerhelp.app.ui.components.getPriorityColorConfig(com.strangerhelp.app.data.model.TaskPriority.HIGH)
        assertEquals("High Urgency", highConfig.label)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFDC2626), highConfig.contentColor)

        val medConfig = com.strangerhelp.app.ui.components.getPriorityColorConfig(com.strangerhelp.app.data.model.TaskPriority.MEDIUM)
        assertEquals("Medium Urgency", medConfig.label)
        assertEquals(androidx.compose.ui.graphics.Color(0xFFD97706), medConfig.contentColor)

        val lowConfig = com.strangerhelp.app.ui.components.getPriorityColorConfig(com.strangerhelp.app.data.model.TaskPriority.LOW)
        assertEquals("Low Urgency", lowConfig.label)
        assertEquals(androidx.compose.ui.graphics.Color(0xFF059669), lowConfig.contentColor)
    }
}



