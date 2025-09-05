package com.example.ofmen.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ofmen.data.Comment
import com.example.ofmen.data.CommentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CommentViewModel(
    private val repository: CommentRepository = CommentRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    fun loadComments(postId: String) {
        viewModelScope.launch {
            repository.getCommentsRealtime(postId).collectLatest { commentList ->
                _comments.value = commentList
            }
        }
    }

    fun addComment(postId: String, text: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch

            // 🔹 Fetch user document
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            val username = userDoc.getString("username") ?: "Anon"
            val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""

            // 🔹 Call repository with correct values
            repository.addComment(
                postId = postId,
                userId = uid,
                username = username,
                profileImageUrl = profileImageUrl,
                text = text
            )
        }
    }


    fun updateComment(postId: String, commentId: String, newText: String) {
        viewModelScope.launch {
            repository.updateComment(postId, commentId, newText)
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(postId, commentId)
        }
    }
}
