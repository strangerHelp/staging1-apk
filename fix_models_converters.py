import re

with open('app/src/main/java/com/strangerhelp/app/data/model/Models.kt', 'r') as f:
    content = f.read()

claim_request_code = """
import com.google.gson.annotations.SerializedName

data class ClaimRequest(
    val id: String = "",
    @SerializedName("requester_id") val requesterId: String = "",
    @SerializedName("requester_name") val requesterName: String = "",
    val status: String = "pending",
    @SerializedName("offered_budget") val offeredBudget: Int? = null,
    val message: String? = null,
    @SerializedName("created_at") val createdAt: String = ""
)
"""

if "data class ClaimRequest" not in content:
    content = content.replace("import androidx.room.PrimaryKey", "import androidx.room.PrimaryKey\n" + claim_request_code)

# Add completionStatus and claimRequests to Task
task_replacement = """    val visibility: String = "public",
    val inviteCode: String? = null,
    val completionStatus: String? = null,
    val claimRequests: List<ClaimRequest>? = emptyList()"""

content = content.replace('    val inviteCode: String? = null,', task_replacement)

with open('app/src/main/java/com/strangerhelp/app/data/model/Models.kt', 'w') as f:
    f.write(content)

# Update Converters.kt
with open('app/src/main/java/com/strangerhelp/app/data/local/Converters.kt', 'r') as f:
    converters_content = f.read()

new_converters = """    @TypeConverter
    fun fromClaimRequestList(value: List<com.strangerhelp.app.data.model.ClaimRequest>?): String {
        if (value == null) return "[]"
        return gson.toJson(value)
    }

    @TypeConverter
    fun toClaimRequestList(value: String?): List<com.strangerhelp.app.data.model.ClaimRequest> {
        if (value.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<com.strangerhelp.app.data.model.ClaimRequest>>() {}.type
        return gson.fromJson(value, listType)
    }
}"""

if "fromClaimRequestList" not in converters_content:
    converters_content = converters_content.replace("}", new_converters)

with open('app/src/main/java/com/strangerhelp/app/data/local/Converters.kt', 'w') as f:
    f.write(converters_content)
