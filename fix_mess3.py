with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "r") as f:
    text = f.read()

text = text.replace(
"""                        Text("Proof submitted — waiting for poster to review", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)            }
                }
            }
""",
"""                        Text("Proof submitted — waiting for poster to review", fontSize = 14.sp, color = AccentOrange, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(16.dp))
                HelperProofGallery(task.completionProof)
            }
"""
)

text = text.replace(
"""                        Text("Please resubmit", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000))            }
                }
""",
"""                        Text("Please resubmit", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFFEE0000))
                    }
                }
"""
)

text = text.replace(
"""                        Text("Task completed!", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFF10B981), fontWeight = FontWeight.Bold)            }
                }
""",
"""                        Text("Task completed!", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
"""
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TaskActionSection.kt", "w") as f:
    f.write(text)

