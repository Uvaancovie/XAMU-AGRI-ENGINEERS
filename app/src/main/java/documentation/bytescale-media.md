Here’s a clean, **Cloud-only Bytescale** markdown you can drop into your docs so a field scientist can **upload photos** and **view them with a description + date**—no backend needed.

---

# XAMU Field Photos — Bytescale (Upload.io)

**Provider:** Bytescale
**Account ID:** `W23MTQd`
**Public API Key:** `public_W23MTQdAnPhU2FUfKjyryo7t8kRJ-`

## Folder Model

**Root Folder Path:** `/`
**Root Folder Description:** This is your account’s root folder.
**Folder Type:** Built-in Storage
**Permissions:** Public

We’ll keep all field photos under a project-scoped folder:

```
/xamu-field/{projectId}/
```

Each uploaded file will include JSON metadata:

```json
{
  "caption": "Short description typed by the scientist",
  "created_at": "1730160000000"
}
```

`created_at` is a UNIX epoch in milliseconds (when uploaded or when the photo was taken).

---

## Field Scientist Flow (UX)

1. Open **Project Details**.
2. Tap **Pick Image** → select photo.
3. Type a **Caption**.
4. Tap **Upload**.
5. Photo appears in the gallery with **Caption** and **Date** (from `created_at`).

---

## Android Integration (Minimal)

### Gradle

```kotlin
dependencies {
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  implementation("io.coil-kt:coil-compose:2.6.0") // image display
}
```

### Upload (with caption + date)

```kotlin
import android.content.Context
import android.net.Uri
import okhttp3.*
import okio.source
import java.util.*

object XamuBytescale {
  private const val ACCOUNT_ID = "W23MTQd"
  private const val PUBLIC_API_KEY = "public_W23MTQdAnPhU2FUfKjyryo7t8kRJ-"
  private const val BASE = "https://api.bytescale.com/v2/accounts/$ACCOUNT_ID"
  private const val ROOT = "/xamu-field"

  data class UploadResult(val filePath: String, val fileUrl: String)

  fun uploadImage(
    context: Context,
    projectId: String,
    imageUri: Uri,
    caption: String,
    createdAtMillis: Long = System.currentTimeMillis()
  ): UploadResult {
    val cr = context.contentResolver
    val mime = cr.getType(imageUri) ?: "image/jpeg"
    val fileName = "photo_${createdAtMillis}.jpg"
    val folderPath = "$ROOT/$projectId"

    val metadataJson = """{"caption":"${caption.replace("\"","'")}","created_at":"$createdAtMillis"}"""

    val body = cr.openInputStream(imageUri)!!
      .source()
      .readByteString()
      .toRequestBody(mime.toMediaType())

    val req = Request.Builder()
      .url("$BASE/uploads/binary?folderPath=$folderPath&fileName=$fileName&originalFileName=$fileName")
      .header("Authorization", "Bearer $PUBLIC_API_KEY")
      .header("Content-Type", mime)
      .header("X-Upload-Metadata", metadataJson)
      .post(body)
      .build()

    OkHttpClient().newCall(req).execute().use { res ->
      if (!res.isSuccessful) error("Upload failed: ${res.code} ${res.message}")
      val json = res.body!!.string()
      val filePath = Regex("\"filePath\"\\s*:\\s*\"([^\"]+)\"").find(json)!!.groupValues[1]
      val fileUrl  = Regex("\"fileUrl\"\\s*:\\s*\"([^\"]+)\"").find(json)!!.groupValues[1]
      return UploadResult(filePath, fileUrl)
    }
  }
}
```

### List files (read caption + date)

