package com.example.xamu_wil_project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE companyName = :company ORDER BY projectName ASC")
    fun getByCompanyFlow(company: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY projectName ASC")
    fun getAllFlow(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(project: ProjectEntity): Long

    @Update
    fun update(project: ProjectEntity): Int

    @Query("DELETE FROM projects")
    fun clearAll(): Int
}
