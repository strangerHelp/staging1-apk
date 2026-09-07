package com.strangerhelp.app.ui.screens.tasks

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.strangerhelp.app.R
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.OnPrimary
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.utils.GpsCameraHelper
import com.strangerhelp.app.utils.GpsStampHelper
import java.io.File
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsCameraScreen(
    taskId: String,
    viewModel: GpsCameraViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val locationHelper = remember { GpsCameraHelper(context) }

    val location by viewModel.location.collectAsStateWithLifecycle()
    val placeName by viewModel.placeName.collectAsStateWithLifecycle()
    val isLocationReady by viewModel.isLocationReady.collectAsStateWithLifecycle()
    val isCheckingLocation by viewModel.isCheckingLocation.collectAsStateWithLifecycle()
    
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    val logo = remember {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_logo_brand_image)
        if (drawable != null) {
            val bitmap = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth.takeIf { it > 0 } ?: 100,
                drawable.intrinsicHeight.takeIf { it > 0 } ?: 100,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } else {
            android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.checkLocation()
        } else {
            // Handle denied permission
        }
    }

    LaunchedEffect(Unit) {
        if (!locationHelper.hasLocationPermission()) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.checkLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Camera") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isCheckingLocation -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Getting your location...",
                            fontSize = 14.sp,
                            color = Muted
                        )
                    }
                }
            }

            !isLocationReady -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("📍", fontSize = 48.sp)
                        Text(
                            "Location Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your GPS location is stamped onto the proof photo to verify you completed the task at the right place.",
                            fontSize = 14.sp,
                            color = Muted,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                if (!locationHelper.hasLocationPermission()) {
                                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                } else {
                                    viewModel.retryLocation()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text("Enable Location & Open Camera")
                        }
                        TextButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }

            location != null && isLocationReady -> {
                if (capturedBitmap == null) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        CameraXView(
                            location = location!!,
                            placeName = placeName,
                            onCapture = { bitmap ->
                                val stamped = GpsStampHelper.stampProof(
                                    bitmap,
                                    location!!.latitude,
                                    location!!.longitude,
                                    placeName,
                                    logo ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                                )
                                capturedBitmap = stamped
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.elevatedCardElevation()
                        ) {
                            BitmapImage(
                                bitmap = capturedBitmap!!,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    capturedBitmap = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(26.dp)
                            ) {
                                Text("Retake")
                            }
                            Button(
                                onClick = {
                                    val bytes = GpsStampHelper.compressStampedProof(capturedBitmap!!)
                                    viewModel.submitProof(
                                        taskId = taskId,
                                        proofBytes = bytes
                                    ) { success ->
                                        if (success) {
                                            navController.popBackStack()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(2f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary
                                ),
                                shape = RoundedCornerShape(26.dp),
                                enabled = !isSubmitting
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = OnPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Submit Proof", color = OnPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BitmapImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Stamped proof",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

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

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.7f)
        ) {
            Text(
                text = "📍 $placeName",
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .size(72.dp)
                .clickable {
                    if (!isCapturing) {
                        isCapturing = true
                        captureImage(imageCapture, { bmp -> 
                            isCapturing = false
                            onCapture(bmp)
                        }, {
                            isCapturing = false
                        }, context)
                    }
                }
                .background(Color.White, CircleShape)
                .border(4.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCapturing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.Black,
                    strokeWidth = 3.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }

        Text(
            text = "Point at the completed task location",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 128.dp)
        )
    }
}

private fun captureImage(
    imageCapture: ImageCapture?,
    onCapture: (Bitmap) -> Unit,
    onError: () -> Unit,
    context: Context
) {
    if (imageCapture == null) {
        onError()
        return
    }

    val outputDirectory = context.cacheDir
    val photoFile = File(outputDirectory, "captured_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (bitmap != null) {
                    onCapture(bitmap)
                } else {
                    onError()
                }
                photoFile.delete()
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                onError()
            }
        }
    )
}
