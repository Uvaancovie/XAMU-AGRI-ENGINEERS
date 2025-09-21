package com.example.xamu_wil_project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDataDao {
    @Query("SELECT * FROM project_data WHERE companyName = :company AND projectName = :project ORDER BY timestamp DESC")
    fun getByCompanyProjectFlow(company: String, project: String): Flow<List<ProjectDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: ProjectDataEntity): Long

    @Query("DELETE FROM project_data WHERE companyName = :company AND projectName = :project")
    fun clearForProject(company: String, project: String): Int
}
