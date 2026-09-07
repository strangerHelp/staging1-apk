package com.strangerhelp.app.data.repository

import com.google.gson.JsonObject
import com.strangerhelp.app.data.api.StrangerHelpApi
import com.strangerhelp.app.data.model.Question
import com.strangerhelp.app.data.model.QuestionRequest
import retrofit2.Response

class AskRepository constructor(
    private val api: StrangerHelpApi
) {
    suspend fun getQuestions(category: String = "All"): Response<List<Question>> {
        val query = if (category != "All") mapOf("category" to category) else emptyMap()
        return api.getQuestions(query)
    }

    suspend fun getQuestion(questionId: String): Response<Question> {
        return api.getQuestion(questionId)
    }

    suspend fun postQuestion(request: QuestionRequest): Response<JsonObject> {
        val body = mapOf(
            "text" to request.text,
            "category" to request.category,
            "location" to request.location,
            "anonymous" to request.anonymous
        ).filterValues { it != null }
        return api.postQuestion(body)
    }

    suspend fun postAnswer(questionId: String, text: String): Response<JsonObject> {
        return api.postAnswer(questionId, mapOf("action" to "answer", "text" to text))
    }

    suspend fun voteAnswer(questionId: String, vote: String): Response<JsonObject> {
        return api.voteAnswer(questionId, mapOf("action" to "vote", "vote" to vote))
    }

    suspend fun deleteQuestion(questionId: String): Response<JsonObject> {
        return api.deleteQuestion(questionId)
    }
}
