package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var companyName: String = "",
    var projectName: String = "",
    var routeName: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    // coordinates stored as JSON array string: [{"lat":..,"lon":..}, ...]
    var coordsJson: String = ""
)

