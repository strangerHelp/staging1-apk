import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/meets/MeetsScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target = '''                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(modifier = Modifier.padding(end = 8.dp)) {
                        for (i in 0 until minOf(3, attendeesToUse)) {
                            Surface(
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset(x = -(8 * i).dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.padding(4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (attendeesToUse > 3) {
                        Surface(
                            modifier = Modifier.offset(x = (-24).dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "+${attendeesToUse - 3}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }'''

replacement = '''                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        for (i in 0 until minOf(3, attendeesToUse)) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.padding(4.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (attendeesToUse > 3) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "+${attendeesToUse - 3}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }'''

if target in content:
    content = content.replace(target, replacement)
    with open(path, 'w') as f:
        f.write(content)
        print("Patched!")
else:
    print("Target not found")
