package com.strangerhelp.app.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.strangerhelp.app.utils.GpsCameraHelper
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun rememberGpsCameraLauncher(onResult: (File?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && currentPhotoFile != null) {
                scope.launch {
                    val stamped = GpsCameraHelper(context).stampPhotoFlow(currentPhotoFile!!)
                    onResult(stamped)
                }
            } else {
                onResult(null)
            }
        }
    )
    
    return {
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        currentPhotoFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        cameraLauncher.launch(uri)
    }
}
