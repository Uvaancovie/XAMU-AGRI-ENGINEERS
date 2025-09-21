package com.example.xamu_wil_project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes WHERE companyName = :company AND projectName = :project ORDER BY timestamp DESC")
    fun getByProjectFlow(company: String, project: String): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(route: RouteEntity): Long

    @Query("DELETE FROM routes WHERE companyName = :company AND projectName = :project")
    fun clearForProject(company: String, project: String): Int
}
