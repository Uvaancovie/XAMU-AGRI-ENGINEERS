package com.example.xamu_wil_project.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.xamu_wil_project.data.BiophysicalAttributes
import com.example.xamu_wil_project.ui.viewmodel.EditBiophysicalDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBiophysicalDataScreen(
    companyName: String,
    projectName: String,
    entryId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditBiophysicalDataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var location by remember { mutableStateOf("") }
    var elevation by remember { mutableStateOf("") }
    var ecoregion by remember { mutableStateOf("") }
    var map by remember { mutableStateOf("") }
    var rainfall by remember { mutableStateOf("") }
    var evapotranspiration by remember { mutableStateOf("") }
    var geology by remember { mutableStateOf("") }
    var waterManagementArea by remember { mutableStateOf("") }
    var soilErodibility by remember { mutableStateOf("") }
    var vegetationType by remember { mutableStateOf("") }
    var conservationStatus by remember { mutableStateOf("") }
    var fepa by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadBiophysicalData(companyName, projectName, entryId)
    }

    LaunchedEffect(uiState.biophysicalData) {
        uiState.biophysicalData?.let {
            location = it.location
            elevation = it.elevation
            ecoregion = it.ecoregion
            map = it.map
            rainfall = it.rainfall
            evapotranspiration = it.evapotranspiration
            geology = it.geology
            waterManagementArea = it.waterManagementArea
            soilErodibility = it.soilErodibility
            vegetationType = it.vegetationType
            conservationStatus = it.conservationStatus
            fepa = it.fepa
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Biophysical Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val updatedData = uiState.biophysicalData?.copy(
                    location = location,
                    elevation = elevation,
                    ecoregion = ecoregion,
                    map = map,
                    rainfall = rainfall,
                    evapotranspiration = evapotranspiration,
                    geology = geology,
                    waterManagementArea = waterManagementArea,
                    soilErodibility = soilErodibility,
                    vegetationType = vegetationType,
                    conservationStatus = conservationStatus,
                    fepa = fepa
                )
                if (updatedData != null) {
                    viewModel.saveBiophysicalData(companyName, projectName, updatedData)
                }
            }) {
                Icon(Icons.Filled.Save, contentDescription = "Save")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
            OutlinedTextField(value = elevation, onValueChange = { elevation = it }, label = { Text("Elevation") })
            OutlinedTextField(value = ecoregion, onValueChange = { ecoregion = it }, label = { Text("Ecoregion") })
            OutlinedTextField(value = map, onValueChange = { map = it }, label = { Text("Map") })
            OutlinedTextField(value = rainfall, onValueChange = { rainfall = it }, label = { Text("Rainfall") })
            OutlinedTextField(value = evapotranspiration, onValueChange = { evapotranspiration = it }, label = { Text("Evapotranspiration") })
            OutlinedTextField(value = geology, onValueChange = { geology = it }, label = { Text("Geology") })
            OutlinedTextField(value = waterManagementArea, onValueChange = { waterManagementArea = it }, label = { Text("Water Management Area") })
            OutlinedTextField(value = soilErodibility, onValueChange = { soilErodibility = it }, label = { Text("Soil Erodibility") })
            OutlinedTextField(value = vegetationType, onValueChange = { vegetationType = it }, label = { Text("Vegetation Type") })
            OutlinedTextField(value = conservationStatus, onValueChange = { conservationStatus = it }, label = { Text("Conservation Status") })
            OutlinedTextField(value = fepa, onValueChange = { fepa = it }, label = { Text("FEPA") })
        }
    }
}
