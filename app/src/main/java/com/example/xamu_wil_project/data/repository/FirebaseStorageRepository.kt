package com.example.xamu_wil_project.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Storage Repository for Xamu Wetlands
 * Handles photo uploads to Firebase Storage
 */
@Singleton
class FirebaseStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth
) {

    companion object {
        private const val TAG = "FirebaseStorage"
    }

    private val storage = FirebaseStorage.getInstance()

    /**
     * Upload photo to Firebase Storage
     * @param uri Local photo URI
     * @param companyName Client company name
     * @param projectName Project name
     * @param photoId Unique photo identifier
     * @return Public URL of uploaded photo
     */
    suspend fun uploadPhoto(
        uri: Uri,
        companyName: String,
        projectName: String,
        photoId: String
    ): Result<String> {
        return try {
            if (auth.currentUser == null) {
                val errorMessage = "User not authenticated. Cannot upload photo."
                Log.e(TAG, errorMessage)
                return Result.failure(Exception(errorMessage))
            }

            Log.d(TAG, "Starting photo upload: $photoId")

            // Generate storage path
            val path = "projects/$companyName/$projectName/photos/$photoId.jpg"
            val storageRef = storage.reference.child(path)

            Log.d(TAG, "Uploading to: $path")

            // Upload file
            storageRef.putFile(uri).await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await()
            val publicUrl = downloadUrl.toString()

            Log.d(TAG, "Upload completed: $publicUrl")
            Result.success(publicUrl)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload photo: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete photo from Firebase Storage
     */
    suspend fun deletePhoto(photoUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(photoUrl)
            storageRef.delete().await()

            Log.d(TAG, "Deleted photo: $photoUrl")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete photo: ${e.message}", e)
            Result.failure(e)
        }
    }
}
