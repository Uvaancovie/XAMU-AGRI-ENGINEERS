package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val projectName: String,
    val routeName: String,
    val coordinates: String, // JSON string of coordinate array
    val timestamp: Long = System.currentTimeMillis()
)
