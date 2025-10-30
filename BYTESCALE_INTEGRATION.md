# Bytescale Photo Upload Integration - Complete

## Summary of Changes

Successfully replaced Cloudinary with Bytescale for field scientist photo uploads. The app now uses Bytescale's public API to upload photos with caption and timestamp metadata.

## What Was Changed

### 1. Created `BytescaleRepo.kt`
- **Location**: `app/src/main/java/com/example/xamu_wil_project/cloudinary/BytescaleRepo.kt`
- **Purpose**: Handles all Bytescale API interactions
- **Features**:
  - Uploads images with caption and `created_at` metadata
  - Lists project photos with metadata retrieval
  - Uses Bytescale account: `W23MTQd`
  - Public API key: `public_W23MTQdAnPhU2FUfKjyryo7t8kRJ-`
  - Folder structure: `/xamu-field/{projectId}/`

### 2. Updated `ProjectPhotosViewModel.kt`
- Changed dependency from `CloudinaryOnlyRepo` to `BytescaleRepo`
- Updated `upload()` method to use suspend functions with coroutines
- Added proper error handling with Result type

### 3. Updated `ProjectImagesScreen.kt`
- Replaced `CloudinaryOnlyRepo` with `BytescaleRepo` in ViewModel factory
- Removed all Cloudinary preset configuration UI
- Changed image display to use direct Bytescale URLs (no transformation needed)
- Simplified UI by removing preset management dialog

## How It Works

### Upload Flow
1. Field scientist selects/captures a photo
2. Enters a caption
3. Taps "Upload"
4. `BytescaleRepo.uploadImage()` uploads to Bytescale with metadata:
   ```json
   {
     "caption": "Field observation description",
     "created_at": "1730160000000"
   }
   ```
5. Photo appears in project gallery with caption and formatted date

### View Flow
1. Field scientist opens "Project Photos"
2. `BytescaleRepo.listProjectPhotos()` fetches photos from `/xamu-field/{projectId}/`
3. For each photo, retrieves metadata (caption, created_at)
4. Displays in grid with:
   - Photo thumbnail (direct Bytescale URL)
   - Caption text
   - Formatted date (e.g., "29 Oct 2025 14:30")

## File Structure

```
/xamu-field/
  ├── {projectId}/
  │   ├── photo_1730160000000.jpg (with metadata)
  │   ├── photo_1730161234567.jpg (with metadata)
  │   └── ...
```

Each photo file stores:
- **caption**: String description entered by scientist
- **created_at**: UNIX timestamp in milliseconds

## API Endpoints Used

1. **Upload**: `POST https://api.bytescale.com/v2/accounts/W23MTQd/uploads/binary`
2. **List**: `GET https://api.bytescale.com/v2/accounts/W23MTQd/folders/list`
3. **Details**: `GET https://api.bytescale.com/v2/accounts/W23MTQd/files/details`

## Benefits Over Cloudinary

✅ **No preset configuration** - Works immediately with public API key
✅ **No unsigned upload restrictions** - Direct metadata support
✅ **Simpler error handling** - No preset validation issues
✅ **Direct URLs** - No transformation pipeline needed
✅ **Built-in metadata** - Caption and timestamp stored with file

## Testing Checklist

- [x] Compile errors resolved
- [ ] Test camera capture → upload
- [ ] Test gallery pick → upload
- [ ] Verify caption displays correctly
- [ ] Verify date formatting (dd MMM yyyy HH:mm)
- [ ] Test multiple projects (different projectIds)
- [ ] Verify photos persist after app restart
- [ ] Test offline behavior (should show error, retry when online)

## Known Limitations

1. **No image transformations** - Images are served as-is (no resize/crop on CDN)
2. **No delete functionality** - Currently view-only after upload
3. **No offline upload queue** - Uploads fail if no network (could add later)
4. **Public API key** - Anyone with key can upload (consider backend proxy for production)

## Next Steps (Optional Enhancements)

1. Add delete photo functionality
2. Implement offline upload queue with WorkManager
3. Add image compression before upload to save bandwidth
4. Add full-screen photo viewer
5. Add photo editing (crop, rotate) before upload
6. Implement server-side upload signing for security

## Migration Notes

- All existing Cloudinary code remains in place but is no longer used
- `CloudinaryHelper.kt` still exists but ProjectImagesScreen no longer references it
- Can safely remove Cloudinary SDK dependencies if no other features use it
- Firebase Realtime DB photos feature (BytescaleRepository.uploadAndPersist) is separate and still available

## Firebase Integration (Alternative)

The app also has `BytescaleRepository.kt` which uploads to Bytescale AND persists metadata to Firebase Realtime DB at:
```
ProjectData/{companyName}/{projectName}/Photos/{photoId}
```

This allows photos to appear in `ProjectDetailsScreen` alongside other field data. Currently, `ProjectImagesScreen` uses the cloud-only approach (no Firebase).

## Troubleshooting

**Error: "Upload failed: 401 Unauthorized"**
- Check API key is correct in `BytescaleRepo.kt`

**Error: "Upload failed: 403 Forbidden"**
- Verify public API key has upload permissions for `/xamu-field/**`

**Photos not appearing after upload**
- Check network logs for successful upload response
- Verify `listProjectPhotos()` returns data
- Check projectId matches between upload and list calls

**No caption or wrong date**
- Verify metadata is included in upload request
- Check `X-Upload-Metadata` header format
- Ensure metadata retrieval in `listProjectPhotos()` works

---

**Status**: ✅ Complete - Ready for testing
**Last Updated**: October 29, 2025

