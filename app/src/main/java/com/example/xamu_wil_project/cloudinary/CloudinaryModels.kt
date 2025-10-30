package com.example.xamu_wil_project.cloudinary

data class ProjectPhotoItem(
    val publicId: String,
    val caption: String?,
    val createdAt: Long
)

data class ProjectManifest(
    val projectId: String,
    val items: MutableList<ProjectPhotoItem> = mutableListOf()
)
