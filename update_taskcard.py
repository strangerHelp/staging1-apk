import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt', 'r') as f:
    content = f.read()

old_footer = """
            // Footer
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Avatar
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (task.anonymous == 1) "?" else (task.posterName.firstOrNull()?.uppercase() ?: "U"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDark
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                Text(
                    text = if (task.anonymous == 1) "Anonymous" else task.posterName.takeIf { it.isNotBlank() } ?: "User",
                    fontSize = 14.sp,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.weight(1f))
                
                Text("• ", color = MutedText, fontSize = 12.sp)
                val statusColor = when(task.status) {
                    "open" -> PrimaryDark
                    "claimed" -> AccentOrange
                    "completed" -> CyanDeep
                    else -> MutedText
                }
                Text(task.status.capitalize(), color = statusColor, fontSize = 12.sp)
            }
"""

new_footer = """
            // Bottom Row: Poster + Posted Time + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Poster info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (task.anonymous == 1) "?" else (task.posterName.firstOrNull()?.uppercase() ?: "U"),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDark
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))

                    // Name
                    Text(
                        text = if (task.anonymous == 1) "Anonymous" else task.posterName.takeIf { it.isNotBlank() } ?: "User",
                        fontSize = 12.sp,
                        color = PrimaryDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Verified badge
                    if (task.posterVerified && task.anonymous == 0) {
                        Text(
                            text = " ✓",
                            fontSize = 10.sp,
                            color = TrustGreen
                        )
                    }
                }

                // Right: Posted Time
                Text(
                    text = com.strangerhelp.app.utils.TimeUtils.getTimeAgo(task.createdAt),
                    fontSize = 10.sp,
                    color = MutedText,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Status Badge
                val statusColor = when(task.status) {
                    "open" -> PrimaryDark
                    "claimed" -> AccentOrange
                    "completed" -> CyanDeep
                    else -> MutedText
                }
                Text(task.status.capitalize(), color = statusColor, fontSize = 12.sp)
            }
"""

if "Bottom Row: Poster + Posted Time" not in content:
    content = content.replace(old_footer.strip(), new_footer.strip())
    
with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt', 'w') as f:
    f.write(content)
