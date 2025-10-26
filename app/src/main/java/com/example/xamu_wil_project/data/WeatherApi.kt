package com.example.xamu_wil_project.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * WeatherAPI.com Integration for Xamu Wetlands
 * Professional weather service using WeatherAPI.com
 */
interface WeatherApiService {
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String, // lat,lon format
        @Query("aqi") aqi: String = "no"
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.weatherapi.com/v1/"
        const val API_KEY = "170bd511de404e92aa8222345250910" // Your provided API key

        fun create(): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}

// WeatherAPI.com Response Models
data class WeatherResponse(
    val location: Location,
    val current: Current
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val localtime: String
)

data class Current(
    val temp_c: Double,
    val temp_f: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Double,
    val wind_kph: Double,
    val wind_degree: Int,
    val wind_dir: String,
    val pressure_mb: Double,
    val pressure_in: Double,
    val precip_mm: Double,
    val precip_in: Double,
    val humidity: Int,
    val cloud: Int,
    val feelslike_c: Double,
    val feelslike_f: Double,
    val vis_km: Double,
    val vis_miles: Double,
    val uv: Double
)

data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)

// Legacy compatibility for existing code
data class WeatherApi(
    val main: WeatherMain,
    val weather: List<WeatherInfo>,
    val visibility: Int?
) {
    companion object {
        fun fromWeatherApi(response: WeatherResponse): WeatherApi {
            return WeatherApi(
                main = WeatherMain(
                    temp = response.current.temp_c,
                    feels_like = response.current.feelslike_c,
                    humidity = response.current.humidity,
                    pressure = response.current.pressure_mb
                ),
                weather = listOf(
                    WeatherInfo(
                        main = response.current.condition.text,
                        description = response.current.condition.text
                    )
                ),
                visibility = (response.current.vis_km * 1000).toInt()
            )
        }
    }
}

data class WeatherMain(
    val temp: Double,
    val feels_like: Double?,
    val humidity: Int,
    val pressure: Double
)

data class WeatherInfo(
    val main: String,
    val description: String
)
