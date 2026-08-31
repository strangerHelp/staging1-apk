import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "r") as f:
    content = f.read()

new_gallery = """@Composable
fun ProofDisplayComponent(proofImages: List<String>) {
    var selectedImage by remember { mutableStateOf<String?>(null) }
    if (proofImages.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = TrustColor, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Submitted Proof",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Primary
                    )
                }
                Text(
                    text = "Images include embedded location and timestamp metadata.",
                    fontSize = 12.sp,
                    color = Body
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(proofImages) { imageUrl ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .clickable { selectedImage = imageUrl }
                        ) {
                            coil.compose.AsyncImage(
                                model = imageUrl,
                                contentDescription = "Proof image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            // Watermark badge overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "WATERMARKED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    selectedImage?.let { url ->
        FullScreenImageDialog(
            imageUrl = url,
            onDismiss = { selectedImage = null }
        )
    }
}
"""

# I will find the @Composable fun HelperProofGallery... string and replace to the end of file
index = content.find("@Composable\nfun HelperProofGallery")
if index != -1:
    content = content[:index] + new_gallery
else:
    content += "\n" + new_gallery

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/PosterComponents.kt", "w") as f:
    f.write(content)

