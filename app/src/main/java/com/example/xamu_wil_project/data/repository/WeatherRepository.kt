package com.example.xamu_wil_project.data.repository

import com.example.xamu_wil_project.data.WeatherApiService
import com.example.xamu_wil_project.data.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WeatherRepository for Xamu Wetlands
 * Handles weather data operations using WeatherAPI.com
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) {

    /**
     * Get current weather for given coordinates
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     * @return WeatherResponse with current conditions
     */
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getCurrentWeather(
                    apiKey = WeatherApiService.API_KEY,
                    location = "$latitude,$longitude"
                )
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Get weather by city name
     * @param cityName Name of the city
     * @return WeatherResponse with current conditions
     */
    suspend fun getWeatherByCity(cityName: String): Result<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApiService.getCurrentWeather(
                    apiKey = WeatherApiService.API_KEY,
                    location = cityName
                )
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
