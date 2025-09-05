package com.example.ofmen.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class Comment(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)


class CommentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val postsCollection = db.collection("posts")

    // 🔹 Real-time listener for comments
    fun getCommentsRealtime(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = postsCollection.document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.map { doc ->
                    Comment(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "Anon",
                        profileImageUrl = doc.getString("profileImageUrl") ?: "",
                        text = doc.getString("text") ?: "",
                        createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    )
                } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    // 🔹 Add comment
    suspend fun addComment(
        postId: String,
        userId: String,
        username: String,
        profileImageUrl: String,
        text: String
    ) {
        val commentsRef = postsCollection.document(postId).collection("comments")
        val comment = hashMapOf(
            "userId" to userId,
            "username" to username,
            "profileImageUrl" to profileImageUrl,
            "text" to text,
            "createdAt" to Timestamp.now()
        )
        commentsRef.add(comment).await()

        // update post's comment count
        postsCollection.document(postId)
            .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1))
            .await()
    }

    // 🔹 Update comment text
    suspend fun updateComment(postId: String, commentId: String, newText: String) {
        val commentRef = postsCollection.document(postId).collection("comments").document(commentId)
        commentRef.update("text", newText).await()
    }

    // 🔹 Delete comment
    suspend fun deleteComment(postId: String, commentId: String) {
        val commentRef = postsCollection.document(postId).collection("comments").document(commentId)
        commentRef.delete().await()

        // decrement post's comment count
        postsCollection.document(postId)
            .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(-1))
            .await()
    }
}
