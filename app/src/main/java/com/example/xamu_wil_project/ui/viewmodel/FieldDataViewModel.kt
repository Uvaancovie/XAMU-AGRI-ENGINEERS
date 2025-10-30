package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.BiophysicalAttributes
import com.example.xamu_wil_project.data.PhaseImpacts
import com.example.xamu_wil_project.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FieldDataUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class FieldDataViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FieldDataUiState())
    val uiState: StateFlow<FieldDataUiState> = _uiState.asStateFlow()

    fun saveBiophysicalData(
        companyName: String,
        projectName: String,
        data: BiophysicalAttributes
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.addBiophysicalData(companyName, projectName, data)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = if (result.isSuccess) "Data saved successfully" else null,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun saveImpactData(
        companyName: String,
        projectName: String,
        data: PhaseImpacts
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = repository.saveImpactData(companyName, projectName, data)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                successMessage = if (result.isSuccess) "Impact data saved successfully" else null,
                errorMessage = result.exceptionOrNull()?.message
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}
