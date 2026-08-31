import re

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "r") as f:
    content = f.read()

# I will replace `val completionProof: List<String> = emptyList(),` with:
replacement = """    val completionProof: List<String> = emptyList(),
    @com.google.gson.annotations.SerializedName("completion_status") val completionStatus: String = "",
    @com.google.gson.annotations.SerializedName("rejection_reason") val rejectionReason: String? = null,"""

content = content.replace("    val completionProof: List<String> = emptyList(),", replacement)

with open("app/src/main/java/com/strangerhelp/app/data/model/Models.kt", "w") as f:
    f.write(content)

