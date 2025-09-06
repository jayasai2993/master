package com.example.ofmen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ofmen.R
import com.example.ofmen.viewmodel.FeedViewModel
import com.example.ofmen.viewmodel.ProfileViewModel
import com.example.ofmen.viewmodel.YourProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun YourProfileScreen(
    navController: NavHostController,
    viewModel: FeedViewModel = viewModel()
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val yourProfileViewModel: YourProfileViewModel = viewModel()
    val yourProfile by yourProfileViewModel.profile.collectAsState()
    val profile by profileViewModel.profileState.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val posts by viewModel.posts.collectAsState()

    LaunchedEffect(userId) {
        profileViewModel.loadUserProfile()
        yourProfileViewModel.loadUserProfile(userId)
        viewModel.loadPostsForUser(userId)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(10.dp)
    ) {
        // ---- Profile Info Section ----
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = profile.profileImageUrl.ifEmpty { R.drawable.user1 },
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(profile.username, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        profile.bio,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---- Stats Row ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat("Following", yourProfile.following.size.toString())
                ProfileStat("Followers", yourProfile.followers.size.toString())
                ProfileStat("Communities", "0")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---- Buttons ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate("editprofile") },
                    modifier = Modifier.weight(1f)
                ) { Text("Edit Profile") }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
        item { Divider() }

        // ---- Posts ----
        items(posts) { post ->
            PostCard(
                post = post,
                isPlaying = false,
                onVisible = {},
                onLikeClick = { viewModel.toggleLike(post) },
                onCommentClick = { navController.navigate("comments/${post.id}") },
                onSaveClick = { viewModel.toggleSavePost(post) },
                navController = navController
            )
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
