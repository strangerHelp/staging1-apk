with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "r") as f:
    content = f.read()

import re
pattern = r"// Mock date header\s*item \{\s*Box\(modifier = Modifier\.fillMaxWidth\(\)\.padding\(vertical = 16\.dp\), contentAlignment = Alignment\.Center\) \{\s*Surface\(\s*color = Hairline,\s*shape = RoundedCornerShape\(16\.dp\),\s*\) \{\s*Text\(\s*text = \"Today\",\s*fontSize = 12\.sp,\s*color = Body,\s*modifier = Modifier\.padding\(horizontal = 16\.dp, vertical = 6\.dp\)\s*\)\s*\}\s*\}\s*\}"

content = re.sub(pattern, "", content)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatDetailScreen.kt", "w") as f:
    f.write(content)
