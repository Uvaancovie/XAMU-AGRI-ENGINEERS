# XAMU Field Photos — **Cloudinary‑Only** Android Implementation (Gemini Agent Context)

This pack gives a Google Gemini agent everything needed to implement **photo uploads with captions** inside the Android app using **Cloudinary only** (no Supabase / no backend). It also includes a **Cloudinary‑hosted manifest** per project so the app can list images without any server.

---

## 0) Quick Vars (use these values)

* **Cloudinary cloud name**: `dir468aeq`
* **Unsigned upload preset**: `XAMU_UNSIG`
  (Create this in Cloudinary Console → Settings → Upload → Upload presets → Add preset; set Mode **Unsigned**)
* **Root folder**: `xamu-field`

> Security: Do **not** put an API Secret in the app. We use an **unsigned** preset and public delivery URLs.

---

## 1) High‑Level Architecture (Cloudinary‑only)

```
Android (Compose)
  ├─ Upload image → Cloudinary (unsigned preset)
  │     └─ stored under: xamu-field/{projectId}/{timestamp-uuid}.jpg (with caption in asset context)
  └─ Maintain a per‑project manifest (JSON) on Cloudinary (resource_type=raw):
        xamu-field/{projectId}/manifest.json

Display: App fetches manifest.json over HTTPS → builds Cloudinary delivery URLs → shows image + caption.
```

Why this works with **no backend**:

* Unsigned uploads allow direct client→Cloudinary uploads (limited, safe params).
* A **public manifest** (`raw/upload/.../manifest.json`) is fetched via HTTPS to list images.
* Captions are stored both in the manifest and (optionally) in the asset’s **context**.

---

## 2) Cloudinary Console Setup

1. **Create unsigned preset** `XAMU_UNSIG`:

    * Mode: **Unsigned**
    * Folder: `xamu-field`
    * Options (recommended):

        * **Use filename as display name**: true
        * **Unique filename**: false (we supply our own `public_id`)
        * **Overwrite**: true (lets us re‑upload `manifest.json`)
    * Allowed image formats: `jpg,png,heic` (as needed)

2. Ensure the preset works for both:

    * `resource_type=image` (default) — for photos
    * `resource_type=raw` — for `manifest.json` uploads (we’ll set this per upload call)

> You don’t need API key/secret in the app for **unsigned** uploads.

---

## 3) Android Implementation (Compose + Cloudinary + Manifest JSON)

### 3.1 Dependencies

```kotlin
// app/build.gradle
dependencies {
    implementation("com.cloudinary:cloudinary-android:2.7.1")   // uploads
    implementation("io.coil-kt:coil-compose:2.6.0")            // image display
    implementation("com.squareup.okhttp3:okhttp:4.12.0")       // fetch manifest.json
}
```

### 3.2 App Init

```kotlin
// App.kt
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val cfg = hashMapOf(
            "cloud_name" to "dir468aeq",
            "secure" to true
        )
        com.cloudinary.android.MediaManager.init(this, cfg)
    }
}
```

**AndroidManifest.xml**

```xml
<application android:name=".App" ...>
</application>

<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" /> <!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /> <!-- pre‑13 -->
```

### 3.3 Data Models (manifest)

```kotlin
data class ProjectPhotoItem(
    val publicId: String,
    val caption: String?,
    val createdAt: Long
)

data class ProjectManifest(
    val projectId: String,
    val items: MutableList<ProjectPhotoItem> = mutableListOf()
)
```

### 3.4 Cloudinary Helper (upload image + upload manifest)

```kotlin
// CloudinaryHelper.kt
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
    private const val PRESET = "XAMU_UNSIG"
    private const val CLOUD = "dir468aeq"

    private val http by lazy { OkHttpClient() }

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
            .unsigned(PRESET)
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

    /** Upload (overwrite) manifest.json as a RAW resource under the project folder. */
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

        // Upload as RAW + overwrite "manifest"
        MediaManager.get().upload(tmp.absolutePath)
            .unsigned(PRESET)
            .option("folder", folder)
            .option("public_id", "manifest")
            .option("resource_type", "raw")
            .option("overwrite", true)
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
```

### 3.5 Repository (single‑source of truth)

```kotlin
// CloudinaryOnlyRepo.kt
import android.content.Context

class CloudinaryOnlyRepo(private val appContext: Context) {
    fun list(projectId: String): List<ProjectPhotoItem> =
        CloudinaryHelper.fetchManifest(projectId).items.sortedByDescending { it.createdAt }

    fun upload(projectId: String, uri: android.net.Uri, caption: String?,
               onProgress: (Int) -> Unit,
               onDone: () -> Unit,
               onError: (String) -> Unit) {

        CloudinaryHelper.uploadImage(
            projectId = projectId,
            imageUri = uri,
            caption = caption,
            onProgress = onProgress,
            onSuccess = { publicId ->
                // Merge into manifest then upload it
                val manifest = CloudinaryHelper.fetchManifest(projectId)
                manifest.items.add(ProjectPhotoItem(publicId, caption, System.currentTimeMillis()))
                CloudinaryHelper.uploadManifest(
                    context = appContext,
                    projectId = projectId,
                    manifest = manifest,
                    onSuccess = onDone,
                    onError = onError
                )
            },
            onError = onError
        )
    }
}
```

