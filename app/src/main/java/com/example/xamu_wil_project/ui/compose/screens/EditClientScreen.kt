package com.example.xamu_wil_project.ui.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.xamu_wil_project.data.Client
import com.example.xamu_wil_project.ui.viewmodel.ClientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClientScreen(
    onNavigateBack: () -> Unit,
    client: Client,
    viewModel: ClientViewModel = hiltViewModel()
) {
    var companyName by remember { mutableStateOf(client.companyName ?: "") }
    var companyEmail by remember { mutableStateOf(client.companyEmail ?: "") }
    var companyType by remember { mutableStateOf(client.companyType ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Client") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = companyName,
                onValueChange = { companyName = it },
                label = { Text("Company Name") }
            )
            OutlinedTextField(
                value = companyEmail,
                onValueChange = { companyEmail = it },
                label = { Text("Company Email") }
            )
            OutlinedTextField(
                value = companyType,
                onValueChange = { companyType = it },
                label = { Text("Company Type") }
            )
            Button(
                onClick = {
                    viewModel.updateClient(client.copy(companyName = companyName, companyEmail = companyEmail, companyType = companyType))
                    onNavigateBack()
                }
            ) {
                Text("Save Changes")
            }
        }
    }
}
