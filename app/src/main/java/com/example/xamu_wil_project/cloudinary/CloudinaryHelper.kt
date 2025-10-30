package com.example.xamu_wil_project.cloudinary

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

object CloudinaryHelper {
    // Make preset mutable so UI and Application can change it at runtime
    private var preset: String = "XAMU-FIELD"
    private const val CLOUD = "dir468aeq"

    private val http by lazy { OkHttpClient() }

    fun setUploadPreset(p: String) {
        preset = p
    }

    fun currentPreset(): String = preset

    fun deliveryUrl(publicId: String, width: Int = 1200): String {
        val t = "f_auto,q_auto,w_${width},c_fill,g_auto"
        return "https://res.cloudinary.com/$CLOUD/image/upload/$t/$publicId"
    }

    /** Upload a single image (unsigned) under xamu-field/{projectId}/... */
    fun uploadImage(
        projectId: String,
        imageUri: Uri,
        caption: String?,
        onProgress: (Int) -> Unit = {},
        onSuccess: (publicId: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val fileName = "${System.currentTimeMillis()}-${UUID.randomUUID()}"
        val folder = "xamu-field/$projectId"

        MediaManager.get().upload(imageUri)
            .unsigned(preset)
            .option("folder", folder)
            .option("public_id", fileName)
            .option("context", "caption=${caption ?: ""}")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val pct = if (totalBytes > 0) ((bytes * 100) / totalBytes).toInt() else 0
                    onProgress(pct)
                }
                override fun onSuccess(requestId: String?, result: Map<Any?, Any?>?) {
                    val publicId = (result?.get("public_id") as? String)
                        ?: "$folder/$fileName"
                    onSuccess(publicId)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Upload failed")
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    onError("Rescheduled: ${error?.description}")
                }
            })
            .dispatch()
    }

    /** Get manifest JSON (or empty) from: /raw/upload/xamu-field/{projectId}/manifest.json */
    fun fetchManifest(projectId: String): ProjectManifest {
        val url = "https://res.cloudinary.com/$CLOUD/raw/upload/xamu-field/$projectId/manifest.json"
        return try {
            val req = Request.Builder().url(url).get().build()
            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return ProjectManifest(projectId)
                val body = resp.body?.string().orEmpty()
                if (body.isBlank()) return ProjectManifest(projectId)
                val json = JSONObject(body)
                val arr = json.optJSONArray("items") ?: JSONArray()
                val items = mutableListOf<ProjectPhotoItem>()
                for (i in 0 until arr.length()) {
                    val it = arr.getJSONObject(i)
                    items.add(
                        ProjectPhotoItem(
                            publicId = it.getString("publicId"),
                            caption = if (it.has("caption")) it.optString("caption", null) else null,
                            createdAt = it.optLong("createdAt", 0L)
                        )
                    )
                }
                ProjectManifest(projectId, items)
            }
        } catch (_: Exception) {
            ProjectManifest(projectId)
        }
    }

    /** Upload (raw) manifest.json as a RAW resource under the project folder. */
    fun uploadManifest(
        context: Context,
        projectId: String,
        manifest: ProjectManifest,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val folder = "xamu-field/$projectId"
        val tmp = File(context.cacheDir, "manifest_${projectId}.json")

        // Serialize
        val arr = JSONArray()
        manifest.items.forEach { item ->
            val obj = JSONObject()
            obj.put("publicId", item.publicId)
            if (item.caption != null) obj.put("caption", item.caption)
            obj.put("createdAt", item.createdAt)
            arr.put(obj)
        }
        val root = JSONObject()
        root.put("projectId", projectId)
        root.put("items", arr)
        tmp.writeText(root.toString())

        // Upload as RAW (no overwrite for unsigned uploads)
        MediaManager.get().upload(tmp.absolutePath)
            .unsigned(preset)
            .option("folder", folder)
            .option("public_id", "manifest")
            .option("resource_type", "raw")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, result: Map<Any?, Any?>?) { onSuccess() }
                override fun onError(requestId: String?, error: ErrorInfo?) { onError(error?.description ?: "Manifest upload failed") }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) { onError("Rescheduled: ${error?.description}") }
            })
            .dispatch()
    }
}