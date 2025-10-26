package com.example.xamu_wil_project.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.*
import com.example.xamu_wil_project.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectDetailsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val notes: List<Note> = emptyList(),
    val routes: List<Route> = emptyList(),
    val photos: List<FieldPhoto> = emptyList(),
    val currentRoute: Route? = null,
    val isTrackingRoute: Boolean = false
)

@HiltViewModel
class ProjectDetailsViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(ProjectDetailsUiState())
    val uiState: StateFlow<ProjectDetailsUiState> = _uiState.asStateFlow()

    fun addNote(
        companyName: String,
        projectName: String,
        noteText: String,
        location: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val note = Note(
                note = noteText,
                location = location,
                timestamp = System.currentTimeMillis(),
                userId = auth.currentUser?.uid
            )

            val result = repository.addNote(companyName, projectName, note)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = if (result.isSuccess) "Note added successfully" else null,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun startRouteTracking(companyName: String, projectName: String) {
        viewModelScope.launch {
            val route = Route(
                routeId = System.currentTimeMillis().toString(),
                projectName = projectName,
                companyName = companyName,
                userId = auth.currentUser?.uid ?: "",
                startTime = System.currentTimeMillis(),
                isActive = true
            )

            _uiState.value = _uiState.value.copy(
                currentRoute = route,
                isTrackingRoute = true,
                successMessage = "Route tracking started"
            )
        }
    }

    fun stopRouteTracking(companyName: String, projectName: String) {
        viewModelScope.launch {
            val currentRoute = _uiState.value.currentRoute ?: return@launch

            val completedRoute = currentRoute.copy(
                endTime = System.currentTimeMillis(),
                isActive = false
            )

            val result = repository.saveRoute(companyName, projectName, completedRoute)

            _uiState.value = _uiState.value.copy(
                currentRoute = null,
                isTrackingRoute = false,
                successMessage = if (result.isSuccess) "Route saved successfully (${completedRoute.points.size} points)" else null,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun addRoutePoint(latitude: Double, longitude: Double, elevation: Double = 0.0) {
        val currentRoute = _uiState.value.currentRoute ?: return

        val point = RoutePoint(
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis(),
            elevation = elevation
        )

        val updatedRoute = currentRoute.copy(
            points = currentRoute.points + point
        )

        _uiState.value = _uiState.value.copy(currentRoute = updatedRoute)
    }

    fun uploadPhoto(
        companyName: String,
        projectName: String,
        photoUri: Uri,
        caption: String,
        location: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.uploadPhoto(
                companyName = companyName,
                projectName = projectName,
                photoUri = photoUri,
                caption = caption,
                location = location
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = if (result.isSuccess) "Photo uploaded successfully" else null,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun loadProjectData(companyName: String, projectName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.getProjectDataFlow(companyName, projectName)
                .collect { projectData ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        notes = projectData.notes,
                        routes = projectData.routes,
                        photos = projectData.photos
                    )
                }
        }
    }

    // Allow UI to set an error message directly (used by composables when they catch local exceptions)
    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}
