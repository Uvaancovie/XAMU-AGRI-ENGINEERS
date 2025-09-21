package com.example.xamu_wil_project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherSoilDao {
    @Insert
    fun insert(entry: WeatherSoilEntity): Long

    @Query("SELECT * FROM weather_soil WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getByClientFlow(clientId: Long): Flow<List<WeatherSoilEntity>>

    @Query("DELETE FROM weather_soil WHERE clientId = :clientId")
    fun clearForClient(clientId: Long): Int
}

