import re
with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'r') as f:
    content = f.read()

# find index using a regex with flexible whitespace
pattern = r'Spacer\(Modifier\.height\(16\.dp\)\)\s*Card\(\s*shape\s*=\s*RoundedCornerShape\(12\.dp\),\s*colors\s*=\s*CardDefaults\.cardColors\(containerColor\s*=\s*if\s*\(isUrgent\)\s*MaterialTheme\.colorScheme\.errorContainer\s*else\s*MaterialTheme\.colorScheme\.surfaceVariant\)\s*\)\s*\{\s*Row\(Modifier\.fillMaxWidth\(\)\.padding\(16\.dp\),\s*verticalAlignment\s*=\s*Alignment\.CenterVertically\)\s*\{\s*Column\(Modifier\.weight\(1f\)\)\s*\{\s*Text\("⚡ Urgent",\s*fontWeight\s*=\s*FontWeight\.SemiBold\)\s*Text\("Helpers will prioritize this",\s*style\s*=\s*MaterialTheme\.typography\.bodySmall,\s*color\s*=\s*MaterialTheme\.colorScheme\.onSurfaceVariant\)\s*\}\s*Switch\(checked\s*=\s*isUrgent,\s*onCheckedChange\s*=\s*\{\s*isUrgent\s*=\s*it\s*\}\)\s*\}\s*\}\s*Spacer\(Modifier\.height\(16\.dp\)\)\s*Card\(\s*shape\s*=\s*RoundedCornerShape\(12\.dp\),\s*colors\s*=\s*CardDefaults\.cardColors\(containerColor\s*=\s*MaterialTheme\.colorScheme\.surfaceVariant\)\s*\)\s*\{\s*Row\(Modifier\.fillMaxWidth\(\)\.padding\(16\.dp\),\s*verticalAlignment\s*=\s*Alignment\.CenterVertically\)\s*\{\s*Column\(Modifier\.weight\(1f\)\)\s*\{\s*Text\("🕵️ Anonymous Posting",\s*fontWeight\s*=\s*FontWeight\.SemiBold\)\s*Text\("Hide your name on this task",\s*style\s*=\s*MaterialTheme\.typography\.bodySmall,\s*color\s*=\s*MaterialTheme\.colorScheme\.onSurfaceVariant\)\s*\}\s*Switch\(checked\s*=\s*isAnonymous,\s*onCheckedChange\s*=\s*\{\s*isAnonymous\s*=\s*it\s*\}\)\s*\}\s*Spacer\(Modifier\.height\(16\.dp\)\)\s*Card\(\s*shape\s*=\s*RoundedCornerShape\(12\.dp\),\s*colors\s*=\s*CardDefaults\.cardColors\(containerColor\s*=\s*MaterialTheme\.colorScheme\.surfaceVariant\)\s*\)\s*\{\s*Row\(Modifier\.fillMaxWidth\(\)\.padding\(16\.dp\),\s*verticalAlignment\s*=\s*Alignment\.CenterVertically\)\s*\{\s*Column\(Modifier\.weight\(1f\)\)\s*\{\s*Text\("🔒 Private Task",\s*fontWeight\s*=\s*FontWeight\.SemiBold\)\s*Text\("Only visible via invite link",\s*style\s*=\s*MaterialTheme\.typography\.bodySmall,\s*color\s*=\s*MaterialTheme\.colorScheme\.onSurfaceVariant\)\s*\}\s*Switch\(checked\s*=\s*isPrivate,\s*onCheckedChange\s*=\s*\{\s*isPrivate\s*=\s*it\s*\}\)\s*\}\s*\}\s*\}'

replacement = """Spacer(Modifier.height(16.dp))
        
        val darkSwitchColors = SwitchDefaults.colors(
            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            uncheckedThumbColor = MaterialTheme.colorScheme.outline
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (isUrgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("⚡ Urgent", fontWeight = FontWeight.SemiBold)
                    Text("Helpers will prioritize this", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isUrgent, onCheckedChange = { isUrgent = it }, colors = darkSwitchColors)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🕵️ Anonymous Posting", fontWeight = FontWeight.SemiBold)
                    Text("Hide your name on this task", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isAnonymous, onCheckedChange = { isAnonymous = it }, colors = darkSwitchColors)
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🔒 Private Task", fontWeight = FontWeight.SemiBold)
                    Text("Only visible via invite link", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isPrivate, onCheckedChange = { isPrivate = it }, colors = darkSwitchColors)
            }
        }"""

new_content = re.sub(pattern, replacement, content, count=1)
if new_content != content:
    with open('app/src/main/java/com/strangerhelp/app/ui/screens/post/PostTaskScreen.kt', 'w') as f:
        f.write(new_content)
    print("Patched successfully")
else:
    print("Could not find target")
