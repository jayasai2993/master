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
import com.example.ofmen.viewmodel.YourProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun UserProfileScreen(
    userId: String,
    navController: NavHostController,
    viewModel: YourProfileViewModel = viewModel()
) {
    val profile by viewModel.profile.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val postsViewModel: FeedViewModel = viewModel()
    val posts by postsViewModel.posts.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
        postsViewModel.loadPostsForUser(userId)
    }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(10.dp)
        ) {
            // Profile Info
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = if (profile.profileImageUrl.isNotEmpty()) profile.profileImageUrl else R.drawable.user1,
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

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat("Following", profile.following.size.toString())
                    ProfileStat("Followers", profile.followers.size.toString())
                    ProfileStat("Communities", "0")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ---- Follow / Unfollow + Message ----
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isFollowing = profile.followers.contains(currentUserId)

                    Button(
                        onClick = {
                            if (isFollowing) viewModel.unfollowUser(userId)
                            else viewModel.followUser(userId)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isFollowing) "Unfollow" else "Follow")
                    }

                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Message")
                    }
                }
            }
            item{
            Spacer(modifier = Modifier.height(16.dp))}

            item{Divider()}

            items(posts) { post ->
                PostCard(
                    post = post,
                    isPlaying = false,
                    onVisible = {},
                    onLikeClick = { postsViewModel.toggleLike(post) },
                    onCommentClick = { navController.navigate("comments/${post.id}") },
                    onSaveClick = { postsViewModel.toggleSavePost(post) },
                    navController = navController
                )
            }
        }

}

