import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/ReviewViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("import dagger.hilt.android.lifecycle.HiltViewModel", "")
content = content.replace("import javax.inject.Inject", "")
content = content.replace("@HiltViewModel\n", "")

old_decl = """class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository
) : ViewModel() {"""

new_decl = """class ReviewViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepository(com.strangerhelp.app.data.api.ApiClient.api),
    private val authRepository: AuthRepository = AuthRepository(com.strangerhelp.app.data.api.ApiClient.api)
) : ViewModel() {"""

content = content.replace(old_decl, new_decl)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/ReviewViewModel.kt", "w") as f:
    f.write(content)
