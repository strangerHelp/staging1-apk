package com.strangerhelp.app.ui.screens.post

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strangerhelp.app.StrangerHelpApp
import com.strangerhelp.app.data.api.ApiClient
import com.strangerhelp.app.data.model.TaskDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

data class PostTaskUiState(
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
    val selectedFileBytes: List<ByteArray> = emptyList(),
    val voiceNoteBytes: ByteArray? = null,
    val isPosting: Boolean = false,
    val isDraftRestored: Boolean = false,
    val draftSavedTimestamp: Long? = null,
    val errorMessage: String? = null
) {
    fun hasContent(): Boolean {
        return title.isNotBlank() ||
                description.isNotBlank() ||
                category.isNotBlank() ||
                budget.isNotBlank() ||
                location.isNotBlank() ||
                selectedFileBytes.isNotEmpty() ||
                voiceNoteBytes != null
    }

    val isValidForSubmission: Boolean
        get() = title.isNotBlank() &&
                category.isNotBlank() &&
                budget.isNotBlank() &&
                location.isNotBlank() &&
                !isPosting
}

class PostTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val taskDraftDao = StrangerHelpApp.instance.database.taskDraftDao()
    private val draftDir: File
        get() = File(getApplication<Application>().filesDir, "task_drafts").apply { mkdirs() }

    private val _uiState = MutableStateFlow(PostTaskUiState())
    val uiState: StateFlow<PostTaskUiState> = _uiState.asStateFlow()

    init {
        loadDraft()
    }

    fun updateTitle(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun updateCategory(value: String) {
        _uiState.update { it.copy(category = value) }
    }

    fun updateBudget(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(budget = filtered) }
    }

    fun updateLocation(addr: String, lat: Double?, lng: Double?) {
        _uiState.update {
            it.copy(
                location = addr,
                taskLat = lat?.toString() ?: it.taskLat,
                taskLng = lng?.toString() ?: it.taskLng
            )
        }
    }

    fun updateDeadline(value: String) {
        _uiState.update { it.copy(deadline = value) }
    }

    fun updateAnonymous(value: Boolean) {
        _uiState.update { it.copy(isAnonymous = value) }
    }

    fun updateMaxClaimers(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { it.copy(maxClaimers = filtered) }
    }

    fun updateUrgent(value: Boolean) {
        _uiState.update { it.copy(isUrgent = value) }
    }

    fun updatePrivate(value: Boolean) {
        _uiState.update { it.copy(isPrivate = value) }
    }

    fun setSelectedFiles(files: List<ByteArray>) {
        _uiState.update { it.copy(selectedFileBytes = files) }
    }

    fun removePhoto(index: Int) {
        _uiState.update { current ->
            val updated = current.selectedFileBytes.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            current.copy(selectedFileBytes = updated)
        }
    }

    fun setVoiceNote(bytes: ByteArray?) {
        _uiState.update { it.copy(voiceNoteBytes = bytes) }
    }

    fun dismissDraftBanner() {
        _uiState.update { it.copy(isDraftRestored = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Loads saved draft from Room and local file cache if it exists.
     */
    fun loadDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val draft = taskDraftDao.getDraft()
                if (draft != null && draft.hasContent()) {
                    // Restore photos from disk
                    val loadedPhotos = mutableListOf<ByteArray>()
                    draft.photoPaths.forEach { path ->
                        val file = File(path)
                        if (file.exists()) {
                            try {
                                loadedPhotos.add(file.readBytes())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // Restore voice note from disk
                    val loadedVoice: ByteArray? = draft.voiceNotePath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            try {
                                file.readBytes()
                            } catch (e: Exception) {
                                null
                            }
                        } else null
                    }

                    _uiState.update {
                        it.copy(
                            title = draft.title,
                            description = draft.description,
                            category = draft.category,
                            budget = draft.budget,
                            location = draft.location,
                            taskLat = draft.taskLat,
                            taskLng = draft.taskLng,
                            deadline = draft.deadline,
                            isAnonymous = draft.isAnonymous,
                            maxClaimers = draft.maxClaimers,
                            isUrgent = draft.isUrgent,
                            isPrivate = draft.isPrivate,
                            selectedFileBytes = loadedPhotos,
                            voiceNoteBytes = loadedVoice,
                            isDraftRestored = true,
                            draftSavedTimestamp = draft.updatedAt
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Saves the draft to Room and local storage.
     */
    fun saveDraft(showToast: Boolean = false, onComplete: (() -> Unit)? = null) {
        val currentState = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (currentState.hasContent()) {
                    // Clear existing files in draftDir first
                    draftDir.listFiles()?.forEach { it.delete() }

                    // Save photo bytes
                    val savedPhotoPaths = mutableListOf<String>()
                    currentState.selectedFileBytes.forEachIndexed { index, bytes ->
                        val photoFile = File(draftDir, "draft_photo_$index.jpg")
                        FileOutputStream(photoFile).use { it.write(bytes) }
                        savedPhotoPaths.add(photoFile.absolutePath)
                    }

                    // Save voice note bytes
                    var savedVoicePath: String? = null
                    currentState.voiceNoteBytes?.let { bytes ->
                        val voiceFile = File(draftDir, "draft_voice.webm")
                        FileOutputStream(voiceFile).use { it.write(bytes) }
                        savedVoicePath = voiceFile.absolutePath
                    }

                    val draft = TaskDraft(
                        id = "active_draft",
                        title = currentState.title,
                        description = currentState.description,
                        category = currentState.category,
                        budget = currentState.budget,
                        location = currentState.location,
                        taskLat = currentState.taskLat,
                        taskLng = currentState.taskLng,
                        deadline = currentState.deadline,
                        isAnonymous = currentState.isAnonymous,
                        maxClaimers = currentState.maxClaimers,
                        isUrgent = currentState.isUrgent,
                        isPrivate = currentState.isPrivate,
                        photoPaths = savedPhotoPaths,
                        voiceNotePath = savedVoicePath,
                        updatedAt = System.currentTimeMillis()
                    )

                    taskDraftDao.insertDraft(draft)

                    if (showToast) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                getApplication(),
                                "Task saved as draft. You can resume anytime!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    onComplete?.invoke()
                }
            }
        }
    }

    /**
     * Intercepts back navigation: saves draft if user has entered content, then triggers navigation.
     */
    fun handleBack(onNavigateBack: () -> Unit) {
        if (_uiState.value.hasContent()) {
            saveDraft(showToast = true) {
                onNavigateBack()
            }
        } else {
            onNavigateBack()
        }
    }

    /**
     * Discards the saved draft and resets the form to empty.
     */
    fun discardDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                taskDraftDao.deleteDraft()
                draftDir.listFiles()?.forEach { it.delete() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                _uiState.update {
                    PostTaskUiState()
                }
                Toast.makeText(
                    getApplication(),
                    "Draft discarded",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Submits the task to the backend API.
     * Clears the draft if submission succeeds.
     */
    fun submitTask(
        context: Context,
        onSuccess: (inviteCode: String?, taskId: String?) -> Unit
    ) {
        val state = _uiState.value
        if (!state.isValidForSubmission) return

        _uiState.update { it.copy(isPosting = true, errorMessage = null) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("title", state.title)
                    .addFormDataPart("description", state.description)
                    .addFormDataPart("category", state.category)
                    .addFormDataPart("budget", state.budget)
                    .addFormDataPart("deadline", state.deadline)
                    .addFormDataPart("location", state.location)
                    .addFormDataPart("anonymous", if (state.isAnonymous) "true" else "false")
                    .addFormDataPart("urgent", if (state.isUrgent) "true" else "false")
                    .addFormDataPart("visibility", if (state.isPrivate) "private" else "public")

                if (state.category == "Event / Group Work") {
                    builder.addFormDataPart("max_claimers", state.maxClaimers)
                }

                if (state.taskLat.isNotEmpty() && state.taskLat != "0.0") {
                    builder.addFormDataPart("lat", state.taskLat)
                }
                if (state.taskLng.isNotEmpty() && state.taskLng != "0.0") {
                    builder.addFormDataPart("lng", state.taskLng)
                }

                state.selectedFileBytes.forEachIndexed { i, bytes ->
                    builder.addFormDataPart(
                        "files", "attachment_$i.jpg",
                        bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                }

                state.voiceNoteBytes?.let {
                    builder.addFormDataPart(
                        "files", "voice-note.webm",
                        it.toRequestBody("audio/webm".toMediaTypeOrNull())
                    )
                }

                val res = ApiClient.api.postTask(builder.build())

                if (res.isSuccessful) {
                    val body = res.body()
                    val inviteCode = body?.get("inviteCode")?.toString()
                    val taskId = body?.get("id")?.toString()

                    // Clean up draft upon successful post
                    taskDraftDao.deleteDraft()
                    draftDir.listFiles()?.forEach { it.delete() }

                    withContext(Dispatchers.Main) {
                        _uiState.update { PostTaskUiState() }
                        onSuccess(inviteCode, taskId)
                    }
                } else {
                    val err = "Failed to post task: ${res.code()}"
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isPosting = false, errorMessage = err) }
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isPosting = false,
                            errorMessage = e.message ?: "Network error"
                        )
                    }
                    Toast.makeText(context, "Failed to post task. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
