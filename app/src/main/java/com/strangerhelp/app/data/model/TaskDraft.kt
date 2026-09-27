package com.strangerhelp.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_drafts")
data class TaskDraft(
    @PrimaryKey
    val id: String = "active_draft",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val budget: String = "",
    val location: String = "",
    val taskLat: String = "",
    val taskLng: String = "",
    val deadline: String = "Today",
    val isAnonymous: Boolean = false,
    val maxClaimers: String = "2",
    val isUrgent: Boolean = false,
    val isPrivate: Boolean = false,
    val photoPaths: List<String> = emptyList(),
    val voiceNotePath: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun hasContent(): Boolean {
        return title.isNotBlank() ||
               description.isNotBlank() ||
               category.isNotBlank() ||
               budget.isNotBlank() ||
               location.isNotBlank() ||
               photoPaths.isNotEmpty() ||
               voiceNotePath != null
    }
}
