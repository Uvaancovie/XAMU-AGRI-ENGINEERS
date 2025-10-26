# 🌤️ WeatherAPI Integration — Xamu Wetlands

This document describes how to integrate the **WeatherAPI** service (https://www.weatherapi.com/docs/) into the **Xamu Wetlands Android application** using Kotlin, Retrofit, Hilt, and MVVM architecture.

---

## 🔑 API Details

**Base URL:**
https://api.weatherapi.com/v1/



**API Key:**  
`170bd511de404e92aa8222345250910`

**Example Endpoint:**

**Query Parameters**

| Parameter | Description | Example |
|:--|:--|:--|
| `key` | WeatherAPI API key | `170bd511de404e92aa8222345250910` |
| `q` | Location (city name or lat,lon) | `Durban` or `-29.8579,31.0292` |
| `aqi` | Include air quality index | `no` |
| `days` | Forecast days (optional) | `3` |

---

## ⚙️ Retrofit Setup

### Step 1 — Add Dependencies

In your **app/build.gradle**:
```gradle
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
https://api.weatherapi.com/v1/current.json?key=170bd511de404e92aa8222345250910&q=-29.8579,31.0292&aqi=no


**Query Parameters**

| Parameter | Description | Example |
|:--|:--|:--|
| `key` | WeatherAPI API key | `170bd511de404e92aa8222345250910` |
| `q` | Location (city name or lat,lon) | `Durban` or `-29.8579,31.0292` |
| `aqi` | Include air quality index | `no` |
| `days` | Forecast days (optional) | `3` |

---

## ⚙️ Retrofit Setup

### Step 1 — Add Dependencies

In your **app/build.gradle**:
```gradle
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
// WeatherApiService.kt
package com.xamu.wetlands.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") aqi: String = "no"
    ): WeatherResponse
}
// WeatherResponse.kt
package com.xamu.wetlands.data.model

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
    val condition: Condition,
    val humidity: Int,
    val pressure_mb: Double,
    val wind_kph: Double,
    val feelslike_c: Double,
    val uv: Double
)

data class Condition(
    val text: String,
    val icon: String
)
// NetworkModule.kt
package com.xamu.wetlands.di

import com.xamu.wetlands.data.remote.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.weatherapi.com/v1/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)
}
// WeatherRepository.kt
package com.xamu.wetlands.data.repository

import com.xamu.wetlands.data.remote.WeatherApiService
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: WeatherApiService
) {
    suspend fun getWeather(lat: Double, lon: Double) =
        api.getCurrentWeather(
            apiKey = "170bd511de404e92aa8222345250910",
            location = "$lat,$lon"
        )
}
// WeatherViewModel.kt
package com.xamu.wetlands.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xamu.wetlands.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repo: WeatherRepository
): ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val state = _state.asStateFlow()

    fun loadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                _state.value = WeatherUiState.Loading
                val result = repo.getWeather(lat, lon)
                _state.value = WeatherUiState.Success(result)
            } catch (e: Exception) {
                _state.value = WeatherUiState.Error(e.localizedMessage ?: "Error")
            }
        }
    }
}

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val data: Any) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}
// WeatherDialog.kt
package com.xamu.wetlands.ui.weather

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.xamu.wetlands.R
import com.xamu.wetlands.data.model.WeatherResponse

fun showWeatherDialog(context: Context, data: WeatherResponse) {
    val dialog = Dialog(context)
    val view = LayoutInflater.from(context).inflate(R.layout.dialog_weather, null)
    dialog.setContentView(view)

    view.findViewById<TextView>(R.id.tvTemperature).text = "${data.current.temp_c}°C"
    view.findViewById<TextView>(R.id.tvCondition).text = data.current.condition.text
    view.findViewById<TextView>(R.id.tvHumidity).text = "Humidity: ${data.current.humidity}%"
    view.findViewById<TextView>(R.id.tvWind).text = "Wind: ${data.current.wind_kph} kph"
    Glide.with(context).load("https:${data.current.condition.icon}")
        .into(view.findViewById<ImageView>(R.id.ivWeatherIcon))

    dialog.show()
}
GET https://api.weatherapi.com/v1/current.json?key=170bd511de404e92aa8222345250910&q=-29.8587,31.0218&aqi=no
{
  "location": {
    "name": "Durban",
    "region": "KwaZulu-Natal",
    "country": "South Africa",
    "lat": -29.86,
    "lon": 31.02,
    "localtime": "2025-10-09 16:00"
  },
  "current": {
    "temp_c": 25.3,
    "condition": {
      "text": "Partly Cloudy",
      "icon": "//cdn.weatherapi.com/weather/64x64/day/116.png"
    },
    "humidity": 74,
    "wind_kph": 15.0,
    "pressure_mb": 1014,
    "feelslike_c": 27.0
  }
}
