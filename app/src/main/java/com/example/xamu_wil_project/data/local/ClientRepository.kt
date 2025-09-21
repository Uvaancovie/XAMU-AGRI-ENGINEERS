package com.example.xamu_wil_project.data.local

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Thin repository wrapper around ClientDao. Keeps call sites simple and testable.
 */
class ClientRepository private constructor(private val db: AppDatabase) {

    fun observeAll(): LiveData<List<ClientEntity>> = db.clientDao().getAllFlow().asLiveData()

    suspend fun insert(entity: ClientEntity): Long = withContext(Dispatchers.IO) {
        db.clientDao().insert(entity)
    }

    suspend fun update(entity: ClientEntity): Int = withContext(Dispatchers.IO) {
        db.clientDao().update(entity)
    }

    suspend fun clearAll(): Int = withContext(Dispatchers.IO) {
        db.clientDao().clearAll()
    }

    companion object {
        @Volatile
        private var INSTANCE: ClientRepository? = null

        fun getInstance(context: Context): ClientRepository = INSTANCE ?: synchronized(this) {
            val inst = ClientRepository(AppDatabase.getInstance(context))
            INSTANCE = inst
            inst
        }
    }
}

