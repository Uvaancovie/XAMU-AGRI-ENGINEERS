package com.example.xamu_wil_project.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyEmail: String,
    val companyName: String,
    val appUserUsername: String,
    val projectName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Extension functions for model conversion
fun ProjectEntity.toModel() = com.example.xamu_wil_project.data.Project(
    id = this.id,
    companyEmail = this.companyEmail,
    companyName = this.companyName,
    appUserUsername = this.appUserUsername,
    projectName = this.projectName
)

fun com.example.xamu_wil_project.data.Project.toEntity() = ProjectEntity(
    id = this.id ?: 0,
    companyEmail = this.companyEmail.orEmpty(),
    companyName = this.companyName.orEmpty(),
    appUserUsername = this.appUserUsername.orEmpty(),
    projectName = this.projectName.orEmpty()
)
