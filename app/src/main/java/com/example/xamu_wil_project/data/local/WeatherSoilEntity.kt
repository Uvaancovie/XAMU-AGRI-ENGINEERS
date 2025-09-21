package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores weather and soil observations tied to a client (by clientId).
 */
@Entity(tableName = "weather_soil")
data class WeatherSoilEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var clientId: Long = 0,
    var temperatureC: Double? = null,
    var humidityPct: Double? = null,
    var pressureHPa: Double? = null,
    var soilMoisturePct: Double? = null,
    var soilPH: Double? = null,
    var notes: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

