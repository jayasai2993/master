package com.example.ofmen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.ofmen.viewmodel.CommentViewModel
import com.example.ofmen.viewmodel.FeedPost
import com.example.ofmen.viewmodel.FeedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun DetailsScreen(
    post: FeedPost,
    commentViewModel: CommentViewModel = viewModel()
) {
    val comments by commentViewModel.comments.collectAsState()
    val viewModel: FeedViewModel = viewModel()

    LaunchedEffect(post.id) {
        commentViewModel.loadComments(post.id)
    }

    var text by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // Load user profile image once
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val userDoc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()
            profileImageUrl = userDoc.getString("profileImageUrl")
        }
    }

    Scaffold(
        bottomBar = {
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (profileImageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = profileImageUrl),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("U")
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment...") },
                    singleLine = true
                )

                TextButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            commentViewModel.addComment(post.id, text)
                            text = ""
                        }
                    }
                ) {
                    Text("Post")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // respect bottomBar
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 🔹 Show Post first
            item {
                PostCard(
                    post = post,
                    isPlaying = false,
                    onVisible = {},
                    onLikeClick = {viewModel.toggleLike(post) },
                    onCommentClick = {},
                    onSaveClick = {viewModel.toggleSavePost(post)}
                )
                Divider()
            }

            // 🔹 Comments
            items(comments, key = { it.id }) { comment ->
                var isEditing by remember { mutableStateOf(false) }
                var editText by remember { mutableStateOf(comment.text) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (comment.profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = comment.profileImageUrl,
                            contentDescription = "User image",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(comment.username.firstOrNull()?.toString() ?: "U")
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = comment.username,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        if (isEditing) {
                            TextField(
                                value = editText,
                                onValueChange = { editText = it },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (editText.isNotBlank() && editText != comment.text) {
                                            commentViewModel.updateComment(
                                                post.id,
                                                comment.id,
                                                editText
                                            )
                                        }
                                        isEditing = false
                                    }
                                )
                            )
                        } else {
                            Text(
                                text = comment.text,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Text(
                        text = timeAgo(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if (comment.userId == currentUserId) {
                        DropdownMenuDemo(
                            onEdit = {
                                isEditing = true
                                editText = comment.text
                            },
                            onDelete = {
                                commentViewModel.deleteComment(post.id, comment.id)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DropdownMenuDemo(
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    expanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
        }
    }
}
