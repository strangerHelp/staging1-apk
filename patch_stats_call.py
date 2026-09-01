import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

old_call = """                StatsRow(stats = stats, city = user?.city ?: "Ban")"""

new_call = """                StatsRow(
                    stats = stats, 
                    city = user?.city ?: "Ban",
                    onPostedClick = { navController.navigate("my_tasks?filter=posted") },
                    onClaimedClick = { navController.navigate("my_tasks?filter=claimed") },
                    onCompletedClick = { navController.navigate("my_tasks?filter=completed") },
                    onCityClick = { navController.navigate("search_location") }
                )"""

content = content.replace(old_call, new_call)

with open(file_path, "w") as f:
    f.write(content)
