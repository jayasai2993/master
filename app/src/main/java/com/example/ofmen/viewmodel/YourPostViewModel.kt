package com.example.ofmen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ofmen.data.YourPostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val profileImageUrl: String,
    val mediaUrl: String,
    val mediaType: String,
    val title: String,
    val description: String,
    val createdAt: Long,
    val likes: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)

class YourPostsViewModel(
    private val repository: YourPostRepository = YourPostRepository()
) : ViewModel() {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    fun loadPosts() {
        viewModelScope.launch {
            try {
                val snapshot = repository.getUserPosts()
                val list = snapshot.documents.map { doc ->
                    Post(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        mediaUrl = doc.getString("mediaUrl") ?: "",
                        mediaType = doc.getString("mediaType") ?: "image",
                        username = doc.getString("username") ?: "Unknown",
                        profileImageUrl = doc.getString("profileImageUrl") ?: "",
                        createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        likes = doc.get("likes") as? List<String> ?: emptyList(),
                        likesCount = doc.getLong("likesCount")?.toInt() ?: 0,
                        commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                        userId = doc.getString("userId") ?: ""
                    )
                }
                _posts.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    // ✅ New: load posts for any user
    fun loadPostsForUser(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = repository.getPostsByUser(userId)
                _posts.value = snapshot.toPostsList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun updatePost(postId: String, title: String, description: String) {
        viewModelScope.launch {
            repository.updatePost(postId, title, description)
            loadPosts() // refresh
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
            loadPosts() // refresh
        }
    }
    private fun com.google.firebase.firestore.QuerySnapshot.toPostsList(): List<Post> {
        return documents.map { doc ->
            Post(
                id = doc.id,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                mediaUrl = doc.getString("mediaUrl") ?: "",
                mediaType = doc.getString("mediaType") ?: "image",
                username = doc.getString("username") ?: "Unknown",
                profileImageUrl = doc.getString("profileImageUrl") ?: "",
                createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                likes = doc.get("likes") as? List<String> ?: emptyList(),
                likesCount = doc.getLong("likesCount")?.toInt() ?: 0,
                commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                userId = doc.getString("userId") ?: ""
            )
        }
    }
}
