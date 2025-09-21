package com.example.xamu_wil_project.data.local

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherSoilRepository private constructor(private val db: AppDatabase) {

    fun observeForClient(clientId: Long): LiveData<List<WeatherSoilEntity>> = db.weatherSoilDao().getByClientFlow(clientId).asLiveData()

    suspend fun insert(entry: WeatherSoilEntity): Long = withContext(Dispatchers.IO) { db.weatherSoilDao().insert(entry) }

    suspend fun clearForClient(clientId: Long): Int = withContext(Dispatchers.IO) { db.weatherSoilDao().clearForClient(clientId) }

    companion object {
        @Volatile
        private var INSTANCE: WeatherSoilRepository? = null

        fun getInstance(context: Context): WeatherSoilRepository = INSTANCE ?: synchronized(this) {
            val inst = WeatherSoilRepository(AppDatabase.getInstance(context))
            INSTANCE = inst
            inst
        }
    }
}

