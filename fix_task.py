import re

with open('app/src/main/java/com/strangerhelp/app/data/model/Models.kt', 'r') as f:
    content = f.read()

# Replace duplicate maxClaimers
content = content.replace("val maxClaimers: Int = 1,", "")
content = content.replace("val claimedUsers: List<ClaimedUser>? = emptyList(),\\n    val maxClaimers: Int = 1", "val claimedUsers: List<ClaimedUser>? = emptyList(),\\n    val maxClaimers: Int = 1")
# Wait, replacing "val maxClaimers: Int = 1," will remove all of them. I'll just do it with regex to keep only the last one.