### 3.6 ViewModel

```kotlin
// ProjectPhotosViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectPhotosViewModel(private val repo: CloudinaryOnlyRepo) : ViewModel() {
    private val _photos = MutableStateFlow<List<ProjectPhotoItem>>(emptyList())
    val photos = _photos.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status = _status.asStateFlow()

    fun refresh(projectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _photos.value = repo.list(projectId)
        }
    }

    fun upload(projectId: String, uri: android.net.Uri, caption: String?) {
        _status.value = "Uploading…"
        repo.upload(
            projectId = projectId,
            uri = uri,
            caption = caption,
            onProgress = { p -> _status.value = "Uploading… $p%" },
            onDone = {
                _status.value = "Uploaded"
                refresh(projectId)
            },
            onError = { msg -> _status.value = "Error: $msg" }
        )
    }
}
```

### 3.7 Compose UI (picker → caption → upload → list)

```kotlin
@Composable
fun ProjectImagesScreen(projectId: String, vm: ProjectPhotosViewModel) {
    val photos by vm.photos.collectAsState()
    val status by vm.status.collectAsState()

    var caption by remember { mutableStateOf("") }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> pickedUri = uri }

    LaunchedEffect(projectId) { vm.refresh(projectId) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text("Caption") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { picker.launch("image/*") }) { Text("Pick image") }
            Button(
                onClick = { pickedUri?.let { vm.upload(projectId, it, caption.ifBlank { null }) } },
                enabled = pickedUri != null
            ) { Text("Upload") }
        }
        if (!status.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp)); Text(status!!)
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(photos) { p ->
                val url = CloudinaryHelper.deliveryUrl(p.publicId, 1200)
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(url),
                        contentDescription = p.caption ?: "Project image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (!p.caption.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(p.caption!!, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
```

---

## 4) Acceptance Criteria

* Pick an image → type caption → **Upload**.
* Photo stored at `xamu-field/{projectId}/...`.
* `manifest.json` at `xamu-field/{projectId}/manifest.json` is created/updated on each upload.
* Screen lists images (newest first) from manifest, shows **image + caption**.
* Delivery uses `f_auto,q_auto,w_1200,c_fill,g_auto` for fast/optimized images.
* No API keys/secrets other than **cloud name** and **unsigned preset** in the app.

---

## 5) Test Checklist

1. Create unsigned preset `XAMU_UNSIG` (Unsigned, folder `xamu-field`, Overwrite=true).
2. Run app → pick image → add caption → Upload.
3. Check Cloudinary: image appears under `xamu-field/{projectId}/...`.
4. Open `https://res.cloudinary.com/dir468aeq/raw/upload/xamu-field/{projectId}/manifest.json` — contains the new entry.
5. Reopen screen → list loads from manifest and renders.

---

## 6) Troubleshooting

* **403/401 on upload**: preset name mismatch; ensure `XAMU_UNSIG` exists and is **Unsigned**.
* **Manifest not updating**: ensure preset has **Overwrite=true**; verify we upload with `resource_type=raw` and `public_id=manifest`.
* **Large images slow**: delivery URL already uses `f_auto,q_auto`; tune width or add height.
* **Folder not created**: Cloudinary creates folders on first upload; ensure the `folder` option is set.

---

## 7) Gemini Implementation Prompt

**Role**: Senior Android (Kotlin + Compose) engineer integrating **Cloudinary‑only** uploads.

**Goal**: Implement field‑scientist image upload with caption and listing in the Project Details screen using **Cloudinary unsigned uploads** and a **per‑project manifest.json** (stored as raw resource on Cloudinary). No other backend.

**Tasks**:

1. Add Gradle deps (Cloudinary, Coil, OkHttp). Declare `INTERNET` permission.
2. Initialize `MediaManager` with `cloud_name=dir468aeq` in `App.kt`.
3. Implement `CloudinaryHelper` (upload image, fetch manifest, upload manifest) as provided.
4. Implement `CloudinaryOnlyRepo` and `ProjectPhotosViewModel` glue.
5. Update Project Details Compose screen to pick → caption → upload → list.
6. Use `deliveryUrl(publicId)` for images with `f_auto,q_auto,w_1200,c_fill,g_auto`.

**Acceptance Criteria**: See Section 4.

**Deliverables**: Updated Gradle, `App.kt`, `CloudinaryHelper.kt`, `CloudinaryOnlyRepo.kt`, `ProjectPhotosViewModel.kt`, and modified Project Details screen.

---

**Done.** This is a Cloudinary‑only, no‑backend implementation ready for a Gemini agent to drop into the Android app.
