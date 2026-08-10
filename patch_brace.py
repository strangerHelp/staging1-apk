import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/tasks/TasksScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''                    if (loadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
        
        PullToRefreshContainer('''

replacement = '''                    if (loadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
            }
        }
        
        PullToRefreshContainer('''

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
    print("Patched.")
else:
    print("Target not found.")
