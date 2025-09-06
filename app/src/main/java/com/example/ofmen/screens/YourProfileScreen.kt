package com.example.ofmen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.ofmen.viewmodel.ProfileViewModel
import com.example.ofmen.viewmodel.YourPostsViewModel
import com.example.ofmen.viewmodel.YourProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun YourProfileScreen(
    navController: NavHostController,
    viewModel: YourPostsViewModel = viewModel()
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val yourProfileViewModel: YourProfileViewModel = viewModel()
    val yourProfile by yourProfileViewModel.profile.collectAsState()
    val profile by profileViewModel.profileState.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    LaunchedEffect(userId) {
        profileViewModel.loadUserProfile()
        yourProfileViewModel.loadUserProfile(userId)
        viewModel.loadPosts()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // ---- Profile Info Section ----
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
                    Text(profile.bio, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            // ---- Buttons (for your own profile: Edit + Logout) ----
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

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            YourPostsScreen(viewModel, navController)
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
