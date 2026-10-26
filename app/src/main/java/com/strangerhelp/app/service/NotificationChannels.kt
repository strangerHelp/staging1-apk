package com.strangerhelp.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Creates all notification channels used by StrangerHelp.
 * Call [createAll] once from Application.onCreate().
 */
object NotificationChannels {

    const val DEFAULT     = "strangerhelp_default"     // General
    const val TASKS       = "strangerhelp_tasks"       // Claim / approve / reject
    const val MESSAGES    = "strangerhelp_messages"    // Chat
    const val PROOF       = "strangerhelp_proof"       // Proof submission / review
    const val PROMOTIONS  = "strangerhelp_promotions"  // Marketing (opt-in)

    fun resolve(type: String): String = when (type.lowercase().trim()) {
        "claim_request", "claim_approved", "claim_rejected",
        "task_completed", "task_claimed", "claim_received" -> TASKS

        "new_message", "chat_message", "message" -> MESSAGES

        "proof_submitted", "proof_accepted", "proof_rejected",
        "review_received", "rating_received" -> PROOF

        "promotion", "promo", "tips" -> PROMOTIONS

        "ask_answer", "question_answered", "ask_reply" -> TASKS

        else -> DEFAULT
    }

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                DEFAULT, "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "General updates from StrangerHelp" }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                TASKS, "Task updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Claims, approvals and task status changes" }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                MESSAGES, "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Chat messages between users" }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                PROOF, "Proof & completion",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Proof submissions and reviews" }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                PROMOTIONS, "Promotions & tips",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Offers and product updates" }
        )
    }
}
