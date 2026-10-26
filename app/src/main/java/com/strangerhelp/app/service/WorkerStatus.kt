package com.strangerhelp.app.service

/**
 * The lifecycle stages a helper passes through while working.
 * Each state maps to a specific notification title + text.
 */
enum class WorkerStatus(val title: String, val text: String, val showStop: Boolean) {
    ONLINE(
        title = "You are online",
        text = "Searching for nearby tasks...",
        showStop = true
    ),
    TRACKING(
        title = "Sharing your location",
        text = "The task poster can see your position.",
        showStop = true
    ),
    TASK_CLAIMED(
        title = "Task claimed",
        text = "You have claimed a task. Head to the location.",
        showStop = true
    ),
    PROOF_PENDING(
        title = "Proof under review",
        text = "Waiting for the poster to accept your proof.",
        showStop = false
    ),
    OFFLINE(
        title = "You are offline",
        text = "Tap to go online and start earning.",
        showStop = false
    )
}
