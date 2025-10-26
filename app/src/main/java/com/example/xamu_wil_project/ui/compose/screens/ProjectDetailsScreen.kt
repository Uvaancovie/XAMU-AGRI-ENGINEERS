package com.example.xamu_wil_project.ui.compose.screens

import android.Manifest
import android.net.Uri
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.xamu_wil_project.ui.compose.components.*
import com.example.xamu_wil_project.ui.viewmodel.ProjectDetailsViewModel
import com.example.xamu_wil_project.ui.viewmodel.WeatherViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File

/**
 * Enhanced Project Details Screen with full field data capture
 * - Weather API integration
 * - GPS Location tracking
 * - Add Note with location
 * - Route tracking (record → save)
 * - Camera capture with metadata upload
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    companyName: String,
    projectName: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddData: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectDetailsViewModel = hiltViewModel(),
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val weatherState by weatherViewModel.uiState.collectAsStateWithLifecycle()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var showCameraOptions by remember { mutableStateOf(false) }
    var currentLatitude by remember { mutableDoubleStateOf(-29.8587) }
    var currentLongitude by remember { mutableDoubleStateOf(31.0218) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Get current location
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLatitude = it.latitude
                        currentLongitude = it.longitude
                    }
                }
            } catch (e: SecurityException) {
                // Permission denied
            }
        }
    }

    // Camera launcher - take picture into supplied Uri
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // use local val to avoid smart-cast issues on delegated property
            val uri = photoUri
            uri?.let {
                viewModel.uploadPhoto(
                    companyName = companyName,
                    projectName = projectName,
                    photoUri = it,
                    caption = "Field Photo",
                    location = "$currentLatitude, $currentLongitude"
                )
            }
            showCameraOptions = false
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.uploadPhoto(
                companyName = companyName,
                projectName = projectName,
                photoUri = it,
                caption = "Field Photo",
                location = "$currentLatitude, $currentLongitude"
            )
            showCameraOptions = false
        }
    }

    // Load project data
    LaunchedEffect(Unit) {
        viewModel.loadProjectData(companyName, projectName)
        // Request location permissions
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = projectName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = companyName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        weatherViewModel.fetchWeather(currentLatitude, currentLongitude)
                        showWeatherDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Cloud,
                            contentDescription = "Weather"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddData,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Field Data"
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Info Card
            LocationInfoCard(
                latitude = currentLatitude,
                longitude = currentLongitude,
                onRefreshLocation = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            // Quick Actions Grid
            QuickActionsSection(
                isTrackingRoute = uiState.isTrackingRoute,
                onToggleRouteTracking = {
                    if (uiState.isTrackingRoute) {
                        viewModel.stopRouteTracking(companyName, projectName)
                    } else {
                        viewModel.startRouteTracking(companyName, projectName)
                    }
                },
                onAddNote = { showAddNoteDialog = true },
                onShowWeather = {
                    weatherViewModel.fetchWeather(currentLatitude, currentLongitude)
                    showWeatherDialog = true
                },
                onTakePhoto = { showCameraOptions = true },
                onNavigateToAddData = onNavigateToAddData
            )

            // Project Data Summary
            ProjectDataSummary(
                notesCount = uiState.notes.size,
                routesCount = uiState.routes.size,
                photosCount = uiState.photos.size
            )

            // Recent Notes
            if (uiState.notes.isNotEmpty()) {
                RecentNotesSection(notes = uiState.notes.take(3))
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AddNoteDialog(
            latitude = currentLatitude,
            longitude = currentLongitude,
            onSave = { noteText ->
                viewModel.addNote(
                    companyName = companyName,
                    projectName = projectName,
                    noteText = noteText,
                    location = "Lat: ${String.format("%.6f", currentLatitude)}, Lon: ${String.format("%.6f", currentLongitude)}"
                )
                showAddNoteDialog = false
            },
            onDismiss = { showAddNoteDialog = false }
        )
    }

    // Weather Dialog
    if (showWeatherDialog) {
        WeatherDialog(
            weatherData = weatherState.weatherData,
            isLoading = weatherState.isLoading,
            errorMessage = weatherState.errorMessage,
            onDismiss = { showWeatherDialog = false }
        )
    }

    // Camera Options Dialog
    if (showCameraOptions) {
        AlertDialog(
            onDismissRequest = { showCameraOptions = false },
            title = { Text("Add Photo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Choose photo source:")
                    Button(
                        onClick = {
                            try {
                                val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                                photoFile.parentFile?.mkdirs()
                                if (!photoFile.exists()) {
                                    photoFile.createNewFile()
                                }
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                photoUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                // If file creation or FileProvider fails, show an error via snackbar
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Unable to create photo file: ${e.message}",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Camera, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Take Photo")
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Photo, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCameraOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Track route points if active
    LaunchedEffect(uiState.isTrackingRoute) {
        if (uiState.isTrackingRoute) {
            // Add route point every 30 seconds
            kotlinx.coroutines.delay(30000)
            viewModel.addRoutePoint(currentLatitude, currentLongitude)
        }
    }
}

@Composable
private fun LocationInfoCard(
    latitude: Double,
    longitude: Double,
    onRefreshLocation: () -> Unit
) {
    DashboardCard(
        title = "Current Location",
        subtitle = "GPS coordinates",
        icon = Icons.Filled.LocationOn
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Latitude: ${String.format("%.6f", latitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Longitude: ${String.format("%.6f", longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRefreshLocation) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh Location")
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    isTrackingRoute: Boolean,
    onToggleRouteTracking: () -> Unit,
    onAddNote: () -> Unit,
    onShowWeather: () -> Unit,
    onTakePhoto: () -> Unit,
    onNavigateToAddData: () -> Unit
) {
    Text(
        text = "Quick Actions",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Add Note Button
        ActionCard(
            title = "Add Note",
            icon = Icons.Filled.Note,
            onClick = onAddNote,
            modifier = Modifier.weight(1f)
        )

        // Route Tracking Button
        ActionCard(
            title = if (isTrackingRoute) "Stop Route" else "Start Route",
            icon = if (isTrackingRoute) Icons.Filled.Stop else Icons.Filled.Route,
            onClick = onToggleRouteTracking,
            containerColor = if (isTrackingRoute)
                MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Take Photo Button
        ActionCard(
            title = "Take Photo",
            icon = Icons.Filled.CameraAlt,
            onClick = onTakePhoto,
            modifier = Modifier.weight(1f)
        )

        // Weather Button
        ActionCard(
            title = "Weather",
            icon = Icons.Filled.Cloud,
            onClick = onShowWeather,
            modifier = Modifier.weight(1f)
        )
    }

    // Add Field Data Button
    Button(
        onClick = onNavigateToAddData,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.DataUsage, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Add Biophysical Data")
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ProjectDataSummary(
    notesCount: Int,
    routesCount: Int,
    photosCount: Int
) {
    DashboardCard(
        title = "Project Data Summary",
        subtitle = "Captured field data",
        icon = Icons.Filled.Assessment
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DataStatItem("Notes", notesCount)
            DataStatItem("Routes", routesCount)
            DataStatItem("Photos", photosCount)
        }
    }
}

@Composable
private fun DataStatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecentNotesSection(notes: List<com.example.xamu_wil_project.data.Note>) {
    Text(
        text = "Recent Notes",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )

    notes.forEach { note ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = note.note ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = note.location ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(note.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNoteDialog(
    latitude: Double,
    longitude: Double,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Field Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Location: ${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note") },
                    placeholder = { Text("Enter field observation...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (noteText.isNotBlank()) onSave(noteText) },
                enabled = noteText.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WeatherDialog(
    weatherData: com.example.xamu_wil_project.data.WeatherResponse?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Current Weather") },
        text = {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
                weatherData != null -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Location: ${weatherData.location?.name ?: "Unknown"}")
                        Text("Temperature: ${weatherData.current?.temp_c}°C")
                        Text("Condition: ${weatherData.current?.condition?.text ?: ""}")
                        Text("Humidity: ${weatherData.current?.humidity}%")
                        Text("Wind: ${weatherData.current?.wind_kph} km/h")
                    }
                }
                else -> {
                    Text("No weather data available")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
