package com.example.xamu_wil_project.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.xamu_wil_project.ui.compose.components.*
import com.example.xamu_wil_project.ui.viewmodel.FieldDataViewModel
import com.example.xamu_wil_project.data.BiophysicalAttributes
import com.example.xamu_wil_project.data.PhaseImpacts

/**
 * Professional Field Data Capture Screen - Jetpack Compose
 * Comprehensive form for wetland biophysical and impact data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDataToProjectScreen(
    companyName: String,
    projectName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    locationStamp: String = "",
    viewModel: FieldDataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Biophysical Data State
    var location by remember { mutableStateOf(locationStamp) }
    var elevation by remember { mutableStateOf("") }
    var ecoregion by remember { mutableStateOf("") }
    var meanAnnualPrecipitation by remember { mutableStateOf("") }
    var rainfallSeasonality by remember { mutableStateOf("") }
    var evapotranspiration by remember { mutableStateOf("") }
    var geology by remember { mutableStateOf("") }
    var waterManagementArea by remember { mutableStateOf("") }
    var soilErodibility by remember { mutableStateOf("") }
    var vegetationType by remember { mutableStateOf("") }
    var conservationStatus by remember { mutableStateOf("") }
    var fepaFeatures by remember { mutableStateOf("") }

    // Impact Data State
    var runoffHardSurfaces by remember { mutableStateOf("") }
    var runoffSepticTanks by remember { mutableStateOf("") }
    var sedimentInput by remember { mutableStateOf("") }
    var floodPeaks by remember { mutableStateOf("") }
    var pollution by remember { mutableStateOf("") }
    var weedsIAP by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Field Data Collection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$projectName - $companyName",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Quick Fill card for rapid testing
            QuickEntryCard(
                title = "Quick Fill Sample Data",
                onQuickFill = {
                    // Prefill realistic sample values for quicker testing
                    location = "Lat: -33.9249, Lon: 18.4241"
                    elevation = "120"
                    ecoregion = "Fynbos"
                    meanAnnualPrecipitation = "800"
                    rainfallSeasonality = "Summer"
                    evapotranspiration = "1200"
                    geology = "Sandstone"
                    waterManagementArea = "Berg River"
                    soilErodibility = "Low"
                    vegetationType = "Fynbos"
                    conservationStatus = "Protected"
                    fepaFeatures = "FEPA priority area"

                    runoffHardSurfaces = "Low"
                    runoffSepticTanks = "None"
                    sedimentInput = "Low"
                    floodPeaks = "None"
                    pollution = "Low"
                    weedsIAP = "Some invasive species"
                }
            ) {
                Text("Prefill the form with sample biophysical and impact values for quick testing.")
            }

            // Location Section
            LocationSection(
                location = location,
                onLocationChange = { location = it }
            )

            // Biophysical Attributes Section
            BiophysicalAttributesSection(
                elevation = elevation,
                ecoregion = ecoregion,
                meanAnnualPrecipitation = meanAnnualPrecipitation,
                rainfallSeasonality = rainfallSeasonality,
                evapotranspiration = evapotranspiration,
                geology = geology,
                waterManagementArea = waterManagementArea,
                soilErodibility = soilErodibility,
                vegetationType = vegetationType,
                conservationStatus = conservationStatus,
                fepaFeatures = fepaFeatures,
                onElevationChange = { elevation = it },
                onEcoregionChange = { ecoregion = it },
                onMeanAnnualPrecipitationChange = { meanAnnualPrecipitation = it },
                onRainfallSeasonalityChange = { rainfallSeasonality = it },
                onEvapotranspirationChange = { evapotranspiration = it },
                onGeologyChange = { geology = it },
                onWaterManagementAreaChange = { waterManagementArea = it },
                onSoilErodibilityChange = { soilErodibility = it },
                onVegetationTypeChange = { vegetationType = it },
                onConservationStatusChange = { conservationStatus = it },
                onFepaFeaturesChange = { fepaFeatures = it }
            )

            // Phase Impacts Section
            PhaseImpactsSection(
                runoffHardSurfaces = runoffHardSurfaces,
                runoffSepticTanks = runoffSepticTanks,
                sedimentInput = sedimentInput,
                floodPeaks = floodPeaks,
                pollution = pollution,
                weedsIAP = weedsIAP,
                onRunoffHardSurfacesChange = { runoffHardSurfaces = it },
                onRunoffSepticTanksChange = { runoffSepticTanks = it },
                onSedimentInputChange = { sedimentInput = it },
                onFloodPeaksChange = { floodPeaks = it },
                onPollutionChange = { pollution = it },
                onWeedsIAPChange = { weedsIAP = it }
            )

            // Action Buttons
            ActionButtonsSection(
                isLoading = uiState.isLoading,
                onSave = {
                    val biophysicalData = BiophysicalAttributes(
                        location = location,
                        elevation = elevation,
                        ecoregion = ecoregion,
                        map = meanAnnualPrecipitation,
                        rainfall = rainfallSeasonality,
                        evapotranspiration = evapotranspiration,
                        geology = geology,
                        waterManagementArea = waterManagementArea,
                        soilErodibility = soilErodibility,
                        vegetationType = vegetationType,
                        conservationStatus = conservationStatus,
                        fepa = fepaFeatures
                    )

                    val impactData = PhaseImpacts(
                        runoffHardSurfaces = runoffHardSurfaces,
                        runoffSepticTanks = runoffSepticTanks,
                        sedimentInput = sedimentInput,
                        floodPeaks = floodPeaks,
                        pollution = pollution,
                        weedsIAP = weedsIAP
                    )

                    viewModel.saveBiophysicalData(
                        companyName = companyName,
                        projectName = projectName,
                        data = biophysicalData
                    )
                    viewModel.saveImpactData(
                        companyName = companyName,
                        projectName = projectName,
                        data = impactData
                    )
                }
            )

            // Bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocationSection(
    location: String,
    onLocationChange: (String) -> Unit
) {
    DashboardCard(
        title = "Location Information",
        subtitle = "GPS coordinates or descriptive location",
        icon = Icons.Filled.LocationOn
    ) {
        ProfessionalTextField(
            value = location,
            onValueChange = onLocationChange,
            label = "Location",
            placeholder = "Lat: -33.9249, Lon: 18.4241 or Site Description",
            leadingIcon = Icons.Filled.LocationOn
        )
    }
}

@Composable
private fun BiophysicalAttributesSection(
    elevation: String,
    ecoregion: String,
    meanAnnualPrecipitation: String,
    rainfallSeasonality: String,
    evapotranspiration: String,
    geology: String,
    waterManagementArea: String,
    soilErodibility: String,
    vegetationType: String,
    conservationStatus: String,
    fepaFeatures: String,
    onElevationChange: (String) -> Unit,
    onEcoregionChange: (String) -> Unit,
    onMeanAnnualPrecipitationChange: (String) -> Unit,
    onRainfallSeasonalityChange: (String) -> Unit,
    onEvapotranspirationChange: (String) -> Unit,
    onGeologyChange: (String) -> Unit,
    onWaterManagementAreaChange: (String) -> Unit,
    onSoilErodibilityChange: (String) -> Unit,
    onVegetationTypeChange: (String) -> Unit,
    onConservationStatusChange: (String) -> Unit,
    onFepaFeaturesChange: (String) -> Unit
) {
    DashboardCard(
        title = "Biophysical Attributes",
        subtitle = "Environmental characteristics and site conditions",
        icon = Icons.Filled.Nature
    ) {
        // Elevation and Ecoregion
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = elevation,
                onValueChange = onElevationChange,
                label = "Elevation (m)",
                placeholder = "1200",
                leadingIcon = Icons.Filled.Terrain,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = ecoregion,
                onValueChange = onEcoregionChange,
                label = "Ecoregion",
                placeholder = "Grassland",
                leadingIcon = Icons.Filled.Forest,
                modifier = Modifier.weight(1f)
            )
        }

        // MAP and Rainfall Seasonality
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = meanAnnualPrecipitation,
                onValueChange = onMeanAnnualPrecipitationChange,
                label = "Mean Annual Precipitation (mm)",
                placeholder = "800",
                leadingIcon = Icons.Filled.WaterDrop,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = rainfallSeasonality,
                onValueChange = onRainfallSeasonalityChange,
                label = "Rainfall Seasonality",
                placeholder = "Summer",
                leadingIcon = Icons.Filled.Cloud,
                modifier = Modifier.weight(1f)
            )
        }

        // Evapotranspiration and Geology
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = evapotranspiration,
                onValueChange = onEvapotranspirationChange,
                label = "Evapotranspiration (mm)",
                placeholder = "1200",
                leadingIcon = Icons.Filled.Air,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = geology,
                onValueChange = onGeologyChange,
                label = "Geology",
                placeholder = "Sandstone",
                leadingIcon = Icons.Filled.Landscape,
                modifier = Modifier.weight(1f)
            )
        }

        // Water Management Area and Soil Erodibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = waterManagementArea,
                onValueChange = onWaterManagementAreaChange,
                label = "Water Management Area",
                placeholder = "Berg River",
                leadingIcon = Icons.Filled.Water,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = soilErodibility,
                onValueChange = onSoilErodibilityChange,
                label = "Soil Erodibility",
                placeholder = "Low/Medium/High",
                leadingIcon = Icons.Filled.Grass,
                modifier = Modifier.weight(1f)
            )
        }

        // Vegetation Type and Conservation Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = vegetationType,
                onValueChange = onVegetationTypeChange,
                label = "Vegetation Type",
                placeholder = "Fynbos",
                leadingIcon = Icons.Filled.LocalFlorist,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = conservationStatus,
                onValueChange = onConservationStatusChange,
                label = "Conservation Status",
                placeholder = "Protected",
                leadingIcon = Icons.Filled.Shield,
                modifier = Modifier.weight(1f)
            )
        }

        // FEPA Features
        ProfessionalTextField(
            value = fepaFeatures,
            onValueChange = onFepaFeaturesChange,
            label = "FEPA Features",
            placeholder = "Freshwater Ecosystem Priority Area details",
            leadingIcon = Icons.Filled.Eco
        )
    }
}

@Composable
private fun PhaseImpactsSection(
    runoffHardSurfaces: String,
    runoffSepticTanks: String,
    sedimentInput: String,
    floodPeaks: String,
    pollution: String,
    weedsIAP: String,
    onRunoffHardSurfacesChange: (String) -> Unit,
    onRunoffSepticTanksChange: (String) -> Unit,
    onSedimentInputChange: (String) -> Unit,
    onFloodPeaksChange: (String) -> Unit,
    onPollutionChange: (String) -> Unit,
    onWeedsIAPChange: (String) -> Unit
) {
    DashboardCard(
        title = "Construction/Operation Impacts",
        subtitle = "Environmental impact assessment data",
        icon = Icons.Filled.Warning
    ) {
        // Runoff Types
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = runoffHardSurfaces,
                onValueChange = onRunoffHardSurfacesChange,
                label = "Runoff (Hard Surfaces)",
                placeholder = "Low/Medium/High",
                leadingIcon = Icons.Filled.Layers,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = runoffSepticTanks,
                onValueChange = onRunoffSepticTanksChange,
                label = "Septic Tank Runoff",
                placeholder = "Present/Absent",
                leadingIcon = Icons.Filled.WaterDrop,
                modifier = Modifier.weight(1f)
            )
        }

        // Sediment and Flood Impacts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = sedimentInput,
                onValueChange = onSedimentInputChange,
                label = "Sediment Input",
                placeholder = "Minimal/Moderate/High",
                leadingIcon = Icons.Filled.Terrain,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = floodPeaks,
                onValueChange = onFloodPeaksChange,
                label = "Flood Peaks",
                placeholder = "Low/Medium/High",
                leadingIcon = Icons.Filled.Flood,
                modifier = Modifier.weight(1f)
            )
        }

        // Pollution and Invasive Species
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = pollution,
                onValueChange = onPollutionChange,
                label = "Pollution",
                placeholder = "Type and severity",
                leadingIcon = Icons.Filled.ReportProblem,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = weedsIAP,
                onValueChange = onWeedsIAPChange,
                label = "Weeds/Invasive Alien Plants",
                placeholder = "Species present",
                leadingIcon = Icons.Filled.Grass,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    isLoading: Boolean,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSave,
            enabled = !isLoading,
            modifier = Modifier.weight(1f),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving...")
            } else {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Field Data")
            }
        }
    }
}
