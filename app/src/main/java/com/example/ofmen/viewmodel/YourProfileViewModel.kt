package com.example.ofmen.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ofmen.data.YourProfile
import com.example.ofmen.data.YourProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class YourProfileViewModel(
    private val repository: YourProfileRepository = YourProfileRepository()
) : ViewModel() {

    private val _profile = MutableStateFlow(YourProfile())
    val profile: StateFlow<YourProfile> = _profile

    fun loadUserProfile(userId: String = "") {
        viewModelScope.launch {
            repository.getUserProfile(userId).collectLatest {
                _profile.value = it
            }
        }
    }

    fun followUser(targetUserId: String) {
        viewModelScope.launch {
            repository.followUser(targetUserId)
        }
    }

    fun unfollowUser(targetUserId: String) {
        viewModelScope.launch {
            repository.unfollowUser(targetUserId)
        }
    }
}
