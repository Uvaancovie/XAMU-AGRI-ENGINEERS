package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores weather and soil observations tied to a client (by clientId).
 */
@Entity(tableName = "weather_soil")
data class WeatherSoilEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val dataType: String, // "weather" or "soil"
    val temperature: String = "", // Changed to String to match UI input
    val humidity: String = "", // Changed to String to match UI input
    val pressure: String = "", // Changed to String to match UI input
    val description: String = "",
    val soilType: String = "",
    val soilPh: String = "", // Changed to String to match UI input
    val soilMoisture: String = "", // Changed to String to match UI input
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
