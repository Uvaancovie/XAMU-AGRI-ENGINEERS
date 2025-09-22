package com.example.xamu_wil_project.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.ProjectEntity
import com.example.xamu_wil_project.data.local.ProjectRepository

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ProjectRepository.getInstance(application)

    fun observeAll(): LiveData<List<ProjectEntity>> = repo.observeAll()
    fun observeForCompany(company: String): LiveData<List<ProjectEntity>> = repo.observeForCompany(company)

    // wrappers for insert/update
    suspend fun insert(entity: ProjectEntity): Long = repo.insert(entity)
    suspend fun update(entity: ProjectEntity): Int = repo.update(entity)
}

