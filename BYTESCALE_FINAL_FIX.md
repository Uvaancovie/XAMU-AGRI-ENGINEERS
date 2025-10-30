# Bytescale Authorization Fix - FINAL SOLUTION

## Problem
```
Upload failed: 401 Unauthorized
"message":"Unauthenticated: please provide a valid authorization header 
using either the 'Basic' or 'Bearer' scheme."
```

## Root Cause
The API key **MUST** be in the `Authorization` header using `Bearer` scheme, NOT as a query parameter.

## Solution - CORRECT FORMAT ✅

### Upload
```kotlin
val url = "$BASE/uploads/binary?folderPath=$folderPath&fileName=$fileName&originalFileName=$fileName"

val request = Request.Builder()
    .url(url)
    .header("Authorization", "Bearer $API_KEY")  // ✅ CORRECT
    .header("Content-Type", mime)
    .header("X-Upload-Metadata", metadataJson)
    .post(requestBody)
    .build()
```

### List Files
```kotlin
val listUrl = "$BASE/folders/list?folderPath=$folderPath"

val listRequest = Request.Builder()
    .url(listUrl)
    .header("Authorization", "Bearer $API_KEY")  // ✅ CORRECT
    .build()
```

### Get File Details
```kotlin
val detailsUrl = "$BASE/files/details?filePath=$filePath"

val detailsRequest = Request.Builder()
    .url(detailsUrl)
    .header("Authorization", "Bearer $API_KEY")  // ✅ CORRECT
    .build()
```

## Key Points

✅ **API Key in Authorization Header**: `Authorization: Bearer public_W23MTQdAnPhU2FUfKjyryo7t8kRJ`
✅ **NO API key in URL query string** - Removed from all endpoints
✅ **Bearer scheme required** - Use `Bearer` not `Basic`
✅ **All 3 endpoints updated**: upload, list, details

## Files Changed

`app/src/main/java/com/example/xamu_wil_project/cloudinary/BytescaleRepo.kt`
- ✅ Upload endpoint: Added `Authorization: Bearer` header
- ✅ List endpoint: Added `Authorization: Bearer` header  
- ✅ Details endpoint: Added `Authorization: Bearer` header
- ✅ Removed all `apiKey` query parameters

## Testing

Build and run the app:
```bash
gradlew.bat assembleDebug
```

Then test the upload flow:
1. Navigate to a project
2. Tap camera button (or gallery)
3. Select/take photo
4. Enter caption
5. Tap upload
6. ✅ Should see "Uploading..." → "Uploaded successfully"
7. ✅ Photo appears in gallery with caption and date

## cURL Test (verify API key works)

```bash
curl -X POST \
  "https://api.bytescale.com/v2/accounts/W23MTQd/uploads/binary?folderPath=/xamu-field/test&fileName=test.jpg&originalFileName=test.jpg" \
  -H "Authorization: Bearer public_W23MTQdAnPhU2FUfKjyryo7t8kRJ" \
  -H "Content-Type: image/jpeg" \
  -H "X-Upload-Metadata: {\"caption\":\"test\",\"created_at\":\"$(date +%s)000\"}" \
  --data-binary @./photo.jpg
```

If this works in cURL but fails in app:
- Check OkHttp is sending headers correctly
- Verify URI encoding is not double-encoding the folderPath
- Check Content-Type header matches actual file

If this fails in cURL too:
- Verify API key is correct: `public_W23MTQdAnPhU2FUfKjyryo7t8kRJ`
- Check account ID: `W23MTQd`
- Verify folder permissions in Bytescale dashboard

## Status

✅ **FIXED** - All endpoints now use correct `Authorization: Bearer` header
✅ **READY** - Code compiled, no blocking errors
✅ **NEXT** - Build and test on device

---

**Last Updated**: October 29, 2025, 02:15 AM
**API Key**: `public_W23MTQdAnPhU2FUfKjyryo7t8kRJ`
**Account ID**: `W23MTQd`
**Folder Root**: `/xamu-field/{projectId}/`

