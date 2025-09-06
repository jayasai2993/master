package com.example.ofmen.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await

class YourPostRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val postsCollection = db.collection("posts")

    suspend fun getUserPosts(): QuerySnapshot {
        val userId = auth.currentUser?.uid ?: throw Exception("Not logged in")
        return postsCollection.whereEqualTo("userId", userId).get().await()
    }

    suspend fun updatePost(postId: String, title: String, description: String) {
        postsCollection.document(postId).update(
            mapOf(
                "title" to title,
                "description" to description
            )
        ).await()
    }

    suspend fun deletePost(postId: String) {
        postsCollection.document(postId).delete().await()
    }
    // ✅ New: Get posts for any user
    suspend fun getPostsByUser(userId: String): QuerySnapshot {
        return postsCollection.whereEqualTo("userId", userId).get().await()
    }
}
