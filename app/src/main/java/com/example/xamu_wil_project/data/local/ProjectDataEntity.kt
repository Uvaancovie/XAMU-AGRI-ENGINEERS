package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_data")
data class ProjectDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val projectName: String,
    val dataType: String, // "BiophysicalAttributes" or "PhaseImpacts"
    val locationStamp: String = "", // Field that AddDataToProjectActivity expects
    val biophysicalJson: String = "", // Field that AddDataToProjectActivity expects
    val phaseJson: String = "", // Field that AddDataToProjectActivity expects
    val elevation: String = "",
    val ecoregion: String = "",
    val mapValue: String = "", // MAP (Mean Annual Precipitation)
    val rainfallSeasonality: String = "",
    val evapotranspiration: String = "",
    val geology: String = "",
    val waterManagementArea: String = "",
    val soilErodibility: String = "",
    val vegetationType: String = "",
    val conservationStatus: String = "",
    val fepaFeatures: String = "",
    val runoffHardSurfaces: String = "",
    val septicRunoff: String = "",
    val sedimentInput: String = "",
    val floodPeaks: String = "",
    val pollution: String = "",
    val weedsIAP: String = "",
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
