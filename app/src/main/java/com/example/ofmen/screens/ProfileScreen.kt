package com.example.ofmen.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ofmen.DataStoreManager
import com.example.ofmen.R
import com.example.ofmen.viewmodel.ProfileViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(
    dataStoreManager: DataStoreManager,
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.profileState.collectAsState()

    var username by remember { mutableStateOf(profile.username) }
    var bio by remember { mutableStateOf(profile.bio) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // ✅ Load profile once when screen starts
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // ✅ Keep username and bio in sync with latest profile
    LaunchedEffect(profile) {
        username = profile.username
        bio = profile.bio
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page heading
            Text(
                text = "Profile",
                fontSize = 22.sp,
                fontFamily = bebasNeue,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AsyncImage(
                model = when {
                    selectedImageUri != null -> selectedImageUri
                    !profile.profileImageUrl.isNullOrEmpty() -> profile.profileImageUrl
                    else -> R.drawable.user1
                },
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .clickable { imagePicker.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Edit",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Green,
                modifier = Modifier.clickable { imagePicker.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Username field
            var isEditingUserName by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                if (!isEditingUserName) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Username",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = username.ifEmpty { "Not set" },
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(
                            onClick = { isEditingUserName = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Username",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

// Bio field
            var isEditing by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                if (!isEditing) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Bio",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = bio.ifEmpty { "Not set" },
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(
                            onClick = { isEditing = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Bio",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }



            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = {
                    viewModel.updateUserProfile(username, bio, selectedImageUri)
                    selectedImageUri = null
                    navController.navigate("home")
                          },
                modifier = Modifier
                    .height(50.dp)
                    .width(170.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Green
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save", fontFamily = bebasNeue,fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = {
                    Firebase.auth.signOut()
                    CoroutineScope(Dispatchers.IO).launch {
                        dataStoreManager.setLoggedIn(false)
                    }
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .height(50.dp)
                    .width(170.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Logout", color = Color.White,fontFamily = bebasNeue, fontWeight = FontWeight.Bold)
            }
        }
    }
}
