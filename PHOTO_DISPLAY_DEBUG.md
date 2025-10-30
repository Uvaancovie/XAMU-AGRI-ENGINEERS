# Photos Not Showing - Debugging Guide

## Problem
Upload shows "Uploaded successfully" but photos don't appear in the gallery.

## Solution
I've added **comprehensive logging** to track the entire upload → refresh → display flow.

## What Changed

### 1. **BytescaleRepo.kt** - Enhanced Logging
Added detailed logs at every step:
- ✅ Upload request details (projectId, folderPath, fileName, metadata)
- ✅ Upload response code and body
- ✅ List request details
- ✅ List response parsing (items count, available keys)
- ✅ Metadata retrieval from each file

### 2. **ProjectPhotosViewModel.kt** - Lifecycle Logging
Added logs to track:
- ✅ When `refresh()` is called
- ✅ How many photos are returned from repo
- ✅ Upload success/failure
- ✅ Refresh after upload

## Testing Steps

### Step 1: Build & Install
```bash
cd D:\xamuwilproject
gradlew.bat clean assembleDebug
gradlew.bat installDebug
```

### Step 2: Open Logcat
In Android Studio:
- View → Tool Windows → Logcat
- Filter: `BytescaleRepo` or `ProjectPhotosVM`

### Step 3: Reproduce the Issue
1. Open project
2. Tap "Take Photo" or "Pick from Gallery"
3. Enter caption
4. Tap upload
5. **Check logs immediately** (follow Step 4 below)

### Step 4: Check Logs - What to Look For

**Expected Flow:**

```
BytescaleRepo: Uploading to projectId=PROJECT_123, folderPath=/xamu-field/PROJECT_123, fileName=photo_1730248000000.jpg
BytescaleRepo: Metadata: {"caption":"test photo","created_at":"1730248000000"}
BytescaleRepo: Upload response code: 200, body: {"filePath":"/xamu-field/PROJECT_123/photo_1730248000000.jpg","fileUrl":"https://..."}
BytescaleRepo: Upload successful: https://...

ProjectPhotosVM: Refreshing photos for projectId=PROJECT_123
BytescaleRepo: Listing photos for projectId=PROJECT_123, folderPath=/xamu-field/PROJECT_123
BytescaleRepo: List response: {"items":[{"filePath":"/xamu-field/PROJECT_123/photo_1730248000000.jpg","fileUrl":"https://..."}]}
BytescaleRepo: Found 1 items in folder
BytescaleRepo: Item 0: filePath=/xamu-field/PROJECT_123/photo_1730248000000.jpg, fileUrl=https://...
BytescaleRepo: Details for /xamu-field/PROJECT_123/photo_1730248000000.jpg: {"metadata":{"caption":"test photo","created_at":"1730248000000"}}
BytescaleRepo: Parsed: caption=test photo, createdAt=1730248000000
BytescaleRepo: Returning 1 photos
ProjectPhotosVM: Got 1 photos from repo
```

## Possible Issues & Solutions

### Issue 1: Upload succeeds but photos list is empty
**Log**: `Found 0 items in folder`

**Cause**: Folder doesn't exist or files aren't being saved there

**Fix**:
1. Check Bytescale dashboard: does `/xamu-field/PROJECT_123/` folder exist?
2. If not, enable auto-folder creation in Bytescale settings
3. Or manually create the folder structure

### Issue 2: Upload response code is NOT 200
**Log**: `Upload response code: 401` or `400` or `500`

**Solution**: Look at the response body in logs:
- **401**: Authorization failed - check API key
- **400**: Bad request - check folderPath format
- **500**: Server error - contact Bytescale support

### Issue 3: Items array is missing
**Log**: `No items array in response. Available keys: [...]`

**Cause**: Bytescale API changed or response format is different

**Fix**: Check log for available keys and update parsing

### Issue 4: Metadata not found
**Log**: `Details for .../photo.jpg: {"metadata":null}`

**Cause**: Metadata wasn't saved during upload

**Fix**:
1. Verify `X-Upload-Metadata` header is being sent
2. Check Bytescale API documentation for metadata format

### Issue 5: Photo shows but no caption/date
**Log**: `Parsed: caption=null, createdAt=0`

**Cause**: Metadata parsing failed

**Fix**: Check the metadata JSON format in upload request

## Quick Verification Test

Open device terminal and run:
```bash
adb logcat | grep -E "BytescaleRepo|ProjectPhotosVM"
```

Then repeat upload steps 2-5 above. All logs should flow in order.

## If Still Not Working

1. **Share full logcat output** from the time you tap upload until you see the error
2. Check:
   - API key is correct: `public_W23MTQdAnPhU2FUfKjyryo7t8kRJ`
   - Account ID is correct: `W23MTQd`
   - ProjectId is not empty (check it's being passed correctly)
   - Folder path format is exactly: `/xamu-field/{projectId}`

## Next Steps

1. ✅ Build with logging
2. ✅ Reproduce the issue
3. ✅ Check logcat for the expected flow
4. ✅ Identify which step fails
5. ✅ Report the failing log line

Once we see the logs, we can identify exactly where the problem is.

---

**Status**: Ready to debug
**Files Modified**: 
- `BytescaleRepo.kt` - Added comprehensive logging
- `ProjectPhotosViewModel.kt` - Added lifecycle logging
**Next**: Build, install, and check logcat during upload/refresh


