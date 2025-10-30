package com.example.xamu_wil_project.ui.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.xamu_wil_project.data.BiophysicalAttributes
import com.example.xamu_wil_project.ui.viewmodel.ViewDataViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewDataScreen(
    companyName: String,
    projectName: String,
    onNavigateBack: () -> Unit,
    onNavigateToEditEntry: (String) -> Unit, // New navigation callback
    viewModel: ViewDataViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBiophysicalData(companyName, projectName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biophysical Data Entries") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                item { CircularProgressIndicator() }
            } else {
                items(uiState.biophysicalData) { entry ->
                    BiophysicalDataEntryCard(entry) {
                        onNavigateToEditEntry(entry.id)
                    }
                }
            }
        }
    }
}

@Composable
fun BiophysicalDataEntryCard(
    entry: BiophysicalAttributes,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Entry from: ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(entry.timestamp))}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Location: ${entry.location}")
            Text("Ecoregion: ${entry.ecoregion}")
            Text("Vegetation Type: ${entry.vegetationType}")
        }
    }
}
