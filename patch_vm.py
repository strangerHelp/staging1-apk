import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/ask/AskViewModel.kt"
with open(file_path, "r") as f:
    content = f.read()

# Remove Hilt/Dagger imports
content = re.sub(r'import dagger\.hilt\.android\.lifecycle\.HiltViewModel\n', '', content)
content = re.sub(r'import javax\.inject\.Inject\n', '', content)
content = re.sub(r'@HiltViewModel\n', '', content)
content = re.sub(r'@Inject constructor', '', content)

# Add Factory
factory_code = """

class AskViewModelFactory(
    private val askRepository: AskRepository,
    private val authRepository: AuthRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AskViewModel(askRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
"""
content += factory_code

with open(file_path, "w") as f:
    f.write(content)
