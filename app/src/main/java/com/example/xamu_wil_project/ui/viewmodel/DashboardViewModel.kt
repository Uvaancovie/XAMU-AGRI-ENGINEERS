package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.Project
import com.example.xamu_wil_project.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalProjects: Int = 0,
    val totalClients: Int = 0,
    val totalNotes: Int = 0,
    val totalRoutes: Int = 0,
    val recentProjects: List<Project> = emptyList(),
    val lastSyncTime: String = "just now"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userEmail = auth.currentUser?.email ?: return@launch

            // Load projects
            repository.getProjectsFlow(userEmail).collect { projects ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalProjects = projects.size,
                    recentProjects = projects.take(5)
                )
            }

            // Load clients count
            repository.getClientsFlow().collect { clients ->
                _uiState.value = _uiState.value.copy(
                    totalClients = clients.size
                )
            }

            // Load notes count
            repository.getNotesFlow(userEmail, "").collect { notes ->
                _uiState.value = _uiState.value.copy(
                    totalNotes = notes.size
                )
            }

            // Load routes count
            repository.getRoutesFlow(userEmail, "").collect { routes ->
                _uiState.value = _uiState.value.copy(
                    totalRoutes = routes.size
                )
            }
        }
    }

    fun refreshData() {
        loadDashboardData()
    }
}
