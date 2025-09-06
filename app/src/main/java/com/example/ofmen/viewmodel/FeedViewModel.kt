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
    val commentsCount: Int = 0,
    val isSaved: Boolean = false
)

class FeedViewModel(
    private val repository: FeedRepository = FeedRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _feedPosts = MutableStateFlow<List<FeedPost>>(emptyList())
    val feedPosts: StateFlow<List<FeedPost>> = _feedPosts

    private val _savedPosts = MutableStateFlow<List<FeedPost>>(emptyList())
    val savedPosts: StateFlow<List<FeedPost>> = _savedPosts

    // ✅ Use only FeedPost for posts, remove Post type
    private val _posts = MutableStateFlow<List<FeedPost>>(emptyList())
    val posts: StateFlow<List<FeedPost>> = _posts

    fun toggleSavePost(post: FeedPost) {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: return@launch
            repository.toggleSavePost(post.id, currentUserId, !post.isSaved)
            loadFeed()
            loadSavedPosts()
        }
    }

    fun loadSavedPosts() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: return@launch
            val snapshot = repository.getSavedPosts(currentUserId)

            val savedPostsList = snapshot.documents.mapNotNull { savedDoc ->
                val postId = savedDoc.getString("postId") ?: return@mapNotNull null
                val postSnapshot = repository.getPostById(postId)
                postSnapshot?.let { doc ->
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
                        commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                        isSaved = true
                    )
                }
            }
            _savedPosts.value = savedPostsList
        }
    }

    fun loadPostsForUser(userId: String) {
        viewModelScope.launch {
            try {
                val snapshot = repository.getPostsByUser(userId)
                _posts.value = snapshot.toFeedPostList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid ?: return@launch
            val snapshot = repository.getAllPosts()
            val savedSnapshot = repository.getSavedPosts(currentUserId)
            val savedIds = savedSnapshot.documents.mapNotNull { it.getString("postId") }.toSet()

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
                    commentsCount = doc.getLong("commentsCount")?.toInt() ?: 0,
                    isSaved = savedIds.contains(doc.id)
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

    // Extension function to convert QuerySnapshot to List<FeedPost>
    private fun com.google.firebase.firestore.QuerySnapshot.toFeedPostList(): List<FeedPost> {
        return documents.map { doc ->
            FeedPost(
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
                userId = doc.getString("userId") ?: "",
                isSaved = false
            )
        }
    }
}
