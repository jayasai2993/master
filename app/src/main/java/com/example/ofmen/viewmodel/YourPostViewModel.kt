package com.example.ofmen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ofmen.data.YourPostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Post(
    val id: String,
    val title: String,
    val description: String,
    val mediaUrl: String,
    val mediaType: String,
    val username: String,
    val profileImageUrl: String
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
                        profileImageUrl = doc.getString("profileImageUrl") ?: ""
                    )
                }
                _posts.value = list
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
}
