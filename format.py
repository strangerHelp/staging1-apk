import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    text = f.read()

# Remove all bad insertions
text = text.replace("            }                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n            TaskUiState.HELPER_PROOF_REJECTED -> {",
"            }\n            TaskUiState.HELPER_PROOF_REJECTED -> {")

text = text.replace("            }                Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n            TaskUiState.HELPER_COMPLETED -> {",
"            }\n            TaskUiState.HELPER_COMPLETED -> {")

# Insert them correctly
# Pending
pending_str = """            TaskUiState.HELPER_PROOF_PENDING -> {
                Card(colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.12f)), shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳", fontSize = 24.sp)
                        Spacer(Modifier.width(16.dp))
                        Text("Proof submitted — waiting for poster to review", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)
                    }
                }
"""
pending_fixed = pending_str + """                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
"""
text = text.replace(pending_str, pending_fixed)

# Rejected
rejected_str = """                Button(
                    onClick = { navController.navigate("gps_camera/${task._id}") },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("📸 Retake Photo & Resubmit")
                }
"""
rejected_fixed = rejected_str + """                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
"""
text = text.replace(rejected_str, rejected_fixed)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(text)

