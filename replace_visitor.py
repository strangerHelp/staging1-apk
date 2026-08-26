import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'r') as f:
    content = f.read()

old_visitor = """
fun VisitorActions(onClaim: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { /* Chat */ },
            modifier = Modifier.weight(1f).height(52.dp),
            border = BorderStroke(1.dp, PrimaryDark),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark)
        ) {
            Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Message Poster")
        }
        
        Button(
            onClick = onClaim,
            modifier = Modifier.weight(1f).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
        ) {
            Icon(Icons.Default.BackHand, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Request to Claim")
        }
    }
}
"""

new_visitor = """
fun VisitorActions(
    task: Task?,
    claimStatus: TaskDetailViewModel.ClaimStatus,
    isClaiming: Boolean,
    onClaimClick: () -> Unit,
    onChatClick: () -> Unit
) {
    if (task == null) return

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        when (claimStatus) {
            TaskDetailViewModel.ClaimStatus.NONE -> {
                Button(
                    onClick = onClaimClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !isClaiming,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentOrange
                    )
                ) {
                    if (isClaiming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.BackHand,
                            null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (task.maxClaimers > 1) "Request to Join"
                            else "Request to Claim"
                        )
                    }
                }
            }

            TaskDetailViewModel.ClaimStatus.REQUESTED -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AccentOrange.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏳", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Request Sent — Waiting for Approval",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentOrange
                            )
                            Text(
                                text = "The poster will review your request",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onChatClick,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryDark
                    ),
                    border = BorderStroke(1.dp, PrimaryDark)
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("💬 Message Poster")
                }
            }

            TaskDetailViewModel.ClaimStatus.APPROVED -> {
                // Handled in main when block (HelperActions)
            }

            TaskDetailViewModel.ClaimStatus.REJECTED -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = ErrorColor.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("❌", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Your request was declined",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ErrorColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onClaimClick,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryDark
                            )
                        ) {
                            Text("🔄 Try Again")
                        }
                    }
                }
            }
        }
    }
}
"""

if "fun VisitorActions(onClaim: () -> Unit)" in content:
    content = content.replace(old_visitor.strip(), new_visitor.strip())
else:
    print("WARNING: VisitorActions old text not found")

with open('app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt', 'w') as f:
    f.write(content)
