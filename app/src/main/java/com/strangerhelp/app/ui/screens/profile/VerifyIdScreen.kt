package com.strangerhelp.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.strangerhelp.app.ui.theme.Error
import com.strangerhelp.app.ui.theme.Muted
import com.strangerhelp.app.ui.theme.OnPrimary
import com.strangerhelp.app.ui.theme.Primary
import com.strangerhelp.app.ui.theme.CyanDeep
import com.strangerhelp.app.ui.theme.SurfaceVariant
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyIdScreen(
    viewModel: VerificationViewModel = viewModel(),
    navController: NavController
) {
    val status by viewModel.status.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    var idType by remember { mutableStateOf("Aadhaar") }
    var idNumber by remember { mutableStateOf("") }
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var backUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify ID") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }

            status?.status == "approved" -> {
                // ⭐ Already Verified
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text("✅", fontSize = 48.sp)
                        Text(
                            text = "Identity Verified",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanDeep
                        )
                        Text(
                            text = "Your identity has been verified. You have the ✓ Verified badge on your profile.",
                            fontSize = 14.sp,
                            color = Muted,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text("Back to Profile")
                        }
                    }
                }
            }

            status?.status == "pending" -> {
                // ⭐ Pending Review
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Primary
                        )
                        Text(
                            text = "Verification Under Review",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Your documents are being reviewed by our team. This usually takes 24-48 hours.",
                            fontSize = 14.sp,
                            color = Muted,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp)
                        ) {
                            Text("Back to Profile")
                        }
                    }
                }
            }

            else -> {
                // ⭐ Upload Form
                var expanded by remember { mutableStateOf(false) }
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    item {
                        Column {
                            Text(
                                text = "🪪 Identity Verification",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Upload your government ID to get the ✓ Verified badge on your profile.",
                                fontSize = 13.sp,
                                color = Muted
                            )
                        }
                    }

                    // ID Type
                    item {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it }
                        ) {
                            OutlinedTextField(
                                value = idType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("ID Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("Aadhaar", "PAN Card", "Passport", "Driving License", "Voter ID").forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            idType = selectionOption
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // ID Number
                    item {
                        OutlinedTextField(
                            value = idNumber,
                            onValueChange = { idNumber = it },
                            label = { Text("ID Number") },
                            placeholder = { Text("Enter your ID number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Front Photo
                    item {
                        PhotoPickerField(
                            label = "Front Photo *",
                            uri = frontUri,
                            onPick = { uri -> frontUri = uri },
                            placeholder = "Upload front photo of ID"
                        )
                    }

                    // Selfie
                    item {
                        PhotoPickerField(
                            label = "Selfie with ID *",
                            uri = selfieUri,
                            onPick = { uri -> selfieUri = uri },
                            placeholder = "Upload selfie holding the ID"
                        )
                    }

                    // Back Photo (Optional)
                    item {
                        PhotoPickerField(
                            label = "Back Photo (Optional)",
                            uri = backUri,
                            onPick = { uri -> backUri = uri },
                            placeholder = "Upload back photo of ID (if applicable)"
                        )
                    }

                    // Error
                    if (error != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Error.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = error ?: "",
                                    fontSize = 13.sp,
                                    color = Error,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Submit Button
                    item {
                        val fUri = frontUri
                        val sUri = selfieUri
                        Button(
                            onClick = {
                                if (fUri != null && sUri != null) {
                                    val frontFile = uriToFile(context, fUri)
                                    val selfieFile = uriToFile(context, sUri)
                                    val backFile = backUri?.let { uriToFile(context, it) }
                                    viewModel.submitVerification(
                                        idType = idType,
                                        idNumber = idNumber,
                                        frontFile = frontFile,
                                        selfieFile = selfieFile,
                                        backFile = backFile
                                    )
                                }
                            },
                            enabled = !isSubmitting && fUri != null && sUri != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = OnPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Submit Verification")
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Success state - navigate back
    LaunchedEffect(success) {
        if (success) {
            navController.popBackStack()
        }
    }
}

@Composable
fun PhotoPickerField(
    label: String,
    uri: Uri?,
    onPick: (Uri?) -> Unit,
    placeholder: String
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { resultUri: Uri? ->
        if (resultUri != null) onPick(resultUri)
    }

    Column {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceVariant)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📷", fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = placeholder, fontSize = 12.sp, color = Muted)
                }
            }
        }
    }
}

fun uriToFile(context: Context, uri: Uri): File {
    val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return tempFile
}
