package com.example.ofmen.viewmodel



import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ofmen.data.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostViewModel(private val repo: PostRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _status = MutableStateFlow<String?>(null)  // "success" or error message
    val status: StateFlow<String?> = _status

    fun uploadPost(context: Context, title: String, description: String, fileUri: Uri) {
        viewModelScope.launch {
            _loading.value = true
            _status.value = null
            try {
                // Fetch user details here
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val userDoc = FirebaseFirestore.getInstance().collection("users")
                    .document(uid ?: "").get().await()
                val username = userDoc.getString("username") ?: "Unknown"
                val profileImageUrl = userDoc.getString("profileImageUrl") ?: ""
                Log.d("PostDebug", "Fetched user -> username: $username, profileImageUrl: $profileImageUrl")
                repo.uploadAndCreatePost(context, title, description, fileUri,username,
                    profileImageUrl)
                _status.value = "success"
            } catch (e: Exception) {
                _status.value = e.message ?: "Upload failed"
            } finally {
                _loading.value = false
            }
        }
    }
}
