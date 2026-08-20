import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/EditProfileScreen.kt", "r") as f:
    content = f.read()

old_textfield = """@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier.height(48.dp),
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingText: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, letterSpacing = 1.sp, color = if (isError) Color(0xFFD32F2F) else Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
            if (trailingText != null) {
                Text(trailingText, fontSize = 10.sp, color = Color.Gray)
            }
        }
        val borderColor = if (isError) Color(0xFFD32F2F) else Color(0xFFE0E0E0)
        Row(
            modifier = Modifier.fillMaxWidth().then(modifier).background(if (isError) Color(0xFFFFEBEE) else Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, borderColor, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = if (singleLine) 0.dp else 12.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            BasicTextFieldWrapper(value = value, onValueChange = onValueChange, modifier = Modifier.weight(1f), singleLine = singleLine)
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
            }
        }
        if (isError && errorMessage != null) {
            Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}"""

new_textfield = """@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier.height(48.dp),
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingText: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    topRightText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 10.sp, letterSpacing = 1.sp, color = if (isError) Color(0xFFD32F2F) else Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))
            if (topRightText != null) {
                Text(topRightText, fontSize = 10.sp, color = Color.Gray)
            }
        }
        val borderColor = if (isError) Color(0xFFD32F2F) else Color(0xFFE0E0E0)
        Row(
            modifier = Modifier.fillMaxWidth().then(modifier).background(if (isError) Color(0xFFFFEBEE) else Color(0xFFF5F5F5), RoundedCornerShape(8.dp)).border(1.dp, borderColor, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = if (singleLine) 0.dp else 12.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            BasicTextFieldWrapper(value = value, onValueChange = onValueChange, modifier = Modifier.weight(1f), singleLine = singleLine)
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (isError && errorMessage != null) {
                Text(errorMessage, color = Color(0xFFD32F2F), fontSize = 10.sp)
            } else {
                Spacer(Modifier.width(1.dp))
            }
            if (trailingText != null) {
                Text(trailingText, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}"""

content = content.replace(old_textfield, new_textfield)
content = content.replace(
    """trailingText = "${bio.length}/5000\"""",
    """topRightText = "${bio.length}/5000\""""
)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/profile/EditProfileScreen.kt", "w") as f:
    f.write(content)
