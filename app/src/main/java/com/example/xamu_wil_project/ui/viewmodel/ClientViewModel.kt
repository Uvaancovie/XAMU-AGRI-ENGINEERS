package com.example.xamu_wil_project.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.local.AppDatabase
import com.example.xamu_wil_project.data.local.ClientEntity
import com.example.xamu_wil_project.data.local.ClientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClientViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ClientRepository.getInstance(application)
    val allClients: LiveData<List<ClientEntity>> = repo.observeAll()

    fun insert(client: ClientEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repo.insert(client)
            onDone(id)
        }
    }

    fun update(client: ClientEntity, onDone: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val rows = repo.update(client)
            onDone(rows)
        }
    }

    fun clearAll(onDone: (Int) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val rows = repo.clearAll()
            onDone(rows)
        }
    }
}

