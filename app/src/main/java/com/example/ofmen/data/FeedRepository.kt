package com.example.ofmen.data



import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FeedRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val postsCollection = db.collection("posts")

    suspend fun getAllPosts() = postsCollection
        .orderBy("likesCount", Query.Direction.DESCENDING)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .get()
        .await()

    suspend fun toggleLike(postId: String, userId: String, liked: Boolean) {
        val postRef = postsCollection.document(postId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likes = snapshot.get("likes") as? MutableList<String> ?: mutableListOf()

            if (liked) likes.add(userId) else likes.remove(userId)

            transaction.update(postRef, mapOf(
                "likes" to likes,
                "likesCount" to likes.size
            ))
        }.await()
    }

    suspend fun addComment(postId: String, userId: String, username: String, text: String) {
        val commentsRef = postsCollection.document(postId).collection("comments")
        val comment = hashMapOf(
            "userId" to userId,
            "username" to username,
            "text" to text,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        commentsRef.add(comment).await()

        // Increment comments count in post
        postsCollection.document(postId).update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1)).await()
    }
    suspend fun toggleSavePost(postId: String, userId: String, saved: Boolean) {
        val userSavedRef = db.collection("users").document(userId).collection("savedPosts").document(postId)

        if (saved) {
            val postData = hashMapOf(
                "postId" to postId,
                "savedAt" to com.google.firebase.Timestamp.now()
            )
            userSavedRef.set(postData).await()
        } else {
            userSavedRef.delete().await()
        }
    }

    suspend fun getSavedPosts(userId: String) =
        db.collection("users").document(userId).collection("savedPosts")
            .orderBy("savedAt", Query.Direction.DESCENDING)
            .get()
            .await()
    suspend fun getPostById(postId: String) =
        postsCollection.document(postId).get().await().takeIf { it.exists() }

}
