package com.example.xamu_wil_project.cloudinary

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectPhotosViewModel(private val repo: BytescaleRepo) : ViewModel() {
    private val _photos = MutableStateFlow<List<ProjectPhotoItem>>(emptyList())
    val photos = _photos.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status = _status.asStateFlow()

    fun refresh(projectId: String) {
        Log.d("ProjectPhotosVM", "Refreshing photos for projectId=$projectId")
        viewModelScope.launch(Dispatchers.IO) {
            val photoList = repo.listProjectPhotos(projectId)
            Log.d("ProjectPhotosVM", "Got ${photoList.size} photos from repo")
            _photos.value = photoList
        }
    }

    fun loadFromRealtimeDb(projectId: String) {
        Log.d("ProjectPhotosVM", "Loading photos from Realtime DB for projectId=$projectId")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = repo.readPhotosFromRealtimeDb(projectId)
                Log.d("ProjectPhotosVM", "RTDB returned ${list.size} photos")
                _photos.value = list
                _status.value = "Loaded ${list.size} photos from Firebase"
            } catch (e: Exception) {
                Log.e("ProjectPhotosVM", "Failed to load from RTDB", e)
                _status.value = "RTDB load failed: ${e.message}"
            }
        }
    }

    fun upload(projectId: String, uri: Uri, caption: String?) {
        Log.d("ProjectPhotosVM", "Uploading photo for projectId=$projectId, caption=$caption")
        viewModelScope.launch {
            _status.value = "Uploading…"

            val createdAt = System.currentTimeMillis()

            val result = repo.uploadImage(
                projectId = projectId,
                imageUri = uri,
                caption = caption,
                createdAtMillis = createdAt,
                onProgress = { p -> _status.value = "Uploading… $p%" }
            )

            if (result.isSuccess) {
                val fileUrl = result.getOrNull()
                Log.d("ProjectPhotosVM", "Upload successful: $fileUrl")
                _status.value = "Uploaded successfully"

                // Optimistically add the uploaded photo so user sees it immediately
                if (!fileUrl.isNullOrBlank()) {
                    val newItem = ProjectPhotoItem(
                        publicId = fileUrl,
                        caption = caption,
                        createdAt = createdAt
                    )
                    // Prepend to the list so newest appears first
                    _photos.value = listOf(newItem) + _photos.value
                }

                // Still attempt to refresh from server in background
                refresh(projectId)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                Log.e("ProjectPhotosVM", "Upload failed: $error")
                _status.value = "Error: $error"
            }
        }
    }
}
