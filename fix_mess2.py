with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    text = f.read()

text = text.replace("                        Spacer(Modifier.height(16.dp))\n                HelperProofGallery(task.completionProof)\n            }", "            }")

# Let's insert them safely at known string points
text = text.replace("""                    }
                }
            }
            TaskUiState.HELPER_PROOF_REJECTED -> {""", """                    }
                }
                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            }
            TaskUiState.HELPER_PROOF_REJECTED -> {""")

text = text.replace("""                ) {
                    Text("📸 Retake Photo & Resubmit")
                }
            }
            TaskUiState.HELPER_COMPLETED -> {""", """                ) {
                    Text("📸 Retake Photo & Resubmit")
                }
                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            }
            TaskUiState.HELPER_COMPLETED -> {""")

text = text.replace("""                Button(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                    Text("⭐ Leave a Review")
                }
            }
            
            TaskUiState.POSTER_WAITING -> {""", """                Button(onClick = onReview, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)) {
                    Text("⭐ Leave a Review")
                }
                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            }
            
            TaskUiState.POSTER_WAITING -> {""")

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(text)

