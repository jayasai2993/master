package com.example.ofmen.data


import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import org.json.JSONObject
import java.io.IOException

class PostRepository(
    private val cloudName: String,
    private val unsignedUploadPreset: String
) {
    private val client = OkHttpClient()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Stream the file content from the Uri to OkHttp request body to avoid OOM on big files
    private fun uriRequestBody(context: Context, uri: Uri, mime: String?): RequestBody {
        val contentResolver = context.contentResolver
        val mediaType = mime?.toMediaTypeOrNull()
        return object : RequestBody() {
            override fun contentType() = mediaType
            override fun writeTo(sink: BufferedSink) {
                // open InputStream and write to sink
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val source = inputStream.source()
                    sink.writeAll(source)
                } ?: throw IOException("Unable to open input stream for URI: $uri")
            }
        }
    }

    /**
     * Unsigned upload to Cloudinary.
     * Returns secure_url (String) or null on failure.
     */
    suspend fun unsignedUploadToCloudinary(context: Context, fileUri: Uri): String? =
        withContext(Dispatchers.IO) {
            val url = "https://api.cloudinary.com/v1_1/$cloudName/auto/upload"
            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val requestBody = uriRequestBody(context, fileUri, mimeType)

            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "upload", requestBody)
                .addFormDataPart("upload_preset", unsignedUploadPreset)
                // optionally add folder param if you didn't set folder in preset:
                // .addFormDataPart("folder", "posts")
                .build()

            val request = Request.Builder().url(url).post(multipart).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) throw IOException("Cloudinary upload failed: ${resp.code}")
                val bodyStr = resp.body?.string() ?: return@withContext null
                val json = JSONObject(bodyStr)
                return@withContext json.optString("secure_url", null)
            }
        }

    /**
     * Create post document in Firestore using mediaUrl returned by Cloudinary
     */
    suspend fun createPost(title: String, description: String, mediaUrl: String,username: String,profileImageUrl: String) {
        withContext(Dispatchers.IO) {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
            val data = hashMapOf(
                "userId" to uid,
                "username" to username,
                "profileImageUrl" to profileImageUrl,
                "title" to title,
                "description" to description,
                "mediaUrl" to mediaUrl,
                "likesCount" to 0,
                "commentsCount" to 0,
                "createdAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("posts").add(data).await()
        }
    }

    /**
     * Convenience: upload file to Cloudinary and create Firestore post in one call.
     * Returns the mediaUrl if everything succeeded.
     */
    suspend fun uploadAndCreatePost(context: Context, title: String, description: String, fileUri: Uri,username: String,
                                    profileImageUrl: String): String? {
        val mediaUrl = unsignedUploadToCloudinary(context, fileUri) ?: throw IOException("Upload returned null URL")
        createPost(title, description, mediaUrl,username, profileImageUrl)
        return mediaUrl
    }
}
