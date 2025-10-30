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
            _uiState.update { it.copy(isLoading = true) }

            val userEmail = auth.currentUser?.email ?: return@launch

            repository.getProjectsFlow(userEmail)
                .combine(repository.getClientsFlow()) { projects, clients ->
                    val recentProjects = projects.sortedByDescending { it.createdAt }.take(5)
                    val notesFlows = recentProjects.map { repository.getNotesFlow(it.companyName ?: "", it.projectName ?: "") }
                    val routesFlows = recentProjects.map { repository.getRoutesFlow(it.companyName ?: "", it.projectName ?: "") }

                    combine(notesFlows) { notes -> notes.sumOf { it.size } }
                        .combine(combine(routesFlows) { routes -> routes.sumOf { it.size } }) { notesCount, routesCount ->
                            _uiState.update { currentState ->
                                currentState.copy(
                                    totalProjects = projects.size,
                                    totalClients = clients.size,
                                    recentProjects = recentProjects,
                                    totalNotes = notesCount,
                                    totalRoutes = routesCount,
                                    isLoading = false
                                )
                            }
                        }.collect()
                }.launchIn(viewModelScope)
        }
    }

    fun refreshData() {
        loadDashboardData()
    }
}
