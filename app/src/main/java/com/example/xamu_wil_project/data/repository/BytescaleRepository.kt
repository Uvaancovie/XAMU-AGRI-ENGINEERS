package com.example.xamu_wil_project.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.xamu_wil_project.data.FieldPhoto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.buffer
import okio.source
import org.json.JSONObject
import java.io.IOException

/**
 * Minimal Bytescale repository for uploading images with metadata (caption + created_at).
 * Uses the public API key (browser/public key). For production consider server-signed uploads.
 */
class BytescaleRepository(private val context: Context) {
    companion object {
        private const val TAG = "BytescaleRepo"
        private const val ACCOUNT_ID = "W23MTQd" // update if needed
        private const val PUBLIC_API_KEY = "public_W23MTQdAnPhU2FUfKjyryo7t8kRJ-" // public key
        private const val BASE = "https://api.bytescale.com/v2/accounts/$ACCOUNT_ID"
        private const val ROOT_FOLDER = "/xamu-field"
        private const val DATABASE_URL = "https://xamu-wil-default-rtdb.firebaseio.com/"
    }

    private val client = OkHttpClient()

    suspend fun uploadImage(
        projectId: String,
        imageUri: Uri,
        caption: String,
        createdAtMillis: Long = System.currentTimeMillis()
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val cr: ContentResolver = context.contentResolver
            val mime = cr.getType(imageUri) ?: "image/jpeg"
            val fileName = "photo_${createdAtMillis}.jpg"
            val folderPath = "$ROOT_FOLDER/$projectId"

            val metadataJson = JSONObject().apply {
                put("caption", caption)
                put("created_at", createdAtMillis.toString())
            }.toString()

            // Build streaming RequestBody that reads directly from content resolver
            val requestBody = object : RequestBody() {
                override fun contentType() = mime.toMediaType()
                override fun writeTo(sink: okio.BufferedSink) {
                    cr.openInputStream(imageUri)?.source()?.buffer()?.use { source ->
                        sink.writeAll(source)
                    } ?: throw IOException("Unable to open input stream for uri: $imageUri")
                }
            }

            val url = "$BASE/uploads/binary?folderPath=$folderPath&fileName=$fileName&originalFileName=$fileName"

            val req = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $PUBLIC_API_KEY")
                .header("Content-Type", mime)
                .header("X-Upload-Metadata", metadataJson)
                .post(requestBody)
                .build()

            client.newCall(req).execute().use { res ->
                val body = res.body?.string().orEmpty()
                if (!res.isSuccessful) {
                    Log.e(TAG, "Upload failed: ${res.code} ${res.message} body=$body")
                    return@withContext Result.failure(Exception("Upload failed: ${res.code} ${res.message}: $body"))
                }

                // Parse filePath and fileUrl from response JSON
                val json = JSONObject(body)
                val filePath = json.optString("filePath")
                val fileUrl = json.optString("fileUrl")
                if (filePath.isBlank() || fileUrl.isBlank()) {
                    Log.e(TAG, "Upload response missing fields: $body")
                    return@withContext Result.failure(Exception("Upload response missing file path or URL"))
                }

                return@withContext Result.success(Pair(filePath, fileUrl))
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadImage exception", e)
            Result.failure(e)
        }
    }

    /**
     * Upload image to Bytescale and persist a FieldPhoto entry to Realtime Database so
     * ProjectDetails UI (which observes ProjectData) will display the uploaded photo.
     */
    suspend fun uploadAndPersist(
        companyName: String,
        projectName: String,
        projectId: String,
        imageUri: Uri,
        caption: String,
        createdAtMillis: Long = System.currentTimeMillis()
    ): Result<FieldPhoto> = withContext(Dispatchers.IO) {
        try {
            val uploadRes = uploadImage(projectId, imageUri, caption, createdAtMillis)
            if (uploadRes.isFailure) return@withContext Result.failure(uploadRes.exceptionOrNull()!!)

            val (filePath, fileUrl) = uploadRes.getOrNull()!!

            // Build FieldPhoto
            val photoId = System.currentTimeMillis().toString()
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val fieldPhoto = FieldPhoto(
                photoId = photoId,
                url = fileUrl,
                thumbnailUrl = null,
                caption = caption,
                location = "",
                timestamp = createdAtMillis,
                userId = uid,
                uploadStatus = "completed"
            )

            // Persist to Realtime DB under ProjectData/{company}/{project}/Photos/{photoId}
            val db = FirebaseDatabase.getInstance(DATABASE_URL)
            val ref = db.getReference("ProjectData/$companyName/$projectName/Photos/$photoId")
            ref.setValue(fieldPhoto).await()

            return@withContext Result.success(fieldPhoto)
        } catch (e: Exception) {
            Log.e(TAG, "uploadAndPersist failed", e)
            Result.failure(e)
        }
    }
}
