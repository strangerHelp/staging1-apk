with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "r") as f:
    content = f.read()

content = content.replace('?.firstOrNull() ?: "Ravi Kumar"', '?.firstOrNull() ?: "User"')

import re
pattern = r"\} else if \(messages\.isEmpty\(\)\) \{\s*// Injecting mock message to mimic the exact screen.*?\s*\} else \{"
replacement = """} else if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("💬", fontSize = 32.sp)
                            Text(
                                text = "No messages yet",
                                fontSize = 14.sp,
                                color = Muted,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = "Say hello to start the conversation!",
                                fontSize = 12.sp,
                                color = Muted
                            )
                        }
                    }
                } else {"""

content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "w") as f:
    f.write(content)
