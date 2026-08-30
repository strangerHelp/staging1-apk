import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

p2p_notice = """
        // ⭐ P2P Payment Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFFFF8F00), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("You will pay the helper directly via UPI after task completion. StrangerHelp does not hold or process payments.", fontSize = 12.sp, color = Color(0xFFE65100), lineHeight = 16.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
"""

content = content.replace("        Button(\n", p2p_notice)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'w') as f:
    f.write(content)
