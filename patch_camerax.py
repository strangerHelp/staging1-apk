import re

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraScreen.kt", "r") as f:
    content = f.read()

new_camerax_view = """
@Composable
fun CameraXView(
    location: Location,
    placeName: String,
    onCapture: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
"""

old_camerax_view_pattern = r"@Composable\s*fun CameraXView\(\s*location: Location,\s*placeName: String,\s*onCapture: \(Bitmap\) -> Unit\s*\)\s*\{.*?\}\s*,\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.background\(Color\.Black\)\s*\)"
content = re.sub(old_camerax_view_pattern, new_camerax_view.strip(), content, flags=re.DOTALL)

with open("app/src/main/java/com/strangerhelp/app/ui/screens/tasks/GpsCameraScreen.kt", "w") as f:
    f.write(content)
