package com.strangerhelp.app.navigation

import android.content.Intent
import android.net.Uri
import android.util.Log
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
    data class ResetPassword(val token: String) : DeepLink()
    data class VerifyEmail(val token: String) : DeepLink()
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
        val uri = intent?.data
        if (uri != null) {
            val parsed = parse(uri) ?: return
            Log.d(TAG, "Emitting deep link from uri: $parsed")
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
        val normalizedUri = if (link.startsWith("/") || (!link.contains("://") && !link.startsWith("strangerhelp:"))) {
            Uri.parse("https://strangerhelp.com/" + link.trimStart('/'))
        } else {
            Uri.parse(link)
        }
        val parsed = parse(normalizedUri) ?: return
        Log.d(TAG, "Emitting deep link from payload: $parsed")
        _links.tryEmit(parsed)
    }

    private fun parse(uri: Uri): DeepLink? {
        // Normalise: strip scheme/host if full URL, keep path only
        val rawPath = when {
            uri.scheme == "https" || uri.scheme == "http" -> uri.path ?: return null
            uri.scheme == "strangerhelp" -> "/" + (uri.host ?: "") + (uri.path ?: "")
            else -> uri.toString()
        }

        val path = rawPath.trimStart('/').trimEnd('/')
        val segments = path.split('/').filter { it.isNotEmpty() }

        Log.d(TAG, "Parsed path=$path segments=$segments query=${uri.query}")

        if (segments.isEmpty()) return DeepLink.Home

        return when (segments[0].lowercase()) {
            "tasks", "task", "task_detail" -> {
                val id = segments.getOrNull(1)?.substringBefore('?') ?: return DeepLink.Home
                val cleanId = id.trim().removeSurrounding("\"")
                if (cleanId.isBlank()) DeepLink.Home else DeepLink.TaskDetail(cleanId)
            }
            "chat", "messages" -> {
                val id = segments.getOrNull(1)?.substringBefore('?') ?: return DeepLink.Home
                if (id.isBlank()) DeepLink.Home else DeepLink.ChatDetail(id)
            }
            "user", "users", "profile" -> {
                val id = segments.getOrNull(1)?.substringBefore('?') ?: return DeepLink.Home
                if (id.isBlank()) DeepLink.Home else DeepLink.UserProfile(id)
            }
            "meets", "meet" -> {
                val id = segments.getOrNull(1)?.substringBefore('?') ?: return DeepLink.Home
                if (id.isBlank()) DeepLink.Home else DeepLink.MeetDetail(id)
            }
            "reset-password" -> {
                val token = uri.getQueryParameter("token") ?: return DeepLink.Home
                DeepLink.ResetPassword(token)
            }
            "verify-email" -> {
                val token = uri.getQueryParameter("token") ?: return DeepLink.Home
                DeepLink.VerifyEmail(token)
            }
            "api" -> {
                if (segments.getOrNull(1) == "auth" && segments.getOrNull(2) == "verify-email") {
                    val token = uri.getQueryParameter("token") ?: return DeepLink.Home
                    DeepLink.VerifyEmail(token)
                } else if (segments.getOrNull(1) == "tasks") {
                    val id = segments.getOrNull(2)?.substringBefore('?') ?: return DeepLink.Home
                    DeepLink.TaskDetail(id)
                } else {
                    DeepLink.Home
                }
            }
            else -> DeepLink.Home
        }
    }
}
