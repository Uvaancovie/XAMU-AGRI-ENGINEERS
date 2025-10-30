package com.example.xamu_wil_project.cloudinary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {
    @Provides
    fun provideBytescaleRepo(@ApplicationContext context: Context): BytescaleRepo = BytescaleRepo(context)

    @Provides
    fun provideProjectPhotosViewModelFactory(repo: BytescaleRepo): ProjectPhotosViewModelFactory = ProjectPhotosViewModelFactory(repo)
}

class ProjectPhotosViewModelFactory(private val repo: BytescaleRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectPhotosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectPhotosViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
