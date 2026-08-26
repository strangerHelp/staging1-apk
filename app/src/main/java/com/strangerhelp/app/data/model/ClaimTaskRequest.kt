package com.strangerhelp.app.data.model

import com.google.gson.annotations.SerializedName

data class ClaimTaskRequest(
    val action: String = "claim",
    @SerializedName("offered_budget") val offeredBudget: Int? = null,
    val message: String? = null
)

data class ClaimResponse(
    val ok: Boolean?,
    val status: String?,              
    @SerializedName("conversationId") val conversationId: String?,
    @SerializedName("conversation_id") val conversation_id: String?,
    val error: String?
)
