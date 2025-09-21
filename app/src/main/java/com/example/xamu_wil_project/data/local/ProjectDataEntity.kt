package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_data")
data class ProjectDataEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var companyName: String = "",
    var projectName: String = "",
    var locationStamp: String = "",
    // store serialized JSON of BiophysicalAttributes and PhaseImpacts for simplicity
    var biophysicalJson: String = "",
    var phaseJson: String = "",
    var timestamp: Long = System.currentTimeMillis()
)
