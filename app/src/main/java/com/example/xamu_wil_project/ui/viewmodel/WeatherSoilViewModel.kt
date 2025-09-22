package com.example.xamu_wil_project.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.local.WeatherSoilEntity
import com.example.xamu_wil_project.data.local.WeatherSoilRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherSoilViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = WeatherSoilRepository.getInstance(application)

    fun insert(entry: WeatherSoilEntity, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repo.insert(entry)
            // return on main thread
            launch(Dispatchers.Main) { onResult(id) }
        }
    }

    fun observeForClient(clientId: Long) = repo.observeForClient(clientId)
}

