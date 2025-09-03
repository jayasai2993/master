package com.example.ofmen.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ofmen.R
import com.example.ofmen.viewmodel.Post
import com.example.ofmen.viewmodel.YourPostsViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun YourPostsScreen(viewModel: YourPostsViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(posts) { post ->
            YourPostCard(
                post = post
            )
        }
    }
}


@Composable
fun YourPostCard(
    post: Post,
    viewModel: YourPostsViewModel = viewModel()
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // User row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = post.profileImageUrl,
                        contentDescription = "Profile image",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.username.firstOrNull()?.toString() ?: "U")
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = timeAgo(post.createdAt),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            // Post Image (show full without cropping)
            if (post.mediaType == "image") {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp) // ensures visible height
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillWidth // show full width, preserve aspect ratio
                )
            }


            // Description
            if (post.description.isNotEmpty()) {
                Text(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Divider()

            // Likes & Comments Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 20.dp)
                ) {
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (post.likes.contains(FirebaseAuth.getInstance().uid))
                                Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.likes.contains(FirebaseAuth.getInstance().uid))
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text("${post.likesCount} likes", style = MaterialTheme.typography.bodySmall)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.comment),
                            contentDescription = "Comment",
                            modifier = Modifier.size(20.dp) // smaller comment icon
                        )
                    }
                    Text("${post.commentsCount} comments", style = MaterialTheme.typography.bodySmall)
                }
            }

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Action Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.updatePost(post.id, "Updated Title", "Updated Desc") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update")
                }
                Button(
                    onClick = { viewModel.deletePost(post.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
