import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''                                if (msg.type == "location") {'''
replacement = '''                                
                            // Timestamp
                            val timeStr = try {
                                if (msg.createdAt.isNotBlank() && msg.createdAt.contains("T")) {
                                    val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                                    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                    val cleanTime = if (msg.createdAt.contains(".")) msg.createdAt.substringBefore(".") else msg.createdAt.replace("Z", "")
                                    val date = format.parse(cleanTime)
                                    if (date != null) {
                                        val outFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                        outFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Kolkata")
                                        outFormat.format(date)
                                    } else {
                                        msg.createdAt.substringAfter("T").take(5)
                                    }
                                } else ""
                            } catch (e: Exception) {
                                ""
                            }
                            if (timeStr.isNotBlank()) {
                                Text(
                                    text = timeStr,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                                )
                            }
                                
                                if (msg.type == "location") {'''

content = content.replace(target, replacement)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print("Updated ChatDetailScreen timestamp")
