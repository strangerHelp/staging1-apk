import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt', 'r') as f:
    content = f.read()

new_method = """    fun submitReview(taskId: String, revieweeId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("taskId" to taskId, "revieweeId" to revieweeId, "rating" to rating, "comment" to comment)
                val response = ApiClient.api.postReview(body)
                if (!response.isSuccessful) {
                    _error.value = parseError(response.errorBody()?.string())
                }
            } catch (e: Exception) {
                _error.value = "Failed to submit review"
            }
        }
    }"""

content = re.sub(r'fun submitReview.*?\}', new_method, content, flags=re.DOTALL)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailViewModel.kt', 'w') as f:
    f.write(content)
