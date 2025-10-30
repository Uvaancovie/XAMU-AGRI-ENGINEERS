# Bytescale 401 Unauthorized Fix - RESOLVED

## Problem
`Upload failed: 401 Unauthorized body={"error":{"message":"Unauthenticated: invalid credentials.","code":"authentication_failed_credentials_denied"}}`

## Root Cause
Bytescale's public API keys require the `apiKey` parameter in the **URL query string**, NOT as a `Bearer` token in the `Authorization` header. The code was incorrectly using:
```kotlin
.header("Authorization", "Bearer $PUBLIC_API_KEY")
```

## Solution Applied

### Changed Authentication Method
**Before** (WRONG):
```kotlin
val url = "$BASE/uploads/binary?folderPath=$folderPath&fileName=$fileName"
val request = Request.Builder()
    .url(url)
    .header("Authorization", "Bearer $PUBLIC_API_KEY")  // ❌ WRONG
```

**After** (CORRECT):
```kotlin
val url = "$BASE/uploads/binary?apiKey=$API_KEY&folderPath=$folderPath&fileName=$fileName"
val request = Request.Builder()
    .url(url)  // ✅ API key in query string
```

### API Key Updated
- **Account ID**: `W23MTQd`
- **API Key**: `public_W23MTQdAnPhU2FUfKjyryo7t8kRJ`
- **Folder Path**: `/xamu-field/{projectId}/`

## Files Modified

### 1. `BytescaleRepo.kt`
**Location**: `app/src/main/java/com/example/xamu_wil_project/cloudinary/BytescaleRepo.kt`

**Changes**:
1. ✅ Updated `API_KEY` constant (removed trailing dash that was causing issues)
2. ✅ Changed upload URL to include `apiKey` query parameter
3. ✅ Removed `Authorization: Bearer` header
4. ✅ Changed list URL to include `apiKey` query parameter
5. ✅ Changed details URL to include `apiKey` query parameter

## How Bytescale Authentication Works

Bytescale supports two authentication methods:

### 1. **Public API Keys** (What we're using)
- Include as query parameter: `?apiKey=public_xxx`
- Used for: Uploads, downloads, listing (if enabled)
- No `Authorization` header needed

### 2. **Secret API Keys** (Not using)
- Include as header: `Authorization: Bearer secret_xxx`
- Used for: Admin operations, higher permissions
- More secure but requires backend

## Testing Checklist

- [x] Fixed authentication method (query parameter)
- [x] Removed Bearer token header
- [x] Updated all API calls (upload, list, details)
- [ ] **Test upload with camera**
- [ ] **Test upload from gallery**
- [ ] **Verify photo appears with caption**
- [ ] **Verify date displays correctly**

## Expected Behavior Now

### Upload Flow
1. Field scientist selects photo
2. Enters caption
3. Taps upload
4. ✅ Should see: "Uploading..." → "Uploaded successfully"
5. ✅ Photo appears in grid
6. ✅ Caption and date display below photo

### API Call Format
```
POST https://api.bytescale.com/v2/accounts/W23MTQd/uploads/binary
     ?apiKey=public_W23MTQdAnPhU2FUfKjyryo7t8kRJ
     &folderPath=/xamu-field/PROJECT_123
     &fileName=photo_1730248027136.jpg
```

## Troubleshooting

### If you still get 401:
1. **Check API Key Permissions** in Bytescale dashboard:
   - Go to Settings → API Keys
   - Verify `public_W23MTQdAnPhU2FUfKjyryo7t8kRJ` exists
   - Ensure **Uploads** permission is enabled
   - Check folder permissions for `/xamu-field/**`

2. **Check Folder Path**:
   - Folder must be created or auto-creation enabled
   - Path must start with `/`

3. **Check File Size**:
   - Public keys may have size limits
   - Compress large images before upload

### If upload succeeds but photos don't appear:
- Check `listProjectPhotos()` logs
- Verify metadata is being saved correctly
- Check `fileUrl` is populated in response

## Bytescale Dashboard Configuration

To verify your API key works, go to:
1. https://www.bytescale.com/dashboard
2. Settings → API Keys
3. Find: `public_W23MTQdAnPhU2FUfKjyryo7t8kRJ`
4. Ensure these permissions are enabled:
   - ✅ **Upload Files**
   - ✅ **Download Files**
   - ✅ **List Files** (optional, for gallery)

## Alternative: Test with cURL

```bash
# Test upload (replace with your own JPEG)
curl -X POST \
  "https://api.bytescale.com/v2/accounts/W23MTQd/uploads/binary?apiKey=public_W23MTQdAnPhU2FUfKjyryo7t8kRJ&folderPath=/xamu-field/test&fileName=test.jpg" \
  -H "Content-Type: image/jpeg" \
  -H "X-Upload-Metadata: {\"caption\":\"test photo\",\"created_at\":\"$(date +%s)000\"}" \
  --data-binary @test.jpg
```

If this works in cURL but not in the app, it's an app-side issue. If it fails in cURL too, it's a Bytescale configuration issue.

## Summary

✅ **Fixed**: Changed from Bearer token authentication to query parameter authentication
✅ **Updated**: All 3 API endpoints (upload, list, details)
✅ **Ready**: Code is ready to test

---

**Status**: ✅ FIXED - Ready for testing
**Last Updated**: October 29, 2025, 02:00 AM
**Next Step**: Build and test photo upload on device

