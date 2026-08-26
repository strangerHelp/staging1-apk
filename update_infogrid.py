import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

old_infogrid = """
fun InfoGrid(task: Task) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox("💰 Budget", "₹${task.budget}", Modifier.weight(1f))
        InfoBox("📅 Deadline", task.deadline, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox("📍 Location", task.location.split(",").firstOrNull() ?: task.location, Modifier.weight(1f))
        InfoBox("🕐 Posted", "Recently", Modifier.weight(1f))
    }
}
"""

new_infogrid = """
fun InfoGrid(task: Task) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox("💰 Budget", "₹${task.budget}", Modifier.weight(1f))
        InfoBox("📅 Deadline", task.deadline, Modifier.weight(1f))
    }
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoBox("📍 Location", task.location.split(",").firstOrNull() ?: task.location, Modifier.weight(1f))
        InfoBox("🕐 Posted", com.strangerhelp.app.utils.TimeUtils.getTimeAgo(task.createdAt), Modifier.weight(1f))
    }
}
"""

if "com.strangerhelp.app.utils.TimeUtils.getTimeAgo" not in content:
    content = content.replace(old_infogrid.strip(), new_infogrid.strip())
    
with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(content)
