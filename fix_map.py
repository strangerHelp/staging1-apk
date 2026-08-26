import re

with open('app/src/main/java/com/strangerhelp/app/ui/screens/path/PathSetupScreen.kt', 'r') as f:
    content = f.read()

# Add imports
if "import androidx.compose.foundation.Canvas" not in content:
    content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.background\nimport androidx.compose.foundation.Canvas\nimport androidx.compose.ui.geometry.Offset")

# Replace AsyncImage
old_image = """AsyncImage(
                            model = "https://i.imgur.com/uR1dZ67.png", // map placeholder
                            contentDescription = "Map",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )"""
                        
new_canvas = """Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F6F5))) {
                            val dotSpacing = 16.dp.toPx()
                            for (x in 0..size.width.toInt() step dotSpacing.toInt()) {
                                for (y in 0..size.height.toInt() step dotSpacing.toInt()) {
                                    drawCircle(color = Color(0xFFE0E0E0), radius = 2.5f, center = Offset(x.toFloat(), y.toFloat()))
                                }
                            }
                            
                            val startX = size.width * 0.25f
                            val startY = size.height * 0.75f
                            val endX = size.width * 0.75f
                            val endY = size.height * 0.25f
                            
                            drawLine(
                                color = PrimaryColor.copy(alpha = 0.6f),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 12f,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            
                            drawCircle(color = Color.White, radius = 20f, center = Offset(startX, startY))
                            drawCircle(color = Color.Black, radius = 10f, center = Offset(startX, startY))
                            
                            drawCircle(color = Color.White, radius = 20f, center = Offset(endX, endY))
                            drawCircle(color = PrimaryColor, radius = 10f, center = Offset(endX, endY))
                        }"""

content = content.replace(old_image, new_canvas)

with open('app/src/main/java/com/strangerhelp/app/ui/screens/path/PathSetupScreen.kt', 'w') as f:
    f.write(content)
