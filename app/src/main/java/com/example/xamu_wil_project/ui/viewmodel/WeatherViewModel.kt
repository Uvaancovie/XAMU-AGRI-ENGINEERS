package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.WeatherResponse
import com.example.xamu_wil_project.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val error: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = weatherRepository.getCurrentWeather(latitude, longitude)
                result.fold(
                    onSuccess = { weatherData ->
                        _uiState.value = _uiState.value.copy(isLoading = false, weatherData = weatherData)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(isLoading = false, error = exception.message)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearWeatherData() {
        _uiState.value = WeatherUiState()
    }
}
