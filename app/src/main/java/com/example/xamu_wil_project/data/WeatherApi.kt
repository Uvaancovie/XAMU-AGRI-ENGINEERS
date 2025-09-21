package com.example.xamu_wil_project.data

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class WeatherMain(val temp: Double, val pressure: Int, val humidity: Int)
data class WeatherDesc(val description: String, val icon: String)
data class WeatherResponse(val main: WeatherMain, val weather: List<WeatherDesc>)

interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(@Query("lat") lat: Double, @Query("lon") lon: Double): Response<WeatherResponse>

    companion object {
        fun create(): WeatherApi = Retrofit.Builder()
            .baseUrl("https://weather-api-zxp0.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}
