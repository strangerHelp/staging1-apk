sed -i '/LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {/,$d' app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt
cat << 'EOF2' >> app/src/main/java/com/strangerhelp/app/ui/screens/pulse/PulseScreen.kt
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (helpRequests.isEmpty()) {
                        Text("No open tasks nearby.", style = MaterialTheme.typography.bodySmall, color = Muted)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(helpRequests) { request ->
                                Column {
                                    Text(request.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("${request.location} • ₹150", style = MaterialTheme.typography.bodySmall, color = Link)
                                    HorizontalDivider(Modifier.padding(top = 8.dp), color = Hairline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
EOF2
