package com.example.xamu_wil_project.data

import com.google.firebase.database.Exclude

data class AppUser(
    val email: String? = "",
    val firstname: String? = "",
    val lastname: String? = "",
    val qualification: String? = ""
)

data class Client(
    var companyName: String? = "",
    var companyRegNum: String? = "",
    var companyType: String? = "",
    var email: String? = "",
    var companyEmail: String? = "",
    var contactPerson: String? = "",
    var phoneNumber: String? = "",
    var address: String? = "",
    var id: Long? = null
)

data class Project(
    var projectName: String? = "",
    var companyName: String? = "",
    var appUserUsername: String? = "",
    var companyEmail: String? = "",
    var createdAt: Long = System.currentTimeMillis(),
    var id: Long? = null
)

data class BiophysicalAttributes(
    @get:Exclude var id: String = "", // Exclude from Firebase serialization
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = "",
    val elevation: String = "",
    val ecoregion: String = "",
    val map: String = "",
    val rainfall: String = "",
    val evapotranspiration: String = "",
    val geology: String = "",
    val waterManagementArea: String = "",
    val soilErodibility: String = "",
    val vegetationType: String = "",
    val conservationStatus: String = "",
    val fepa: String = ""
)

data class PhaseImpacts(
    val runoffHardSurfaces: String = "",
    val runoffSepticTanks: String = "",
    val sedimentInput: String = "",
    val floodPeaks: String = "",
    val pollution: String = "",
    val weedsIAP: String = ""
)

data class Note(
    var note: String? = "",
    var location: String? = "",
    var timestamp: Long = System.currentTimeMillis(),
    var userId: String? = "",
    var imageUrl: String? = null
)

data class RoutePoint(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val elevation: Double = 0.0,
    val accuracy: Float = 0f
)

data class Route(
    val routeId: String = "",
    val projectName: String = "",
    val companyName: String = "",
    val userId: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val points: List<RoutePoint> = emptyList(),
    val totalDistance: Double = 0.0,
    val isActive: Boolean = true
)

data class WeatherSoilData(
    val temperature: Double = 0.0,
    val humidity: Int = 0,
    val condition: String = "",
    val windSpeed: Double = 0.0,
    val pressure: Double = 0.0,
    val soilMoisture: String? = null,
    val soilPh: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String = ""
)

data class UserSettings(
    val theme: String = "system", // light, dark, system
    val language: String = "en", // en, af
    val notificationsEnabled: Boolean = true,
    val autoSync: Boolean = true,
    val offlineMapsEnabled: Boolean = false,
    val biometricEnabled: Boolean = false
)

data class FieldPhoto(
    val photoId: String = "",
    val url: String = "",
    val thumbnailUrl: String? = null,
    val caption: String = "",
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = "",
    val uploadStatus: String = "pending" // pending, uploading, completed, failed
)

data class ProjectData(
    val biophysical: List<BiophysicalAttributes> = emptyList(),
    val impacts: PhaseImpacts? = null,
    val notes: List<Note> = emptyList(),
    val routes: List<Route> = emptyList(),
    val photos: List<FieldPhoto> = emptyList(),
    val weatherData: List<WeatherSoilData> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)
