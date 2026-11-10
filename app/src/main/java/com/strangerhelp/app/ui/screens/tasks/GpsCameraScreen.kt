package com.strangerhelp.app.ui.screens.tasks

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.strangerhelp.app.service.GpsCameraController
import com.strangerhelp.app.service.ProofStamper
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.Saffron
import com.strangerhelp.app.ui.theme.TrustColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

@Composable
fun GpsCameraScreen(
    taskId: String,
    onSubmitProof: (ByteArray) -> Unit,
    onBack: () -> Unit
) {
    GpsCameraScreen(
        taskId = taskId,
        onSubmitProof = { bytes, onResult ->
            onSubmitProof(bytes)
            onResult(true, null)
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun GpsCameraScreen(
    taskId: String,
    onSubmitProof: (ByteArray, (Boolean, String?) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val executor = remember { Executors.newSingleThreadExecutor() }

    val gps = remember { GpsCameraController(context) }

    // Accompanist permission state for CAMERA
    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    // User feedback states
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedFeedbackDialog by remember { mutableStateOf(false) }
    var hasRequestedPermissionOnce by remember { mutableStateOf(false) }

    // Fallback to ic_logo_brand_image if ic_logo doesn't exist
    val logoBitmap = remember {
        val resId = context.resources.getIdentifier("ic_logo", "drawable", context.packageName).takeIf { it != 0 }
            ?: context.resources.getIdentifier("ic_logo_brand_image", "drawable", context.packageName)

        if (resId != 0) {
            val drawable = ContextCompat.getDrawable(context, resId)
            if (drawable != null) {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth.takeIf { it > 0 } ?: 100,
                    drawable.intrinsicHeight.takeIf { it > 0 } ?: 100,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            } else {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
        } else {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }

    var captured by remember { mutableStateOf<Bitmap?>(null) }
    var capturedLat by remember { mutableStateOf<Double?>(null) }
    var capturedLng by remember { mutableStateOf<Double?>(null) }
    var capturedTimestamp by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isCameraBound by remember { mutableStateOf(false) }
    var cameraBindingError by remember { mutableStateOf<String?>(null) }
    var locationNote by remember { mutableStateOf<String?>(null) }

    // Trigger initial permission flow if needed
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            if (cameraPermissionState.status.shouldShowRationale) {
                showPermissionRationaleDialog = true
            } else {
                hasRequestedPermissionOnce = true
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    // React to permission status changes
    LaunchedEffect(cameraPermissionState.status) {
        if (!cameraPermissionState.status.isGranted && hasRequestedPermissionOnce) {
            // Permission was asked and user denied or dismissed
            if (cameraPermissionState.status.shouldShowRationale) {
                showPermissionRationaleDialog = true
            } else {
                // Permanently denied or denied again
                showPermissionDeniedFeedbackDialog = true
            }
        }
    }

    fun fixOrientation(file: File, bitmap: Bitmap): Bitmap {
        return try {
            val exif = android.media.ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = android.graphics.Matrix()
            when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    fun processAndDisplayBitmap(bmp: Bitmap) {
        scope.launch {
            isProcessing = true
            errorMessage = null
            try {
                val loc = gps.getCurrentLocation()
                val lat = loc?.latitude ?: 0.0
                val lng = loc?.longitude ?: 0.0
                val place = if (loc != null) {
                    gps.reverseGeocode(lat, lng)
                } else {
                    "Location Unavailable"
                }
                locationNote = place
                capturedLat = if (lat != 0.0) lat else null
                capturedLng = if (lng != 0.0) lng else null
                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy • hh:mm a", java.util.Locale.getDefault())
                capturedTimestamp = sdf.format(java.util.Date())

                val stamped = withContext(Dispatchers.IO) {
                    try {
                        ProofStamper.stamp(bmp, lat, lng, place, logoBitmap)
                    } catch (e: Exception) {
                        Log.e("GpsCameraScreen", "Proof stamping failed, using original photo", e)
                        bmp
                    }
                }
                captured = stamped
            } catch (e: Exception) {
                Log.e("GpsCameraScreen", "Error processing photo", e)
                captured = bmp
            } finally {
                isProcessing = false
            }
        }
    }

    // Android Photo Picker (zero permissions required by Google Play policy)
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                errorMessage = null
                try {
                    val bmp = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    if (bmp != null) {
                        processAndDisplayBitmap(bmp)
                    } else {
                        errorMessage = "Could not decode selected photo."
                        isProcessing = false
                    }
                } catch (e: Exception) {
                    Log.e("GpsCameraScreen", "Failed to load gallery photo", e)
                    errorMessage = "Failed to load selected photo: ${e.message}"
                    isProcessing = false
                }
            }
        }
    }

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // System Camera Intent launcher using FileProvider URI for full-resolution photos
    val systemCameraUriLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            val file = tempPhotoFile
            if (file != null && file.exists()) {
                scope.launch {
                    isProcessing = true
                    errorMessage = null
                    try {
                        val bmp = withContext(Dispatchers.IO) {
                            val raw = BitmapFactory.decodeFile(file.absolutePath)
                            if (raw != null) fixOrientation(file, raw) else null
                        }
                        if (bmp != null) {
                            processAndDisplayBitmap(bmp)
                        } else {
                            errorMessage = "Failed to load photo from system camera."
                            isProcessing = false
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error loading photo: ${e.message}"
                        isProcessing = false
                    }
                }
            } else {
                isProcessing = false
            }
        } else {
            isProcessing = false
        }
    }

    // Secondary fallback: thumbnail preview launcher
    val systemCameraPreviewLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            processAndDisplayBitmap(bmp)
        } else {
            isProcessing = false
        }
    }

    fun launchSystemCamera() {
        isProcessing = true
        errorMessage = null
        try {
            val file = File(context.cacheDir, "camera_proof_${System.currentTimeMillis()}.jpg")
            tempPhotoFile = file
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            systemCameraUriLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e("GpsCameraScreen", "FileProvider failed, using preview fallback", e)
            systemCameraPreviewLauncher.launch(null)
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    fun capturePhoto() {
        if (isProcessing) return
        if (!isCameraBound) {
            launchSystemCamera()
            return
        }
        isProcessing = true
        errorMessage = null
        val file = File(
            context.cacheDir,
            "proof_${System.currentTimeMillis()}.jpg"
        )
        val opts = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            opts,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(out: ImageCapture.OutputFileResults) {
                    scope.launch {
                        try {
                            val bmp = withContext(Dispatchers.IO) {
                                val raw = BitmapFactory.decodeFile(file.absolutePath)
                                if (raw != null) fixOrientation(file, raw) else null
                            }
                            if (bmp != null) {
                                processAndDisplayBitmap(bmp)
                            } else {
                                errorMessage = "Failed to load captured photo. Please try System Camera or Gallery."
                                isProcessing = false
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error loading photo: ${e.message}"
                            isProcessing = false
                        }
                    }
                }

                override fun onError(exc: ImageCaptureException) {
                    scope.launch {
                        Log.e("GpsCameraScreen", "In-app capture failed: ${exc.message}", exc)
                        isProcessing = false
                        launchSystemCamera()
                    }
                }
            }
        )
    }

    // 1. Accompanist Rationale Permission Dialog
    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            icon = {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Camera Permission Needed",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "StrangerHelp requires camera access to capture real-time photo proofs with GPS coordinates and timestamps when you complete a task.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "This verifies your task completion to the task poster securely.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRationaleDialog = false
                        hasRequestedPermissionOnce = true
                        cameraPermissionState.launchPermissionRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                ) {
                    Text("Grant Permission", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionRationaleDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. Permission Denied Feedback Dialog (Informing user of denial & providing alternative actions)
    if (showPermissionDeniedFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedFeedbackDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Camera Access Denied",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Without camera permission, the in-app viewfinder cannot be started.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "You can enable camera permission in App Settings, or select an existing photo directly from your gallery without needing any camera permissions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDeniedFeedbackDialog = false
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Settings")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showPermissionDeniedFeedbackDialog = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Choose from Gallery")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Dedicated Post-Capture Preview Screen displaying image with verification metadata & confirmation step
    val currentCaptured = captured
    if (currentCaptured != null) {
        PostCapturePreviewScreen(
            taskId = taskId,
            capturedBitmap = currentCaptured,
            locationNote = locationNote,
            capturedLat = capturedLat,
            capturedLng = capturedLng,
            capturedTimestamp = capturedTimestamp,
            isProcessing = isProcessing,
            errorMessage = errorMessage,
            onRetake = {
                captured = null
                errorMessage = null
            },
            onSubmitConfirmed = {
                val bitmapToSubmit = captured ?: return@PostCapturePreviewScreen
                isProcessing = true
                errorMessage = null
                scope.launch {
                    try {
                        val bytes = ProofStamper.toJpeg(bitmapToSubmit)
                        onSubmitProof(bytes) { success, errorMsg ->
                            if (!success) {
                                isProcessing = false
                                errorMessage = errorMsg ?: "Failed to submit proof. Please try again."
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Failed to encode proof: ${e.message}"
                        isProcessing = false
                    }
                }
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Task Completion Proof", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("ID: ${taskId.take(8)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cameraPermissionState.status.isGranted) {
                        IconButton(
                            onClick = {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            }
                        ) {
                            Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera")
                        }
                    }
                }
            )
        }
    ) { padding ->

        // Case A: Permission Denied Placeholder View with Feedback
        if (!cameraPermissionState.status.isGranted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    "Camera Access Denied",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (cameraPermissionState.status.shouldShowRationale) {
                        "Camera permission is required to capture geotagged proof of completion. Tap below to allow camera access."
                    } else {
                        "Camera access has been disabled. You can grant access from App Settings or pick a photo from your gallery without any permissions."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                if (cameraPermissionState.status.shouldShowRationale) {
                    Button(
                        onClick = {
                            hasRequestedPermissionOnce = true
                            cameraPermissionState.launchPermissionRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Grant Camera Access", color = Primary, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open App Settings")
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "  OR  ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cameraPermissionState.status.shouldShowRationale) MaterialTheme.colorScheme.secondaryContainer else Saffron
                    ),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = if (cameraPermissionState.status.shouldShowRationale) MaterialTheme.colorScheme.onSecondaryContainer else Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Choose Photo from Gallery",
                        color = if (cameraPermissionState.status.shouldShowRationale) MaterialTheme.colorScheme.onSecondaryContainer else Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { launchSystemCamera() },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Launch System Camera")
                }
            }
            return@Scaffold
        }

        // Case C: Live Camera View
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            CameraPreview(
                lifecycleOwner = lifecycleOwner,
                imageCapture = imageCapture,
                lensFacing = lensFacing,
                onCameraBoundStatus = { bound, err ->
                    isCameraBound = bound
                    cameraBindingError = err
                },
                onTapToCapture = { capturePhoto() }
            )

            // Top Status Chip: GPS Location Verification
            Surface(
                color = Color(0xCC101828),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Saffron,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "GPS Geotag Verification Active",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // In-App Camera Unavailable Fallback Overlay (Emulator or hardware without CameraX support)
            if (!isCameraBound && cameraBindingError != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "In-App Camera Unavailable",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Live camera preview is not supported on this device or emulator. You can take a photo with your device camera or upload from gallery.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { launchSystemCamera() },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Open System Camera")
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Choose from Gallery")
                        }
                    }
                }
            }

            // Bottom Shutter & Controls
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color(0x80000000))
                    .padding(vertical = 24.dp, horizontal = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Choose from Gallery button
                    IconButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x4DFFFFFF))
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Center: Capture Photo Shutter button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Saffron, CircleShape)
                            .clickable(enabled = !isProcessing) { capturePhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                                color = Primary
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Primary)
                            )
                        }
                    }

                    // Right: System Camera Fallback
                    IconButton(
                        onClick = { launchSystemCamera() },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0x4DFFFFFF))
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "System Camera",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            errorMessage?.let {
                Surface(
                    color = Color(0xEEB00020),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                ) {
                    Text(
                        text = it,
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    imageCapture: ImageCapture,
    lensFacing: Int,
    onCameraBoundStatus: (Boolean, String?) -> Unit,
    onTapToCapture: () -> Unit
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )

    LaunchedEffect(lensFacing) {
        try {
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val hasBack = try { provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) } catch (_: Exception) { false }
                    val hasFront = try { provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) } catch (_: Exception) { false }

                    val selector = when {
                        lensFacing == CameraSelector.LENS_FACING_FRONT && hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
                        lensFacing == CameraSelector.LENS_FACING_BACK && hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
                        hasBack -> CameraSelector.DEFAULT_BACK_CAMERA
                        hasFront -> CameraSelector.DEFAULT_FRONT_CAMERA
                        else -> null
                    }

                    if (selector == null) {
                        onCameraBoundStatus(false, "No hardware camera found on this device.")
                    } else {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageCapture
                        )
                        onCameraBoundStatus(true, null)
                    }
                } catch (e: Exception) {
                    Log.e("CameraPreview", "bind failed", e)
                    onCameraBoundStatus(false, "Camera initialization failed: ${e.localizedMessage ?: "Unknown error"}")
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            Log.e("CameraPreview", "getInstance failed", e)
            onCameraBoundStatus(false, "Camera provider unavailable: ${e.localizedMessage}")
        }
    }

    DisposableEffect(previewView) {
        previewView.setOnClickListener {
            onTapToCapture()
        }
        onDispose {
            previewView.setOnClickListener(null)
        }
    }
}
