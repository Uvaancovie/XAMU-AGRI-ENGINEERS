package com.example.xamu_wil_project.ui.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.xamu_wil_project.ui.compose.components.*
import com.example.xamu_wil_project.ui.viewmodel.ClientViewModel
import com.example.xamu_wil_project.data.Client

/**
 * Professional Add Client Screen - Jetpack Compose
 * Modern Material 3 design with Firebase integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClientScreen(
    onNavigateBack: () -> Unit,
    onClientSelected: (Client) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clients by viewModel.clients.collectAsStateWithLifecycle()

    // Form state
    var companyName by remember { mutableStateOf("") }
    var companyRegNum by remember { mutableStateOf("") }
    var companyType by remember { mutableStateOf("") }
    var companyEmail by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            // Clear form on success
            companyName = ""
            companyRegNum = ""
            companyType = ""
            companyEmail = ""
            contactPerson = ""
            phoneNumber = ""
            address = ""
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
                    Text(
                        text = "Add Client",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
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
            // Add Client Form
            AddClientForm(
                companyName = companyName,
                companyRegNum = companyRegNum,
                companyType = companyType,
                companyEmail = companyEmail,
                contactPerson = contactPerson,
                phoneNumber = phoneNumber,
                address = address,
                isLoading = uiState.isLoading,
                onCompanyNameChange = { companyName = it },
                onCompanyRegNumChange = { companyRegNum = it },
                onCompanyTypeChange = { companyType = it },
                onCompanyEmailChange = { companyEmail = it },
                onContactPersonChange = { contactPerson = it },
                onPhoneNumberChange = { phoneNumber = it },
                onAddressChange = { address = it },
                onSave = {
                    if (companyName.isNotBlank()) {
                        val newClient = Client(
                            companyName = companyName,
                            companyRegNum = companyRegNum,
                            companyType = companyType,
                            companyEmail = companyEmail,
                            contactPerson = contactPerson,
                            phoneNumber = phoneNumber,
                            address = address
                        )
                        viewModel.addClient(newClient)
                    }
                }
            )

            // Existing Clients List
            ExistingClientsList(
                clients = clients,
                onClientSelected = onClientSelected
            )
        }
    }
}

@Composable
private fun AddClientForm(
    companyName: String,
    companyRegNum: String,
    companyType: String,
    companyEmail: String,
    contactPerson: String,
    phoneNumber: String,
    address: String,
    isLoading: Boolean,
    onCompanyNameChange: (String) -> Unit,
    onCompanyRegNumChange: (String) -> Unit,
    onCompanyTypeChange: (String) -> Unit,
    onCompanyEmailChange: (String) -> Unit,
    onContactPersonChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onSave: () -> Unit
) {
    QuickEntryCard(
        title = "Add New Client",
        onQuickFill = {
            // Quick fill with sample data
            onCompanyNameChange("Sample Company")
            onCompanyRegNumChange("REG123456")
            onCompanyTypeChange("Private")
            onCompanyEmailChange("info@sample.com")
            onContactPersonChange("John Doe")
            onPhoneNumberChange("+27 123 456 789")
            onAddressChange("123 Main St, City")
        }
    ) {
        // Required fields row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = companyName,
                onValueChange = onCompanyNameChange,
                label = "Company Name *",
                placeholder = "Acme Corp",
                leadingIcon = Icons.Filled.Business,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = companyType,
                onValueChange = onCompanyTypeChange,
                label = "Company Type",
                placeholder = "Private/Public",
                leadingIcon = Icons.Filled.Category,
                modifier = Modifier.weight(1f)
            )
        }

        // Registration and contact row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = companyRegNum,
                onValueChange = onCompanyRegNumChange,
                label = "Registration Number",
                placeholder = "REG123456",
                leadingIcon = Icons.Filled.Numbers,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = companyEmail,
                onValueChange = onCompanyEmailChange,
                label = "Company Email",
                placeholder = "info@company.com",
                leadingIcon = Icons.Filled.Email,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                modifier = Modifier.weight(1f)
            )
        }

        // Contact person and phone
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfessionalTextField(
                value = contactPerson,
                onValueChange = onContactPersonChange,
                label = "Contact Person",
                placeholder = "John Doe",
                leadingIcon = Icons.Filled.Person,
                modifier = Modifier.weight(1f)
            )

            ProfessionalTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = "Phone Number",
                placeholder = "+27 123 456 789",
                leadingIcon = Icons.Filled.Phone,
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                modifier = Modifier.weight(1f)
            )
        }

        // Address field
        ProfessionalTextField(
            value = address,
            onValueChange = onAddressChange,
            label = "Address",
            placeholder = "123 Main St, City, Province",
            leadingIcon = Icons.Filled.LocationOn
        )

        // Save button
        Button(
            onClick = onSave,
            enabled = !isLoading && companyName.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isLoading) "Saving..." else "Add Client",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ExistingClientsList(
    clients: List<Client>,
    onClientSelected: (Client) -> Unit
) {
    if (clients.isNotEmpty()) {
        Text(
            text = "Existing Clients (${clients.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(clients) { client ->
                ClientItem(
                    client = client,
                    onClientSelected = { onClientSelected(client) }
                )
            }
        }
    }
}

@Composable
private fun ClientItem(
    client: Client,
    onClientSelected: () -> Unit
) {
    Card(
        onClick = onClientSelected,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = client.companyName ?: "N/A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!client.companyType.isNullOrBlank()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(client.companyType ?: "") }
                    )
                }
            }

            if (!client.companyEmail.isNullOrBlank()) {
                Text(
                    text = client.companyEmail ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!client.contactPerson.isNullOrBlank()) {
                Text(
                    text = "Contact: ${client.contactPerson}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
