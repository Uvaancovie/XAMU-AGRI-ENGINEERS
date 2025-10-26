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
import com.example.xamu_wil_project.ui.viewmodel.ProjectViewModel
import com.example.xamu_wil_project.ui.viewmodel.ClientViewModel
import com.example.xamu_wil_project.data.Project
import com.example.xamu_wil_project.data.Client

/**
 * Professional Add Project Screen - Jetpack Compose
 * Modern Material 3 design with Firebase integration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(
    onNavigateBack: () -> Unit,
    onProjectCreated: (Project) -> Unit,
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel = hiltViewModel(),
    clientViewModel: ClientViewModel = hiltViewModel()
) {
    val projectUiState by projectViewModel.uiState.collectAsStateWithLifecycle()
    val clientUiState by clientViewModel.uiState.collectAsStateWithLifecycle()
    val clients by clientViewModel.clients.collectAsStateWithLifecycle()
    val projects by projectViewModel.projects.collectAsStateWithLifecycle()

    // Form state
    var selectedClientIndex by remember { mutableIntStateOf(-1) }
    var projectName by remember { mutableStateOf("") }

    // Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(projectUiState.successMessage) {
        projectUiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            // Clear form on success
            projectName = ""
            selectedClientIndex = -1
            projectViewModel.clearMessages()
        }
    }

    LaunchedEffect(projectUiState.errorMessage) {
        projectUiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            projectViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Project",
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
            // Add Project Form
            AddProjectForm(
                clients = clients,
                selectedClientIndex = selectedClientIndex,
                projectName = projectName,
                isLoading = projectUiState.isLoading,
                onClientSelected = { selectedClientIndex = it },
                onProjectNameChange = { projectName = it },
                onSave = {
                    if (selectedClientIndex >= 0 && projectName.isNotBlank()) {
                        val selectedClient = clients[selectedClientIndex]
                        projectViewModel.addProject(
                            companyName = selectedClient.companyName ?: "",
                            companyEmail = selectedClient.companyEmail ?: "",
                            projectName = projectName
                        )
                    }
                }
            )

            // Existing Projects List
            ExistingProjectsList(
                projects = projects,
                onProjectSelected = onProjectCreated
            )
        }
    }
}

@Composable
private fun AddProjectForm(
    clients: List<Client>,
    selectedClientIndex: Int,
    projectName: String,
    isLoading: Boolean,
    onClientSelected: (Int) -> Unit,
    onProjectNameChange: (String) -> Unit,
    onSave: () -> Unit
) {
    QuickEntryCard(
        title = "Create New Project",
        onQuickFill = {
            // Quick fill with sample data
            if (clients.isNotEmpty()) {
                onClientSelected(0)
            }
            onProjectNameChange("Sample Wetland Project 2024")
        }
    ) {
        // Client Selection
        ProfessionalDropdown(
            items = clients.map { "${it.companyName} (${it.companyType})" },
            selectedIndex = selectedClientIndex,
            onItemSelected = onClientSelected,
            label = "Select Client *",
            enabled = clients.isNotEmpty()
        )

        if (clients.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No clients available. Please add a client first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Project Name Field
        ProfessionalTextField(
            value = projectName,
            onValueChange = onProjectNameChange,
            label = "Project Name *",
            placeholder = "Wetland Assessment 2024",
            leadingIcon = Icons.Filled.Work
        )

        // Save Button
        Button(
            onClick = onSave,
            enabled = !isLoading && selectedClientIndex >= 0 && projectName.isNotBlank() && clients.isNotEmpty(),
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
                text = if (isLoading) "Creating..." else "Create Project",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun ExistingProjectsList(
    projects: List<Project>,
    onProjectSelected: (Project) -> Unit
) {
    if (projects.isNotEmpty()) {
        Text(
            text = "My Projects (${projects.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projects) { project ->
                ProjectItem(
                    project = project,
                    onProjectSelected = { onProjectSelected(project) }
                )
            }
        }
    }
}

@Composable
private fun ProjectItem(
    project: Project,
    onProjectSelected: () -> Unit
) {
    Card(
        onClick = onProjectSelected,
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.projectName ?: "Unnamed Project",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Open project",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = project.companyName ?: "No Company",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Created: ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(project.createdAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
