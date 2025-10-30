package com.example.xamu_wil_project.ui.compose.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import com.example.xamu_wil_project.data.repository.BytescaleRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectBytescaleScreen(
    companyName: String,
    projectName: String,
    projectId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { BytescaleRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var caption by remember { mutableStateOf("") }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pickedUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempUri?.let { pickedUri = it }
        } else {
            status = "Camera cancelled"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            try {
                val file = File.createTempFile("JPEG_", ".jpg", context.externalCacheDir)
                val authority = context.packageName + ".fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, file)
                tempUri = uri
                cameraLauncher.launch(uri)
            } catch (e: Exception) {
                status = "Camera error: ${e.message}"
            }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val readPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) galleryLauncher.launch("image/*")
        else Toast.makeText(context, "Permission denied to pick images", Toast.LENGTH_SHORT).show()
    }

    fun pickFromGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Upload Photo") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(12.dp)) {
            OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("Caption") }, modifier = Modifier.fillMaxWidth(), enabled = !isUploading)

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { pickFromGallery() }, enabled = !isUploading) { Text("Pick Image") }
                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }, enabled = !isUploading) { Text("Take Photo") }
                Button(onClick = {
                    val uri = pickedUri
                    if (uri == null) {
                        status = "No image selected"
                        return@Button
                    }
                    isUploading = true
                    status = "Uploading..."
                    coroutineScope.launch {
                        val createdAt = System.currentTimeMillis()
                        val res = repo.uploadAndPersist(companyName, projectName, projectId, uri, caption, createdAt)
                        isUploading = false
                        if (res.isSuccess) {
                            status = "Uploaded"
                            // clear selections and go back so ProjectDetails will refresh from DB
                            pickedUri = null
                            caption = ""
                            onBack()
                        } else {
                            status = "Upload failed: ${res.exceptionOrNull()?.message}"
                        }
                    }
                }, enabled = !isUploading) { Text("Upload") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (pickedUri != null) {
                AsyncImage(model = pickedUri, contentDescription = "Selected image", modifier = Modifier.fillMaxWidth().height(220.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            status?.let { Text(it) }

            if (isUploading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
