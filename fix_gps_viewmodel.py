import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraViewModel.kt", "r") as f:
    text = f.read()

text = text.replace("""class GpsCameraViewModelFactory(
    private val taskRepository: TaskRepository,
    private val locationHelper: GpsCameraHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GpsCameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GpsCameraViewModel(taskRepository, locationHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}""", """class GpsCameraViewModelFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = com.strangerhelp.app.data.api.ApiClient.api
        val repository = TaskRepository(api)
        val locationHelper = GpsCameraHelper(context)
        if (modelClass.isAssignableFrom(GpsCameraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GpsCameraViewModel(repository, locationHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}""")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraViewModel.kt", "w") as f:
    f.write(text)
