import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

p2p_warning = """
        // ⭐ P2P Payment Notice
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            border = BorderStroke(1.dp, Color(0xFFFFB300)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFFF8F00), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("⚠️ Direct Payment — Escrow Coming Soon", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                    Text("Payment of ₹${task.budget} is made directly by the poster to the helper via UPI after completion. StrangerHelp does not hold or process payments.", fontSize = 12.sp, color = Color(0xFFE65100), lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        
        ClaimButton(
"""

content = content.replace("        ClaimButton(\n", p2p_warning)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(content)
