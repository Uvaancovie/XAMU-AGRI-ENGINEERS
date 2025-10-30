package com.example.xamu_wil_project.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase Storage Repository for Xamu Wetlands
 * Uses S3-compatible API with AWS Signature v4
 */
@Singleton
class SupabaseStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "SupabaseStorage"

        // S3-compatible endpoint base for Supabase (use the storage/v1/s3 path)
        private const val S3_BASE = "https://tmbxpdvfqskgwgjhubbi.storage.supabase.co/storage/v1/s3"
        private const val BUCKET_NAME = "xamu-field"
        private const val REGION = "eu-north-1"

        // S3 Credentials (provided)
        private const val ACCESS_KEY_ID = "3420df3077eec47308e8e23e2200ded7"
        private const val SECRET_ACCESS_KEY = "8024309384a3933a8f95f8d3030cb1a15154c9c34b97e966edad8df5a0e71a35"

        private const val SERVICE = "s3"
        private const val ALGORITHM = "AWS4-HMAC-SHA256"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Upload photo to Supabase Storage using S3 PUT (Signature V4)
     */
    suspend fun uploadPhoto(
        uri: Uri,
        companyName: String,
        projectName: String,
        photoId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting S3 v4 upload: $photoId")

            val tempFile = createTempFileFromUri(uri)
            val objectKey = "projects/$companyName/$projectName/photos/$photoId.jpg"
            val url = "$S3_BASE/$BUCKET_NAME/$objectKey"

            val payloadBytes = tempFile.readBytes()
            val payloadHash = hashSHA256Hex(payloadBytes)

            val now = Date()
            val amzDate = amzDateTime(now) // yyyyMMdd'T'HHmmss'Z'
            val dateStamp = dateStamp(now) // yyyyMMdd

            // Canonical request
            val method = "PUT"
            // canonical URI must match the path portion of the URL exactly
            val canonicalUri = "/storage/v1/s3/$BUCKET_NAME/$objectKey"
            val canonicalQueryString = ""
            val host = urlHost()

            val canonicalHeaders = StringBuilder()
                .append("host:").append(host).append('\n')
                .append("x-amz-content-sha256:").append(payloadHash).append('\n')
                .append("x-amz-date:").append(amzDate).append('\n')
                .append("x-amz-acl:public-read").append('\n')
                .toString()

            val signedHeaders = "host;x-amz-content-sha256;x-amz-date;x-amz-acl"

            val canonicalRequest = StringBuilder()
                .append(method).append('\n')
                .append(canonicalUri).append('\n')
                .append(canonicalQueryString).append('\n')
                .append(canonicalHeaders).append('\n')
                .append(signedHeaders).append('\n')
                .append(payloadHash)
                .toString()

            val canonicalRequestHash = hashSHA256Hex(canonicalRequest.toByteArray())

            // String to sign
            val credentialScope = "$dateStamp/$REGION/$SERVICE/aws4_request"
            val stringToSign = StringBuilder()
                .append(ALGORITHM).append('\n')
                .append(amzDate).append('\n')
                .append(credentialScope).append('\n')
                .append(canonicalRequestHash)
                .toString()

            // Signing key
            val signingKey = getSignatureKey(SECRET_ACCESS_KEY, dateStamp, REGION, SERVICE)
            val signature = hmacSHA256Hex(signingKey, stringToSign.toByteArray())

            val authorizationHeader = StringBuilder()
                .append(ALGORITHM).append(' ')
                .append("Credential=").append(ACCESS_KEY_ID).append('/').append(credentialScope).append(',')
                .append("SignedHeaders=").append(signedHeaders).append(',')
                .append("Signature=").append(signature)
                .toString()

            // Build request
            val request = Request.Builder()
                .url(url)
                .addHeader("Host", host)
                .addHeader("x-amz-date", amzDate)
                .addHeader("x-amz-content-sha256", payloadHash)
                .addHeader("x-amz-acl", "public-read")
                .addHeader("Authorization", authorizationHeader)
                .put(tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            Log.d(TAG, "Request URL: $url")
            Log.d(TAG, "Authorization: $authorizationHeader")

            // Execute upload
            val response = client.newCall(request).execute()

            // Clean up temp file
            tempFile.delete()

            return@withContext if (response.isSuccessful) {
                // Construct public URL
                val publicUrl = "$S3_BASE/$BUCKET_NAME/$objectKey"
                Log.d(TAG, "Upload successful: $publicUrl")
                Result.success(publicUrl)
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.e(TAG, "Upload failed: ${response.code}")
                Log.e(TAG, "Error: $errorBody")
                Result.failure(Exception("Upload failed: ${response.code} - $errorBody"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            Result.failure(e)
        }
    }

    /**
     * Delete photo from Supabase Storage
     */
    suspend fun deletePhoto(photoUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Extract key from URL
            val objectKey = photoUrl.substringAfter("/$BUCKET_NAME/")
            val url = "$S3_BASE/$BUCKET_NAME/$objectKey"

            val now = Date()
            val amzDate = amzDateTime(now)
            val dateStamp = dateStamp(now)

            val method = "DELETE"
            val canonicalUri = "/storage/v1/s3/$BUCKET_NAME/$objectKey"
            val canonicalQueryString = ""
            val host = urlHost()

            val payloadHash = hashSHA256Hex(ByteArray(0))

            val canonicalHeaders = StringBuilder()
                .append("host:").append(host).append('\n')
                .append("x-amz-content-sha256:").append(payloadHash).append('\n')
                .append("x-amz-date:").append(amzDate).append('\n')
                .toString()

            val signedHeaders = "host;x-amz-content-sha256;x-amz-date"

            val canonicalRequest = StringBuilder()
                .append(method).append('\n')
                .append(canonicalUri).append('\n')
                .append(canonicalQueryString).append('\n')
                .append(canonicalHeaders).append('\n')
                .append(signedHeaders).append('\n')
                .append(payloadHash)
                .toString()

            val canonicalRequestHash = hashSHA256Hex(canonicalRequest.toByteArray())

            val credentialScope = "$dateStamp/$REGION/$SERVICE/aws4_request"
            val stringToSign = StringBuilder()
                .append(ALGORITHM).append('\n')
                .append(amzDate).append('\n')
                .append(credentialScope).append('\n')
                .append(canonicalRequestHash)
                .toString()

            val signingKey = getSignatureKey(SECRET_ACCESS_KEY, dateStamp, REGION, SERVICE)
            val signature = hmacSHA256Hex(signingKey, stringToSign.toByteArray())

            val authorizationHeader = StringBuilder()
                .append(ALGORITHM).append(' ')
                .append("Credential=").append(ACCESS_KEY_ID).append('/').append(credentialScope).append(',')
                .append("SignedHeaders=").append(signedHeaders).append(',')
                .append("Signature=").append(signature)
                .toString()

            val request = Request.Builder()
                .url(url)
                .addHeader("Host", host)
                .addHeader("x-amz-date", amzDate)
                .addHeader("x-amz-content-sha256", payloadHash)
                .addHeader("Authorization", authorizationHeader)
                .delete()
                .build()

            val response = client.newCall(request).execute()
            return@withContext if (response.isSuccessful) {
                Log.d(TAG, "Delete successful")
                Result.success(Unit)
            } else {
                val body = response.body?.string() ?: ""
                Log.e(TAG, "Delete failed: ${response.code} - $body")
                Result.failure(Exception("Delete failed: ${response.code} - $body"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Delete exception", e)
            Result.failure(e)
        }
    }

    // --- Helpers ---
    private fun urlHost(): String {
        return UriHostHelper.hostFromEndpoint(S3_BASE)
    }

    private fun amzDateTime(date: Date): String {
        val sdf = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    private fun dateStamp(date: Date): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    private fun hashSHA256Hex(data: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(data)
        return bytesToHexLower(digest)
    }

    private fun hmacSHA256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun hmacSHA256Hex(key: ByteArray, data: ByteArray): String {
        return bytesToHexLower(hmacSHA256(key, data))
    }

    private fun getSignatureKey(key: String, dateStamp: String, regionName: String, serviceName: String): ByteArray {
        val kDate = hmacSHA256(("AWS4" + key).toByteArray(), dateStamp.toByteArray())
        val kRegion = hmacSHA256(kDate, regionName.toByteArray())
        val kService = hmacSHA256(kRegion, serviceName.toByteArray())
        return hmacSHA256(kService, "aws4_request".toByteArray())
    }

    private fun bytesToHexLower(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    private fun createTempFileFromUri(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open input stream for URI: $uri")

        val tempFile = File.createTempFile(
            "upload_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )

        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        inputStream.close()

        Log.d(TAG, "Created temp file: ${tempFile.length()} bytes")
        return tempFile
    }
}

// small helper to parse host
private object UriHostHelper {
    fun hostFromEndpoint(endpoint: String): String {
        return endpoint
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
    }
}
