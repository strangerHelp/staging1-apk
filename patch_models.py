import re

file_path = "app/src/main/java/com/strangerhelp/app/data/model/Models.kt"
with open(file_path, "r") as f:
    content = f.read()

old_question = """data class Question(
    @SerializedName(value = "_id", alternate = ["id"]) val _id: String = "",
    val text: String = "",
    val category: String = "",
    val location: String = "",
    val votes: Int = 0,
    val anonymous: Int = 0,
    val posterId: String = "",
    val createdAt: String = "",
)"""

new_question = """data class Question(
    @SerializedName(value = "_id", alternate = ["id"]) val id: String = "",
    val text: String = "",
    val category: String = "",
    val location: String? = null,
    val anonymous: Int = 0,
    @SerializedName("author_id") val authorId: String = "",
    @SerializedName("author_name") val authorName: String = "",
    val votes: Int = 0,
    @SerializedName("answerCount") val answerCount: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    val answers: List<Answer> = emptyList()
)

data class Answer(
    val id: String = "",
    @SerializedName("question_id") val questionId: String = "",
    @SerializedName("author_id") val authorId: String = "",
    @SerializedName("author_name") val authorName: String = "",
    val text: String = "",
    val votes: Int = 0,
    @SerializedName("created_at") val createdAt: String = ""
)

data class QuestionRequest(
    val text: String,
    val category: String,
    val location: String?,
    val anonymous: Boolean
)"""

content = content.replace(old_question, new_question)

with open(file_path, "w") as f:
    f.write(content)
