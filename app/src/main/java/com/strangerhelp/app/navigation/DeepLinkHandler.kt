package com.strangerhelp.app.navigation

import android.content.Intent
import android.net.Uri
import com.strangerhelp.app.utils.AppLogger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Central deep link parser.
 *
 * Accepts EITHER:
 *   - A full https URL:  https://strangerhelp.com/tasks/abc123
 *   - A relative link:   /tasks/abc123
 *   - An app link:       strangerhelp://tasks/abc123
 *   - A chat link:       /chat/conv123
 *   - A profile link:    /user/xyz
 *
 * Emits a strongly-typed DeepLink that the root navigator collects.
 */
sealed class DeepLink {
    data class TaskDetail(val taskId: String) : DeepLink()
    data class ChatDetail(val conversationId: String) : DeepLink()
    data class UserProfile(val userId: String) : DeepLink()
    data class MeetDetail(val meetId: String) : DeepLink()
    data class AskDetail(val questionId: String) : DeepLink()
    data class ResetPassword(val token: String) : DeepLink()
    data class VerifyEmail(val token: String) : DeepLink()
    object TasksList : DeepLink()
    object ChatList : DeepLink()
    object AskList : DeepLink()
    object Notifications : DeepLink()
    object Home : DeepLink()
}

object DeepLinkHandler {

    private const val TAG = "DeepLinkHandler"

    private val _links = MutableSharedFlow<DeepLink>(
        replay = 1,
        extraBufferCapacity = 8
    )
    val links: SharedFlow<DeepLink> = _links.asSharedFlow()

    /** Call this from MainActivity.onCreate and onNewIntent. */
    fun handleIntent(intent: Intent?) {
        val uriString = intent?.dataString
        if (!uriString.isNullOrBlank()) {
            val parsed = parse(uriString) ?: return
            AppLogger.d(TAG, "Emitting deep link from uri: $parsed")
            _links.tryEmit(parsed)
            return
        }
        val deepLinkExtra = intent?.getStringExtra("deep_link")
        if (!deepLinkExtra.isNullOrBlank()) {
            handleLink(deepLinkExtra)
        }
    }

    /** Call this from notification click payloads that carry a raw link. */
    fun handleLink(link: String) {
        if (link.isBlank()) return
        val parsed = parse(link) ?: return
        AppLogger.d(TAG, "Emitting deep link from payload: $parsed")
        _links.tryEmit(parsed)
    }

    fun parse(uri: Uri): DeepLink? = parse(uri.toString())

    fun parse(rawLink: String): DeepLink? {
        val trimmed = rawLink.trim()
        if (trimmed.isEmpty()) return DeepLink.Home

        // Extract query string if any
        val queryPart = if (trimmed.contains('?')) trimmed.substringAfter('?').substringBefore('#') else ""
        val withoutQuery = trimmed.substringBefore('?').substringBefore('#')

        // Strip scheme and host
        val pathOnly = when {
            withoutQuery.startsWith("https://") -> withoutQuery.removePrefix("https://").substringAfter('/', "")
            withoutQuery.startsWith("http://") -> withoutQuery.removePrefix("http://").substringAfter('/', "")
            withoutQuery.startsWith("strangerhelp://") -> {
                val afterScheme = withoutQuery.removePrefix("strangerhelp://").trimStart('/')
                if (afterScheme.startsWith("strangerhelp.com/")) {
                    afterScheme.removePrefix("strangerhelp.com/")
                } else {
                    afterScheme
                }
            }
            withoutQuery.startsWith("/") -> withoutQuery.trimStart('/')
            else -> withoutQuery
        }

        val cleanPath = pathOnly.trimStart('/').trimEnd('/')
        val segments = cleanPath.split('/').filter { it.isNotBlank() }

        AppLogger.d(TAG, "Parsed path=$cleanPath segments=$segments query=$queryPart")

        if (segments.isEmpty()) return DeepLink.Home

        fun getQueryParam(key: String): String? {
            if (queryPart.isEmpty()) return null
            val pairs = queryPart.split('&')
            for (pair in pairs) {
                val parts = pair.split('=', limit = 2)
                if (parts[0].equals(key, ignoreCase = true)) {
                    return parts.getOrNull(1)
                }
            }
            return null
        }

        return when (segments[0].lowercase()) {
            "tasks", "task", "task_detail" -> {
                val id = segments.getOrNull(1)
                val cleanId = id?.trim()?.removeSurrounding("\"")
                if (cleanId.isNullOrBlank()) DeepLink.TasksList else DeepLink.TaskDetail(cleanId)
            }
            "chat", "messages" -> {
                val id = segments.getOrNull(1)
                val cleanId = id?.trim()?.removeSurrounding("\"")
                if (cleanId.isNullOrBlank()) DeepLink.ChatList else DeepLink.ChatDetail(cleanId)
            }
            "user", "users", "profile" -> {
                val id = segments.getOrNull(1)
                if (id.isNullOrBlank()) DeepLink.Home else DeepLink.UserProfile(id)
            }
            "meets", "meet", "meet_detail" -> {
                val id = segments.getOrNull(1)
                if (id.isNullOrBlank()) DeepLink.Home else DeepLink.MeetDetail(id)
            }
            "ask_detail", "ask" -> {
                val id = segments.getOrNull(1)
                if (id.isNullOrBlank()) DeepLink.AskList else DeepLink.AskDetail(id)
            }
            "notifications", "notification" -> {
                DeepLink.Notifications
            }
            "reset-password" -> {
                val token = getQueryParam("token") ?: return DeepLink.Home
                DeepLink.ResetPassword(token)
            }
            "verify-email" -> {
                val token = getQueryParam("token") ?: return DeepLink.Home
                DeepLink.VerifyEmail(token)
            }
            "api" -> {
                if (segments.getOrNull(1) == "auth" && segments.getOrNull(2) == "verify-email") {
                    val token = getQueryParam("token") ?: return DeepLink.Home
                    DeepLink.VerifyEmail(token)
                } else if (segments.getOrNull(1) == "tasks") {
                    val id = segments.getOrNull(2)
                    if (id.isNullOrBlank()) DeepLink.TasksList else DeepLink.TaskDetail(id)
                } else {
                    DeepLink.Home
                }
            }
            else -> DeepLink.Home
        }
    }
}
