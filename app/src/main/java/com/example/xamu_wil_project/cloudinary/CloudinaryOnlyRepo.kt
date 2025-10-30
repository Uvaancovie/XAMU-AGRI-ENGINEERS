package com.example.xamu_wil_project.cloudinary

import android.content.Context
import android.net.Uri
import com.example.xamu_wil_project.cloudinary.ProjectPhotoItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.HashMap

class CloudinaryOnlyRepo(private val appContext: Context) {
    companion object {
        private const val DATABASE_URL = "https://xamu-wil-default-rtdb.firebaseio.com/"
    }

    // Read the photo list from Realtime Database (returns newest-first)
    suspend fun list(projectId: String): List<ProjectPhotoItem> {
        return try {
            val db = FirebaseDatabase.getInstance(DATABASE_URL)
            val ref = db.getReference("CloudinaryPhotos/$projectId")
            val snapshot = ref.get().await()
            val list = mutableListOf<ProjectPhotoItem>()
            for (child in snapshot.children) {
                val publicId = child.child("publicId").getValue(String::class.java) ?: continue
                val caption = child.child("caption").getValue(String::class.java)
                val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L
                list.add(ProjectPhotoItem(publicId = publicId, caption = caption, createdAt = createdAt))
            }
            list.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun upload(
        projectId: String,
        uri: Uri,
        caption: String?,
        onProgress: (Int) -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        CloudinaryHelper.uploadImage(
            projectId = projectId,
            imageUri = uri,
            caption = caption,
            onProgress = onProgress,
            onSuccess = { publicId ->
                // Persist metadata to Realtime Database so app can list images by date
                try {
                    val db = FirebaseDatabase.getInstance(DATABASE_URL)
                    val ref = db.getReference("CloudinaryPhotos/$projectId").push()

                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    val entry = HashMap<String, Any?>()
                    entry["publicId"] = publicId
                    entry["caption"] = caption
                    entry["createdAt"] = System.currentTimeMillis()
                    entry["userId"] = uid

                    ref.setValue(entry)
                        .addOnSuccessListener { onDone() }
                        .addOnFailureListener { ex -> onError(ex.message ?: "DB write failed") }
                } catch (e: Exception) {
                    onError(e.message ?: "DB error")
                }
            },
            onError = onError
        )
    }
}