package com.example.xamu_wil_project.data.local

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProjectRepository private constructor(private val db: AppDatabase) {

    fun observeAll(): LiveData<List<ProjectEntity>> = db.projectDao().getAllFlow().asLiveData()

    fun observeForCompany(company: String): LiveData<List<ProjectEntity>> = db.projectDao().getByCompanyFlow(company).asLiveData()

    suspend fun insert(entity: ProjectEntity): Long = withContext(Dispatchers.IO) { db.projectDao().insert(entity) }

    suspend fun update(entity: ProjectEntity): Int = withContext(Dispatchers.IO) { db.projectDao().update(entity) }

    suspend fun clearAll(): Int = withContext(Dispatchers.IO) { db.projectDao().clearAll() }

    companion object {
        @Volatile
        private var INSTANCE: ProjectRepository? = null

        fun getInstance(context: Context): ProjectRepository = INSTANCE ?: synchronized(this) {
            val inst = ProjectRepository(AppDatabase.getInstance(context))
            INSTANCE = inst
            inst
        }
    }
}

