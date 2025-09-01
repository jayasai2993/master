package com.example.ofmen.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ofmen.data.FeedRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FeedPost(
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

class FeedViewModel(
    private val repository: FeedRepository = FeedRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _feedPosts = MutableStateFlow<List<FeedPost>>(emptyList())
    val feedPosts: StateFlow<List<FeedPost>> = _feedPosts

    fun loadFeed() {
        viewModelScope.launch {
            val snapshot = repository.getAllPosts()
            val posts = snapshot.documents.map { doc ->
                FeedPost(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    username = doc.getString("username") ?: "Unknown",
                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                    mediaUrl = doc.getString("mediaUrl") ?: "",
                    mediaType = doc.getString("mediaType") ?: "image",
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                    likes = doc.get("likes") as? List<String> ?: emptyList(),
                    likesCount = doc.getLong("likesCount")?.toInt() ?: 0,
                    commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0
                )
            }
            _feedPosts.value = posts
        }
    }

    fun toggleLike(post: FeedPost) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: return@launch
            val liked = !post.likes.contains(currentUserId)
            repository.toggleLike(post.id, currentUserId, liked)
            loadFeed()
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            val user = auth.currentUser ?: return@launch
            repository.addComment(postId, user.uid, user.displayName ?: "Anon", text)
            loadFeed()
        }
    }
}
