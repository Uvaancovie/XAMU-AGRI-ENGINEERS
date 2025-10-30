package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.BiophysicalAttributes
import com.example.xamu_wil_project.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ViewDataUiState(
    val isLoading: Boolean = false,
    val biophysicalData: List<BiophysicalAttributes> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ViewDataViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewDataUiState())
    val uiState: StateFlow<ViewDataUiState> = _uiState.asStateFlow()

    fun loadBiophysicalData(companyName: String, projectName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getProjectDataFlow(companyName, projectName).collect { result ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    biophysicalData = result.biophysical.sortedByDescending { it.timestamp }
                )
            }
        }
    }
}