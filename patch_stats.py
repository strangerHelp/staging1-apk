import re

file_path = "app/src/main/java/com/strangerhelp/app/ui/screens/feed/FeedScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

# Update StatsRow signature
old_stats_row = """@Composable
fun StatsRow(stats: UserStats, city: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(icon = Icons.Outlined.AddCircleOutline, iconTint = Color(0xFF8B5A2B), value = stats.tasksPosted.toString(), label = "Posted")
        StatCard(icon = Icons.Outlined.CheckBox, iconTint = Color(0xFFFF9800), value = stats.tasksClaimed.toString(), label = "Claimed")
        StatCard(icon = Icons.Outlined.CheckCircle, iconTint = PrimaryText, value = stats.tasksCompleted.toString(), label = "Done")
        StatCard(icon = Icons.Outlined.LocationCity, iconTint = PrimaryText, value = city.take(3), label = "City")
    }
}"""

new_stats_row = """@Composable
fun StatsRow(
    stats: UserStats, 
    city: String,
    onPostedClick: () -> Unit = {},
    onClaimedClick: () -> Unit = {},
    onCompletedClick: () -> Unit = {},
    onCityClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(icon = Icons.Outlined.AddCircleOutline, iconTint = Color(0xFF8B5A2B), value = stats.tasksPosted.toString(), label = "Posted", onClick = onPostedClick)
        StatCard(icon = Icons.Outlined.CheckBox, iconTint = Color(0xFFFF9800), value = stats.tasksClaimed.toString(), label = "Claimed", onClick = onClaimedClick)
        StatCard(icon = Icons.Outlined.CheckCircle, iconTint = PrimaryText, value = stats.tasksCompleted.toString(), label = "Done", onClick = onCompletedClick)
        StatCard(icon = Icons.Outlined.LocationCity, iconTint = PrimaryText, value = city.take(3), label = "City", onClick = onCityClick)
    }
}"""

content = content.replace(old_stats_row, new_stats_row)

# Update StatCard signature
old_stat_card = """@Composable
fun RowScope.StatCard(icon: ImageVector, iconTint: Color, value: String, label: String) {
    Card(
        modifier = Modifier.animateContentSize().weight(1f),"""

new_stat_card = """@Composable
fun RowScope.StatCard(icon: ImageVector, iconTint: Color, value: String, label: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.animateContentSize().weight(1f).clickable { onClick() },"""

content = content.replace(old_stat_card, new_stat_card)

with open(file_path, "w") as f:
    f.write(content)
