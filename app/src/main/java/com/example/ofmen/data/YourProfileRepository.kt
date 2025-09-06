package com.example.ofmen.data


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class YourProfile(
    val username: String = "",
    val bio: String = "",
    val profileImageUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList()
)

class YourProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val currentUserId = auth.currentUser?.uid

    fun getUserProfile(userId: String = currentUserId ?: "") = callbackFlow {
        val docRef = firestore.collection("users").document(userId)
        val listener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) {
                trySend(YourProfile()) // fallback
                return@addSnapshotListener
            }
            val profile = snapshot.toObject(YourProfile::class.java) ?: YourProfile()
            trySend(profile)
        }
        awaitClose { listener.remove() }
    }

    suspend fun followUser(targetUserId: String) {
        val myId = currentUserId ?: return

        val myRef = firestore.collection("users").document(myId)
        val targetRef = firestore.collection("users").document(targetUserId)

        firestore.runBatch { batch ->
            // Add me to target's followers
            batch.update(targetRef, "followers", com.google.firebase.firestore.FieldValue.arrayUnion(myId))
            // Add target to my following
            batch.update(myRef, "following", com.google.firebase.firestore.FieldValue.arrayUnion(targetUserId))
        }.await()
    }

    suspend fun unfollowUser(targetUserId: String) {
        val myId = currentUserId ?: return

        val myRef = firestore.collection("users").document(myId)
        val targetRef = firestore.collection("users").document(targetUserId)

        firestore.runBatch { batch ->
            // Remove me from target's followers
            batch.update(targetRef, "followers", com.google.firebase.firestore.FieldValue.arrayRemove(myId))
            // Remove target from my following
            batch.update(myRef, "following", com.google.firebase.firestore.FieldValue.arrayRemove(targetUserId))
        }.await()
    }
}