```kotlin
data class ProjectPhoto(
  val fileUrl: String,
  val caption: String?,
  val createdAtMillis: Long
)

fun listProjectPhotos(projectId: String): List<ProjectPhoto> {
  val accountId = "W23MTQd"
  val apiKey = "public_W23MTQdAnPhU2FUfKjyryo7t8kRJ-"
  val base = "https://api.bytescale.com/v2/accounts/$accountId"
  val listUrl = "$base/folders/list?folderPath=/xamu-field/$projectId"

  val client = OkHttpClient()
  val listReq = Request.Builder().url(listUrl).header("Authorization", "Bearer $apiKey").build()

  client.newCall(listReq).execute().use { listRes ->
    if (!listRes.isSuccessful) error("List failed ${listRes.code}")
    val body = listRes.body!!.string()
    val itemRegex = Regex("\\{\"filePath\":\"([^\"]+)\",\"fileUrl\":\"([^\"]+)\"")
    val items = itemRegex.findAll(body).map { it.groupValues[1] to it.groupValues[2] }.toList()

    return items.map { (path, url) ->
      val detailsReq = Request.Builder()
        .url("$base/files/details?filePath=$path")
        .header("Authorization", "Bearer $apiKey")
        .build()

      client.newCall(detailsReq).execute().use { d ->
        val json = d.body!!.string()
        val caption = Regex("\"caption\"\\s*:\\s*\"([^\"]*)\"").find(json)?.groupValues?.getOrNull(1)
        val createdAtStr = Regex("\"created_at\"\\s*:\\s*\"(\\d+)\"").find(json)?.groupValues?.getOrNull(1)
        val createdAt = createdAtStr?.toLongOrNull() ?: 0L
        ProjectPhoto(url, caption, createdAt)
      }
    }.sortedByDescending { it.createdAtMillis }
  }
}
```

### Compose gallery (shows image + caption + date)

```kotlin
@Composable
fun ProjectGallery(photos: List<ProjectPhoto>) {
  LazyColumn {
    items(photos) { p ->
      Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Image(
          painter = rememberAsyncImagePainter(p.fileUrl),
          contentDescription = p.caption ?: "Project photo",
          modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)),
          contentScale = ContentScale.Crop
        )
        val date = remember(p.createdAtMillis) {
          java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(java.util.Date(p.createdAtMillis))
        }
        if (!p.caption.isNullOrBlank()) Text(p.caption!!)
        Text(date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
      }
    }
  }
}
```

---

## Command-line (optional sanity checks)

### Upload (JPEG with caption + date)

```bash
curl -X POST \
  "https://api.bytescale.com/v2/accounts/W23MTQd/uploads/binary?folderPath=/xamu-field/PROJECT_123&fileName=photo_$(date +%s%3N).jpg" \
  -H "Authorization: Bearer public_W23MTQdAnPhU2FUfKjyryo7t8kRJ-" \
  -H "Content-Type: image/jpeg" \
  -H "X-Upload-Metadata: {\"caption\":\"river transect A\",\"created_at\":\"$(date +%s%3N)\"}" \
  --data-binary @./sample.jpg
```

### List a project folder

```bash
curl -H "Authorization: Bearer public_W23MTQdAnPhU2FUfKjyryo7t8kRJ-" \
  "https://api.bytescale.com/v2/accounts/W23MTQd/folders/list?folderPath=/xamu-field/PROJECT_123"
```

### Get file details (read metadata)

```bash
curl -H "Authorization: Bearer public_W23MTQdAnPhU2FUfKjyryo7t8kRJ-" \
  "https://api.bytescale.com/v2/accounts/W23MTQd/files/details?filePath=/xamu-field/PROJECT_123/photo_1730160000000.jpg"
```

---

## Notes / Options

* Your **Public API Key** must have **Uploads + Downloads** permission for `/xamu-field/*/**`.
* If you need thumbnails, append query params (e.g., `?w=800&h=600&fit=cover`) if enabled in your plan; otherwise render as-is.
* If you later want deletes or tighter access control, create a tiny backend to mint short-lived tokens; keep using the same folder scheme.

---

**Done.** This markdown is ready to share with the field team and your Android dev so uploads, captions, and dates all work from the app with Bytescale.
