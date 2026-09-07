import os

file_path = "app/src/main/java/com/strangerhelp/app/ui/components/StrangerHelpLogo.kt"
with open(file_path, "r") as f:
    content = f.read()

target = """@Composable
fun StrangerHelpLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = "Logo",
            tint = Primary,
            modifier = Modifier.size(size)
        )
        Box(
            modifier = Modifier
                .padding(bottom = size * 0.125f)
                .size(size * 0.42f)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Handshake,
                contentDescription = null,
                tint = Saffron,
                modifier = Modifier.size(size * 0.32f)
            )
        }
    }
}"""

replacement = """@Composable
fun StrangerHelpLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Icon(
        painter = androidx.compose.ui.res.painterResource(id = com.strangerhelp.app.R.drawable.ic_logo_brand),
        contentDescription = "StrangerHelp Logo",
        tint = Color.Unspecified, // Important: don't tint to preserve the original colors
        modifier = modifier.size(size)
    )
}"""

if "@Composable\nfun StrangerHelpLogo" in content:
    content = content[:content.find("@Composable\nfun StrangerHelpLogo")] + replacement
    with open(file_path, "w") as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
