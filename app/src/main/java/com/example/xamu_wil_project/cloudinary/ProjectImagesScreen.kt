package com.example.xamu_wil_project.cloudinary

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectImagesScreen(
    projectId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Local factory to instantiate ProjectPhotosViewModel with BytescaleRepo
    val factory = remember {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProjectPhotosViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ProjectPhotosViewModel(BytescaleRepo(context)) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    val viewModel: ProjectPhotosViewModel = viewModel(factory = factory)

    val photos by viewModel.photos.collectAsState()
    val status by viewModel.status.collectAsState()

    var caption by remember { mutableStateOf("") }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }

    // snackbar & dialog state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showUploadDialog by remember { mutableStateOf(false) }
    var uploadDialogMessage by remember { mutableStateOf("") }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        pickedUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempUri?.let { viewModel.upload(projectId, it, caption) }
            caption = ""
            tempUri = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = File.createTempFile("JPEG_", ".jpg", context.externalCacheDir)
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                file
            )
            tempUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Read media permission for gallery on newer Android versions
    val readPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) galleryLauncher.launch("image/*")
        else Toast.makeText(context, "Permission denied. Can't pick images.", Toast.LENGTH_SHORT).show()
    }

    // Helper to request appropriate permission or directly launch picker
    val pickFromGallery = {
        // refresh photos before opening gallery so user sees the project photos immediately
        viewModel.refresh(projectId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    // Refresh when screen opens
    LaunchedEffect(projectId) {
        viewModel.refresh(projectId)
    }

    // Show snackbar/dialog on status changes
    LaunchedEffect(status) {
        val s = status ?: ""
        if (s.isNotBlank()) {
            scope.launch {
                when {
                    s.contains("Uploaded successfully", ignoreCase = true) -> {
                        snackbarHostState.showSnackbar("Upload complete")
                        uploadDialogMessage = "Upload finished successfully"
                        showUploadDialog = true
                    }
                    s.startsWith("Error", ignoreCase = true) -> {
                        snackbarHostState.showSnackbar(s)
                        uploadDialogMessage = s
                        showUploadDialog = true
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Photos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = "Add Photo")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(12.dp)) {

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Description / caption") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { pickFromGallery() }) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pick from Gallery")
                }
                Button(onClick = {
                    pickedUri?.let {
                        viewModel.upload(projectId, it, caption)
                        caption = ""
                        pickedUri = null
                    }
                }, enabled = pickedUri != null) {
                    Text("Upload Selected")
                }
                Spacer(Modifier.width(8.dp))
                // Manual RTDB loader in case Bytescale listing is forbidden
                Button(onClick = { viewModel.loadFromRealtimeDb(projectId) }) {
                    Text("Load from Firebase")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!status.isNullOrBlank()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(status!!, modifier = Modifier.padding(8.dp))
                if (status!!.contains("403", ignoreCase = true) || status!!.contains("forbidden", ignoreCase = true)) {
                    Text("Permission denied when listing Bytescale files. Tap 'Load from Firebase' to view uploaded photos stored in Realtime DB.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 140.dp), modifier = Modifier.fillMaxSize()) {
                items(photos) { photo ->
                    Column(modifier = Modifier
                        .padding(6.dp)
                        .clickable { /* future: open full screen */ }) {
                        AsyncImage(
                            model = photo.publicId, // Bytescale fileUrl stored in publicId
                            contentDescription = photo.caption,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = photo.caption ?: "", style = MaterialTheme.typography.bodyMedium)
                        val formatted = remember(photo.createdAt) {
                            val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                            sdf.format(Date(photo.createdAt))
                        }
                        Text(text = formatted, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload status") },
            text = { Text(uploadDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showUploadDialog = false }) { Text("OK") }
            }
        )
    }
}
