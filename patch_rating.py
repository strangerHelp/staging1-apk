import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskDetailScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''                    if (t.posterId == user.id) {
                        val prefs = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("strangerhelp_prefs", android.content.Context.MODE_PRIVATE)
                        val ratingKey = "rating_${t._id}"
                        var rating by remember { mutableStateOf(prefs.getInt(ratingKey, 0)) }
                        
                        if (rating == 0) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Rate Helper's Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (i in 1..5) {
                                        IconButton(onClick = { 
                                            rating = i
                                            prefs.edit().putInt(ratingKey, i).apply()
                                        }, modifier = Modifier.size(40.dp)) {
                                            Icon(
                                                Icons.Outlined.Star,
                                                contentDescription = "Rate $i stars",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("You rated the helper:", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                                    for (i in 1..5) {
                                        Icon(
                                            if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = if (i <= rating) androidx.compose.ui.graphics.Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }'''

replacement = '''                    if (t.posterId == user.id) {
                        val prefs = androidx.compose.ui.platform.LocalContext.current.getSharedPreferences("strangerhelp_prefs", android.content.Context.MODE_PRIVATE)
                        val ratingKey = "rating_${t._id}"
                        var rating by remember { mutableStateOf(prefs.getInt(ratingKey, 0)) }
                        var showRatingDialog by remember { mutableStateOf(false) }
                        
                        if (rating == 0) {
                            Button(
                                onClick = { showRatingDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanDeep)
                            ) {
                                Text("Rate Helper's Performance", fontWeight = FontWeight.SemiBold)
                            }
                            
                            if (showRatingDialog) {
                                var tempRating by remember { mutableStateOf(0) }
                                AlertDialog(
                                    onDismissRequest = { showRatingDialog = false },
                                    title = { Text("Rate Helper") },
                                    text = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("How was the helper's performance?", style = MaterialTheme.typography.bodyMedium)
                                            Spacer(Modifier.height(16.dp))
                                            com.strangerhelp.app.ui.components.StarRating(
                                                rating = tempRating,
                                                onRatingChange = { tempRating = it }
                                            )
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(
                                            onClick = {
                                                if (tempRating > 0) {
                                                    rating = tempRating
                                                    prefs.edit().putInt(ratingKey, tempRating).apply()
                                                    showRatingDialog = false
                                                }
                                            },
                                            enabled = tempRating > 0
                                        ) {
                                            Text("Submit")
                                        }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showRatingDialog = false }) {
                                            Text("Cancel")
                                        }
                                    }
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("You rated the helper:", style = MaterialTheme.typography.bodyMedium, color = Muted)
                                Spacer(Modifier.height(4.dp))
                                com.strangerhelp.app.ui.components.StarRating(
                                    rating = rating,
                                    onRatingChange = {},
                                    readOnly = true,
                                    starSize = 24.dp
                                )
                            }
                        }
                    }'''

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched.")
else:
    print("Not found.")
