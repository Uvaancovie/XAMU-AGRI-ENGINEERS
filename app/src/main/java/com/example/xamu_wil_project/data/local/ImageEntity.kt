package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var companyName: String = "",
    var projectName: String = "",
    var storagePath: String = "",
    var description: String = "",
    var location: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

