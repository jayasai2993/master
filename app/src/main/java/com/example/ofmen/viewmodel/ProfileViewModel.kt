package com.example.ofmen.viewmodel


import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ofmen.data.ProfileRepository
import com.example.ofmen.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _profileState = MutableStateFlow(UserProfile())
    val profileState: StateFlow<UserProfile> = _profileState

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _updateSuccess = MutableStateFlow<Boolean?>(null)
    val updateSuccess: StateFlow<Boolean?> = _updateSuccess

    fun loadUserProfile() {
        viewModelScope.launch {
            _loading.value = true
            val profile = repository.getUserProfile()
            if (profile != null) _profileState.value = profile
            _loading.value = false
        }
    }

    fun updateUserProfile(username: String, bio: String, imageUri: Uri?) {
        viewModelScope.launch {
            _loading.value = true
            val success = repository.updateUserProfile(username, bio, imageUri)
            _updateSuccess.value = success
            // ✅ Refresh profile immediately if update succeeds
            if (success) loadUserProfile()

            _loading.value = false
        }
    }
}
