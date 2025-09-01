package com.example.ofmen.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.SetOptions

data class UserProfile(
    val username: String = "",
    val bio: String = "",
    val profileImageUrl: String = ""
)

class ProfileRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val userId: String get() = auth.currentUser?.uid ?: ""

    suspend fun getUserProfile(): UserProfile? {
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            snapshot.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(username: String, bio: String, imageUri: Uri?): Boolean {
        return try {
            var profileImageUrl: String? = null

            // If new image uploaded → save to storage & get download URL
            if (imageUri != null) {
                val ref = storage.reference.child("profile_images/$userId.jpg")
                ref.putFile(imageUri).await()
                profileImageUrl = ref.downloadUrl.await().toString()
            }

            // Build update map
            val updates = hashMapOf<String, Any>(
                "username" to username,
                "bio" to bio
            )

            if (profileImageUrl != null) {
                updates["profileImageUrl"] = profileImageUrl
            }

            // ✅ Use merge to avoid overwriting old fields
            firestore.collection("users")
                .document(userId)
                .set(updates, SetOptions.merge())
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
}
