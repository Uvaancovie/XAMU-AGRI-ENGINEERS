# Supabase Storage Setup Guide for Xamu Wetlands App

## S3 Credentials Configuration

The app is now configured to use Supabase's S3-compatible API:

- **S3 Endpoint**: `https://tmbxpdvfqskgwgjhubbi.storage.supabase.co/storage/v1/s3`
- **Region**: `eu-north-1`
- **Access Key ID**: `3420df3077eec47308e8e23e2200ded7`
- **Secret Access Key**: `8024309384a3933a8f95f8d3030cb1a15154c9c34b97e966edad8df5a0e71a35`
- **Bucket Name**: `xamu-field`

## Quick Setup Steps

### 1. Create Storage Bucket
1. Go to your Supabase Dashboard: https://supabase.com/dashboard
2. Select your project: `tmbxpdvfqskgwgjhubbi`
3. Navigate to **Storage** in the left sidebar
4. Click **"New bucket"**
5. Enter bucket name: `xamu-field`
6. **IMPORTANT**: Check "Public bucket" to make files publicly accessible
7. Click **"Create bucket"**

### 2. Set Storage Policies (If you want more control)

If you unchecked "Public bucket", you need to add these policies:

Go to **Storage > Policies** and add:

#### Policy 1: Allow Public Uploads
```sql
-- Allow anyone to upload files
CREATE POLICY "Allow public uploads"
ON storage.objects
FOR INSERT
TO public
WITH CHECK (bucket_id = 'xamu-field');
```

#### Policy 2: Allow Public Access to Files
```sql
-- Allow anyone to view files
CREATE POLICY "Allow public access"
ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'xamu-field');
```

#### Policy 3: Allow Public Deletion (Optional - only if you want users to delete)
```sql
-- Allow anyone to delete files
CREATE POLICY "Allow public deletion"
ON storage.objects
FOR DELETE
TO public
USING (bucket_id = 'xamu-field');
```

### 3. Get Your Service Role Key (For Better Security - Optional)

If you want to use service role key instead of anon key:

1. Go to **Settings > API** in Supabase Dashboard
2. Find **"service_role" key** (secret - never expose in client code!)
3. Copy the key
4. Update `SupabaseStorageRepository.kt`:

```kotlin
// Replace SUPABASE_ANON_KEY with:
private const val SUPABASE_SERVICE_KEY = "your-service-role-key-here"
```

⚠️ **WARNING**: Service role key bypasses all RLS policies. Only use if you implement server-side validation.

### 4. Verify Setup

After creating the bucket, test upload by:
1. Building and running the app
2. Navigate to a project
3. Try taking a photo or selecting from gallery
4. Check Supabase Storage dashboard to see if file appears

### 5. View Uploaded Files

1. Go to **Storage > xamu-field** in Supabase Dashboard
2. You should see folders: `projects/CompanyName/ProjectName/photos/`
3. Click any image to view or get public URL

## Public URL Format

After successful upload, photos will be accessible at:
```
https://tmbxpdvfqskgwgjhubbi.supabase.co/storage/v1/object/public/xamu-field/projects/{company}/{project}/photos/{photoId}.jpg
```

## Troubleshooting

### Error: "Bucket does not exist"
- Make sure you created bucket named exactly `xamu-field`
- Check bucket is not paused or deleted

### Error: "Unauthorized" or "403"
- Make sure bucket is set to **Public**
- OR add the RLS policies above
- OR use service_role key

### Error: "File size limit"
- Default Supabase limit is 50MB per file
- Adjust in Storage settings if needed

### Error: "Invalid content type"
- App sends `image/jpeg` content type
- Supabase accepts this by default

## Storage Pricing (Supabase Free Tier)
- **Storage**: 1GB included
- **Bandwidth**: 2GB/month included
- Photos are typically 1-5MB each
- ~200-1000 photos before hitting limit

## Recommended: Enable CDN
1. Go to Storage settings
2. Enable CDN for faster global access
3. Images will be cached at edge locations

## Security Best Practices

### For Production:
1. Use authenticated uploads (Firebase Auth + Supabase JWT)
2. Implement file size validation
3. Scan uploads for malicious content
4. Set up file retention policies
5. Enable logging and monitoring

### Current Setup:
- ✅ Public bucket (simple, works immediately)
- ✅ No authentication required
- ⚠️ Anyone with URL can upload (okay for MVP)
- ⚠️ No file size validation (will hit limits)

## Need Help?
- Supabase Docs: https://supabase.com/docs/guides/storage
- Supabase Discord: https://discord.supabase.com

