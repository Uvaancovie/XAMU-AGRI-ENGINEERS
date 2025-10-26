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

data class ProjectUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(ProjectUiState())
    val uiState: StateFlow<ProjectUiState> = _uiState.asStateFlow()

    val projects: StateFlow<List<Project>> = auth.currentUser?.email?.let { userEmail ->
        firebaseRepository.getProjectsFlow(userEmail).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<Project>()
        )
    } ?: flowOf(emptyList<Project>()).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList<Project>()
    )

    fun addProject(
        companyName: String,
        companyEmail: String,
        projectName: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val userEmail = auth.currentUser?.email ?: return@launch

            val newProject = Project(
                companyEmail = companyEmail,
                companyName = companyName,
                appUserUsername = userEmail,
                projectName = projectName,
                createdAt = System.currentTimeMillis()
            )

            val result = firebaseRepository.addProject(newProject)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Project added successfully",
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    successMessage = null,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to add project"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}
