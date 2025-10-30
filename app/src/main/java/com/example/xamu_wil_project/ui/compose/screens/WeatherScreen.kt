package com.example.xamu_wil_project.ui.compose.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.xamu_wil_project.data.Current
import com.example.xamu_wil_project.data.Location
import com.example.xamu_wil_project.ui.viewmodel.WeatherViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onNavigateBack: () -> Unit,
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by weatherViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        weatherViewModel.fetchWeather(location.latitude, location.longitude)
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Forecast") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text("Error: ${uiState.error}")
                }
                uiState.weatherData != null -> {
                    WeatherDetails(uiState.weatherData!!.location, uiState.weatherData!!.current)
                }
                else -> {
                    Text("Fetching weather data...")
                }
            }
        }
    }
}

@Composable
fun WeatherDetails(location: Location, current: Current) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = location.name, style = MaterialTheme.typography.headlineMedium)
        Text(text = "${current.temp_c}°C", style = MaterialTheme.typography.displayLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = "https:${current.condition.icon}",
                contentDescription = current.condition.text,
                modifier = Modifier.size(64.dp)
            )
            Text(text = current.condition.text, style = MaterialTheme.typography.titleLarge)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Feels like: ${current.feelslike_c}°C", fontWeight = FontWeight.Bold)
        Text("Wind: ${current.wind_kph} kph ${current.wind_dir}")
        Text("Humidity: ${current.humidity}%")
        Text("Pressure: ${current.pressure_mb} mb")
    }
}
