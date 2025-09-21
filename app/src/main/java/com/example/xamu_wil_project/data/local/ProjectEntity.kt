package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var projectName: String = "",
    var companyName: String = "",
    var appUserUsername: String = "",
    var companyEmail: String = ""
)

fun ProjectEntity.toModel() = com.example.xamu_wil_project.data.Project(
    projectName = this.projectName,
    companyName = this.companyName,
    appUserUsername = this.appUserUsername,
    companyEmail = this.companyEmail,
    id = this.id
)

fun com.example.xamu_wil_project.data.Project.toEntity() = ProjectEntity(
    id = this.id ?: 0,
    projectName = this.projectName.orEmpty(),
    companyName = this.companyName.orEmpty(),
    appUserUsername = this.appUserUsername.orEmpty(),
    companyEmail = this.companyEmail.orEmpty()
)
