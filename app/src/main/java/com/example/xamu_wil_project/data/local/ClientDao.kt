package com.example.xamu_wil_project.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY companyName ASC")
    fun getAllFlow(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    fun getById(id: Long): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(client: ClientEntity): Long

    @Update
    fun update(client: ClientEntity): Int

    @Query("DELETE FROM clients")
    fun clearAll(): Int
}
