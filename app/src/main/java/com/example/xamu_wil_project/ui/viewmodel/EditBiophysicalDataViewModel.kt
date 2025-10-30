package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.BiophysicalAttributes
import com.example.xamu_wil_project.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditBiophysicalDataUiState(
    val isLoading: Boolean = false,
    val biophysicalData: BiophysicalAttributes? = null,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class EditBiophysicalDataViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditBiophysicalDataUiState())
    val uiState: StateFlow<EditBiophysicalDataUiState> = _uiState.asStateFlow()

    fun loadBiophysicalData(companyName: String, projectName: String, entryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getProjectDataFlow(companyName, projectName).collect { result ->
                val entry = result.biophysical.find { it.id == entryId }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    biophysicalData = entry
                )
            }
        }
    }

    fun saveBiophysicalData(
        companyName: String,
        projectName: String,
        data: BiophysicalAttributes
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = repository.updateBiophysicalData(companyName, projectName, data)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSaved = result.isSuccess,
                error = result.exceptionOrNull()?.message
            )
        }
    }
}