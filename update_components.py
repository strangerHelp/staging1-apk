import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatComponents.kt", "r") as f:
    content = f.read()

new_input_bar = """@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttach: (File) -> Unit,
    isSending: Boolean = false,
    maxLength: Int = 5000
) {
    val context = LocalContext.current
    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val file = compressAndSaveImage(context, uri)
                file?.let { onAttach(it) }
            } catch (e: Exception) {
            }
        }
    }

    val charCount = text.length
    val isNearLimit = charCount > maxLength - 500
    val isOverLimit = charCount > maxLength

    Surface(
        color = BackgroundLight,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { pickImage.launch("image/*") },
                    modifier = Modifier.size(40.dp),
                    enabled = !isSending
                ) {
                    Text("📎", fontSize = 20.sp, color = if (isSending) Muted else Color.Black)
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = {
                        Text(
                            text = "Type a message...",
                            fontSize = 15.sp,
                            color = Muted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp, max = 120.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Hairline,
                        unfocusedBorderColor = Hairline,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 4,
                    isError = isOverLimit,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { if (!isOverLimit && text.isNotBlank()) onSend() }
                    ),
                    singleLine = false
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank() && !isOverLimit && !isSending,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (text.isNotBlank() && !isOverLimit && !isSending) Color.Black else Hairline,
                            CircleShape
                        )
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            if (isNearLimit) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${charCount}/${maxLength}",
                        fontSize = 10.sp,
                        color = if (isOverLimit) Color.Red else Muted,
                        modifier = Modifier.padding(end = 12.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}"""

content = re.sub(r'@Composable\nfun ChatInputBar\(.*?(?=\n}\n*$|\Z)', new_input_bar, content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/chat/ChatComponents.kt", "w") as f:
    f.write(content)
