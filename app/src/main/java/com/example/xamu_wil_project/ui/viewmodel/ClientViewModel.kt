package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.Client
import com.example.xamu_wil_project.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    val clients: StateFlow<List<Client>> = repository.getClientsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addClient(client: Client) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.addClient(client)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Client added successfully",
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    successMessage = null,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to add client"
                )
            }
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.updateClient(client)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Client updated successfully",
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    successMessage = null,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to update client"
                )
            }
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.deleteClient(client)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Client deleted successfully",
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    successMessage = null,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete client"
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
