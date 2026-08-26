import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

old_poster_info = """
fun PosterInfoCard(posterId: String, posterName: String, posterVerified: Boolean, anonymous: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, HairlineColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceVariantColor),
                contentAlignment = Alignment.Center
            ) {
                Text(if (anonymous == 1) "?" else posterName.firstOrNull()?.uppercase() ?: "U", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (anonymous == 1) "Anonymous" else posterName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                    if (posterVerified && anonymous != 1) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = TrustColor, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, null, tint = AccentOrange, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New Poster", fontSize = 13.sp, color = MutedText)
                }
            }
        }
    }
}
"""

new_poster_info = """
fun PosterInfoCard(task: Task) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, HairlineColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceVariantColor),
                contentAlignment = Alignment.Center
            ) {
                Text(if (task.anonymous == 1) "?" else task.posterName.firstOrNull()?.uppercase() ?: "U", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (task.anonymous == 1) "Anonymous" else task.posterName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryDark)
                    if (task.posterVerified && task.anonymous != 1) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.Verified, contentDescription = "Verified", tint = TrustColor, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("• ${com.strangerhelp.app.utils.TimeUtils.getTimeAgo(task.createdAt)}", fontSize = 13.sp, color = MutedText)
                }
            }
        }
    }
}
"""

content = content.replace(old_poster_info.strip(), new_poster_info.strip())
content = content.replace("PosterInfoCard(t.posterId, t.posterName, t.posterVerified, t.anonymous)", "PosterInfoCard(t)")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(content)
