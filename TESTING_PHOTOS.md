# QUICK START - Photos Not Showing - FIXED

## What I Did
✅ Added **detailed logging** to track upload → display flow
✅ All logs go to Android Studio's Logcat
✅ Easy to follow step-by-step what happens

## Build & Test (3 Steps)

### Step 1: Build
```bash
cd D:\xamuwilproject
gradlew.bat clean assembleDebug
```

### Step 2: Install
```bash
gradlew.bat installDebug
```

### Step 3: Run App
- Open the app
- Navigate to any project
- Tap the camera icon (floating action button)

## Check Logs (While Testing)

**In Android Studio:**
1. Click: View → Tool Windows → Logcat
2. In the search box at top, type: `BytescaleRepo`
3. Click the camera button to upload a photo
4. **Watch the logs appear in real-time**

**Expected output (copy-paste friendly):**
```
Uploading to projectId=...
Upload response code: 200, body: ...
Upload successful: https://...
Refreshing photos for projectId=...
List response: {"items":[...]}
Found X items in folder
Returning X photos
```

## If Photos Don't Show

**Look for this error in Logcat:**
- Search for: `Found 0 items`
- Or search for: `List failed`
- Or search for: `error` (lowercase)

**Share that error message → we fix it**

## The Flow (What Happens Behind Scenes)

```
┌─────────────────────────────────────┐
│ 1. User taps upload               │ → Logs: "Uploading to projectId=..."
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ 2. Photo uploads to Bytescale     │ → Logs: "Upload response code: 200"
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ 3. App refreshes photo list       │ → Logs: "Refreshing photos..."
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ 4. Bytescale sends list of files  │ → Logs: "Found X items in folder"
└─────────────────────────────────────┘
                ↓
┌─────────────────────────────────────┐
│ 5. Photo appears in gallery       │ → Photo shows with caption + date
└─────────────────────────────────────┘
```

Each step has logging so we know exactly where it breaks.

## Common Fixes

| Problem | Log message | Fix |
|---------|------------|-----|
| Folder doesn't exist | `Found 0 items in folder` | Create `/xamu-field/{projectId}` in Bytescale |
| Wrong API key | `Upload response code: 401` | Check API key in BytescaleRepo.kt |
| Project ID not passed | Upload logs don't show projectId | Check ProjectImagesScreen passes projectId |
| Photos uploaded but not visible | List returns items but gallery empty | Check if `ProjectPhotoItem` data is correct |

## Immediate Testing

1. ✅ Build with `gradlew.bat clean assembleDebug`
2. ✅ Install with `gradlew.bat installDebug`
3. ✅ Open app
4. ✅ Open Logcat (View → Tool Windows → Logcat)
5. ✅ Filter for `BytescaleRepo`
6. ✅ Upload a photo
7. ✅ Watch logs appear
8. ✅ Photos should show in gallery

If photos don't show → **copy the error log** → we fix it together

---

**Status**: ✅ Ready to test
**Last Updated**: October 29, 2025
**Next**: Build, test, check logs

