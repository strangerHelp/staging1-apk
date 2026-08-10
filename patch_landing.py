import os

path = 'app/src/main/java/com/strangerhelp/app/ui/screens/LandingScreen.kt'
with open(path, 'r') as f:
    content = f.read()

target_logo = '''            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Logo",
                    tint = TextDark,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "strangerhelp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )
            }'''

replacement_logo = '''            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Logo",
                        tint = DarkNavy,
                        modifier = Modifier.size(32.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Handshake,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(16.dp).padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                androidx.compose.ui.text.buildAnnotatedString {
                    withStyle(androidx.compose.ui.text.SpanStyle(color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append("stranger")
                    }
                    withStyle(androidx.compose.ui.text.SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                        append("help")
                    }
                }.let { text ->
                    Text(text = text)
                }
            }'''

content = content.replace(target_logo, replacement_logo)

target_bottom_logo = '''                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Logo", tint = TextDark, modifier = Modifier.size(20.dp))
                    Text("strangerhelp", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                }'''

replacement_bottom_logo = '''                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocationOn, contentDescription = "Logo", tint = DarkNavy, modifier = Modifier.size(24.dp))
                        Icon(Icons.Outlined.Handshake, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(12.dp).padding(bottom = 2.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    androidx.compose.ui.text.buildAnnotatedString {
                        withStyle(androidx.compose.ui.text.SpanStyle(color = DarkNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                            append("stranger")
                        }
                        withStyle(androidx.compose.ui.text.SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                            append("help")
                        }
                    }.let { text ->
                        Text(text = text)
                    }
                }'''

content = content.replace(target_bottom_logo, replacement_bottom_logo)

# Fix missing imports
import_insert = "import androidx.compose.ui.text.withStyle\nimport androidx.compose.ui.text.buildAnnotatedString\n"
if "import androidx.compose.ui.text.withStyle" not in content:
    content = content.replace("import androidx.compose.ui.text.style.TextAlign", import_insert + "import androidx.compose.ui.text.style.TextAlign")

with open(path, 'w') as f:
    f.write(content)
print("Patched logos in LandingScreen")
