package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.repository.WeatherRepository
import com.example.xamu_wil_project.data.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * WeatherViewModel for Xamu Wetlands
 * Uses WeatherAPI.com via WeatherRepository
 * Implements MVVM pattern with Hilt DI
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /**
     * Fetch weather for GPS coordinates
     */
    fun fetchWeather(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            weatherRepository.getCurrentWeather(latitude, longitude)
                .fold(
                    onSuccess = { weatherData ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            weatherData = weatherData,
                            successMessage = "Weather data loaded successfully"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to fetch weather: ${error.localizedMessage}"
                        )
                    }
                )
        }
    }

    /**
     * Fetch weather by city name
     */
    fun fetchWeatherByCity(cityName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            weatherRepository.getWeatherByCity(cityName)
                .fold(
                    onSuccess = { weatherData ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            weatherData = weatherData,
                            successMessage = "Weather data loaded for $cityName"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to fetch weather for $cityName: ${error.localizedMessage}"
                        )
                    }
                )
        }
    }

    /**
     * Clear weather data and reset state
     */
    fun clearWeatherData() {
        _uiState.value = WeatherUiState()
    }

    /**
     * Clear error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clear success messages
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

/**
 * UI State for Weather functionality
 */
data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
