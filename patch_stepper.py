import sys

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "r") as f:
    content = f.read()

stepper_composable = """
@Composable
fun TaskLifecycleStepper(task: Task, currentUserId: String?) {
    val isOwner = currentUserId == task.posterId
    val isClaimer = currentUserId == task.claimedBy
    
    // Determine the current step index (0 to 3)
    val currentStep = when {
        task.status == "completed" -> 3
        task.completionStatus == "pending" -> 2
        task.status == "claimed" || task.status == "in_progress" -> 1
        else -> 0
    }
    
    // Define the step labels based on perspective
    val steps = if (isOwner) {
        listOf("Open", "In Progress", "Review", "Completed")
    } else if (isClaimer) {
        listOf("Claimed", "Working", "Submitted", "Completed")
    } else {
        listOf("Open", "Claimed", "Verifying", "Completed")
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Task Lifecycle",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PrimaryDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, label ->
                    val isCompleted = index < currentStep
                    val isCurrent = index == currentStep
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCompleted -> TrustColor
                                        isCurrent -> AccentOrange
                                        else -> SurfaceVariantColor
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            } else if (isCurrent) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Label
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent || isCompleted) PrimaryDark else MutedText,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                    
                    // Line separator (except for last item)
                    if (index < steps.size - 1) {
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .height(2.dp)
                                .background(if (isCompleted) TrustColor else SurfaceVariantColor)
                        )
                    }
                }
            }
            
            // Helpful description of current state
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusText = when {
                    isOwner && currentStep == 0 -> "Task is open. Waiting for someone to request to claim it."
                    isOwner && currentStep == 1 -> "A helper is currently working on this task."
                    isOwner && currentStep == 2 -> "The helper has submitted proof. Please review and accept/reject it."
                    isOwner && currentStep == 3 -> "Task is completed and verified. Don't forget to leave a review!"
                    
                    isClaimer && currentStep == 0 -> "Your request was approved. You can now start working."
                    isClaimer && currentStep == 1 -> "You are actively working on this task. Submit proof when done."
                    isClaimer && currentStep == 2 -> "Proof submitted! Waiting for the poster to verify it."
                    isClaimer && currentStep == 3 -> "Great job! Task is fully completed."
                    
                    currentStep == 0 -> "This task is open and available to claim."
                    currentStep == 1 -> "Someone is currently working on this task."
                    currentStep == 2 -> "This task is currently being verified."
                    currentStep == 3 -> "This task has been successfully completed."
                    else -> ""
                }
                
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("ℹ️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(statusText, fontSize = 12.sp, color = Color(0xFF4D4D4D))
                }
            }
        }
    }
}
"""

if "fun TaskLifecycleStepper(" not in content:
    content += "\n" + stepper_composable

target_insert = """                    // 3. Info Grid
                    item { InfoGrid(t) }"""
                    
replacement = """                    // 3. Info Grid
                    item { InfoGrid(t) }
                    
                    // 3.5. Task Lifecycle Stepper
                    item { 
                        Spacer(Modifier.height(16.dp))
                        TaskLifecycleStepper(t, currentUser?.id) 
                    }"""

if "TaskLifecycleStepper(t, currentUser?.id)" not in content:
    content = content.replace(target_insert, replacement)
    
with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt", "w") as f:
    f.write(content)

print("Injected TaskLifecycleStepper successfully.")
