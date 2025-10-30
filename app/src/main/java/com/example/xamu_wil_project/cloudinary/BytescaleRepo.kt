package com.example.xamu_wil_project.cloudinary

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.buffer
import okio.source
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Bytescale repository for XAMU Field Photos
 * - Uploads images with caption + created_at metadata
 * - Lists project photos with metadata
 * - Persists metadata to Firebase Realtime DB as a fallback when Bytescale listing is forbidden
 */
class BytescaleRepo(private val context: Context) {
    companion object {
        private const val TAG = "BytescaleRepo"
        private const val ACCOUNT_ID = "W23MTQd"
        // Using the secret API key so the app can list and read file details (fixes 403 forbidden)
        // NOTE: For production, move this secret to a secure store (Server / Keystore / BuildConfig)
        private const val API_KEY = "secret_W23MTQd8x9mGxbtchdJ7sik1NA93"
        private const val BASE = "https://api.bytescale.com/v2/accounts/$ACCOUNT_ID"
        private const val ROOT_FOLDER = "/xamu-field"
    }

    private val client = OkHttpClient()

    private val firebaseDb by lazy { FirebaseDatabase.getInstance("https://xamu-wil-default-rtdb.firebaseio.com/") }

    /**
     * Upload image to Bytescale with caption and timestamp metadata
     * Returns the public URL of the uploaded image
     */
    suspend fun uploadImage(
        projectId: String,
        imageUri: Uri,
        caption: String?,
        createdAtMillis: Long = System.currentTimeMillis(),
        onProgress: (Int) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cr = context.contentResolver
            val mime = cr.getType(imageUri) ?: "image/jpeg"
            val fileName = "photo_${createdAtMillis}.jpg"
            val folderPath = "$ROOT_FOLDER/$projectId"

            val metadataJson = JSONObject().apply {
                put("caption", caption ?: "")
                put("created_at", createdAtMillis.toString())
            }.toString()

            Log.d(TAG, "Uploading to projectId=$projectId, folderPath=$folderPath, fileName=$fileName")
            Log.d(TAG, "Metadata: $metadataJson")

            // Build streaming RequestBody
            val requestBody = object : RequestBody() {
                override fun contentType() = mime.toMediaType()
                override fun writeTo(sink: okio.BufferedSink) {
                    cr.openInputStream(imageUri)?.source()?.buffer()?.use { source ->
                        sink.writeAll(source)
                    } ?: throw IOException("Unable to open input stream for uri: $imageUri")
                }
            }

            // Bytescale requires Bearer token in Authorization header
            val url = "$BASE/uploads/binary?folderPath=$folderPath&fileName=$fileName&originalFileName=$fileName"

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", mime)
                .header("X-Upload-Metadata", metadataJson)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                Log.d(TAG, "Upload response code: ${response.code}, body: $body")

                if (!response.isSuccessful) {
                    Log.e(TAG, "Upload failed: ${response.code} ${response.message} body=$body")
                    return@withContext Result.failure(Exception("Upload failed: ${response.code} ${response.message}"))
                }

                val json = JSONObject(body)
                val fileUrl = json.optString("fileUrl")
                if (fileUrl.isBlank()) {
                    Log.e(TAG, "Upload response missing fileUrl: $body")
                    return@withContext Result.failure(Exception("Upload response missing file URL"))
                }

                Log.d(TAG, "Upload successful: $fileUrl")

                // Persist metadata to Firebase Realtime DB as fallback for listing permissions
                try {
                    val ref = firebaseDb.getReference("ProjectPhotos").child(projectId).push()
                    val payload = mapOf(
                        "fileUrl" to fileUrl,
                        "caption" to (caption ?: ""),
                        "createdAt" to createdAtMillis
                    )
                    val task = ref.setValue(payload)
                    task.addOnSuccessListener {
                        Log.d(TAG, "Persisted photo metadata to RTDB at ${ref.path}")
                    }
                    task.addOnFailureListener { ex ->
                        Log.w(TAG, "Failed to persist to RTDB", ex)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to persist to RTDB", e)
                }

                return@withContext Result.success(fileUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadImage exception", e)
            Result.failure(e)
        }
    }

    /**
     * Suspend helper to read photos stored in Realtime DB for a project
     */
    suspend fun readPhotosFromRealtimeDb(projectId: String): List<ProjectPhotoItem> = suspendCancellableCoroutine { cont ->
        try {
            val ref = firebaseDb.getReference("ProjectPhotos").child(projectId)
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val list = mutableListOf<ProjectPhotoItem>()
                        for (child in snapshot.children) {
                            val fileUrl = child.child("fileUrl").getValue(String::class.java) ?: continue
                            val caption = child.child("caption").getValue(String::class.java)
                            val createdAt = child.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                            list.add(ProjectPhotoItem(publicId = fileUrl, caption = caption, createdAt = createdAt))
                        }
                        cont.resume(list)
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    cont.resumeWithException(RuntimeException("RTDB cancelled: ${error.message}"))
                }
            }
            ref.addListenerForSingleValueEvent(listener)
            cont.invokeOnCancellation { try { ref.removeEventListener(listener) } catch (_: Exception) {} }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

    /**
     * List all photos for a project with their metadata
     * Attempts Bytescale list; if 403 Forbidden, falls back to Realtime DB
     */
    suspend fun listProjectPhotos(projectId: String): List<ProjectPhotoItem> = withContext(Dispatchers.IO) {
        try {
            val folderPath = "$ROOT_FOLDER/$projectId"
            val listUrl = "$BASE/folders/list?folderPath=$folderPath"

            Log.d(TAG, "Listing photos for projectId=$projectId, folderPath=$folderPath")

            val listRequest = Request.Builder()
                .url(listUrl)
                .header("Authorization", "Bearer $API_KEY")
                .build()

            client.newCall(listRequest).execute().use { listResponse ->
                val body = listResponse.body?.string().orEmpty()
                Log.d(TAG, "List response code: ${listResponse.code}, body: $body")

                if (listResponse.code == 403) {
                    Log.w(TAG, "Bytescale listing forbidden (403). Falling back to Realtime DB for projectId=$projectId")
                    return@withContext try {
                        readPhotosFromRealtimeDb(projectId)
                    } catch (e: Exception) {
                        Log.e(TAG, "RTDB fallback failed", e)
                        emptyList()
                    }
                }

                if (!listResponse.isSuccessful) {
                    Log.e(TAG, "List failed: ${listResponse.code} ${listResponse.message}")
                    return@withContext emptyList()
                }

                if (body.isBlank()) {
                    Log.w(TAG, "List response is blank")
                    return@withContext emptyList()
                }

                val json = JSONObject(body)
                val items = json.optJSONArray("items")

                if (items == null) {
                    Log.w(TAG, "No items array in response. Available keys: ${json.keys().asSequence().toList()}")
                    return@withContext emptyList()
                }

                Log.d(TAG, "Found ${items.length()} items in folder")

                val photos = mutableListOf<ProjectPhotoItem>()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val filePath = item.optString("filePath", "")
                    val fileUrl = item.optString("fileUrl", "")

                    Log.d(TAG, "Item $i: filePath=$filePath, fileUrl=$fileUrl")

                    if (filePath.isBlank() || fileUrl.isBlank()) {
                        Log.w(TAG, "Skipping item $i: filePath or fileUrl is blank")
                        continue
                    }

                    // Fetch file details to get metadata
                    val detailsUrl = "$BASE/files/details?filePath=$filePath"
                    val detailsRequest = Request.Builder()
                        .url(detailsUrl)
                        .header("Authorization", "Bearer $API_KEY")
                        .build()

                    try {
                        client.newCall(detailsRequest).execute().use { detailsResponse ->
                            if (detailsResponse.isSuccessful) {
                                val detailsBody = detailsResponse.body?.string().orEmpty()
                                Log.d(TAG, "Details for $filePath: $detailsBody")

                                val detailsJson = JSONObject(detailsBody)
                                val metadata = detailsJson.optJSONObject("metadata")

                                val caption = metadata?.optString("caption")
                                val createdAtStr = metadata?.optString("created_at")
                                val createdAt = createdAtStr?.toLongOrNull() ?: System.currentTimeMillis()

                                Log.d(TAG, "Parsed: caption=$caption, createdAt=$createdAt")

                                // Use fileUrl as publicId for compatibility with existing UI
                                photos.add(
                                    ProjectPhotoItem(
                                        publicId = fileUrl,
                                        caption = caption,
                                        createdAt = createdAt
                                    )
                                )
                            } else {
                                Log.e(TAG, "Details request failed: ${detailsResponse.code}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching details for $filePath", e)
                    }
                }

                Log.d(TAG, "Returning ${photos.size} photos")
                photos.sortedByDescending { it.createdAt }
            }
        } catch (e: Exception) {
            Log.e(TAG, "listProjectPhotos exception", e)
            emptyList()
        }
    }
}
