package com.example.ofmen.screens


import android.view.ViewGroup
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.ofmen.R
import com.example.ofmen.viewmodel.Post
import com.example.ofmen.viewmodel.YourPostsViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoSize
import com.google.firebase.auth.FirebaseAuth

@Composable
fun YourPostsScreen(viewModel: YourPostsViewModel = viewModel(), navController: NavHostController) {
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
                post = post,
                onCommentClick = { navController.navigate("comments/${post.id}") }
            )
        }
    }
}


@Composable
fun YourPostCard(
    post: Post,
    viewModel: YourPostsViewModel = viewModel(),
    onCommentClick: () -> Unit
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

            // Check if media is a video
            val isVideo = post.mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                    post.mediaUrl.endsWith(".mov", ignoreCase = true) ||
                    post.mediaUrl.endsWith(".mkv", ignoreCase = true)

            if (isVideo) {
                val context = LocalContext.current
                var videoAspectRatio by remember { mutableStateOf(16f / 9f) }
                val exoPlayer = remember(post.mediaUrl) {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(post.mediaUrl))
                        prepare()
                        playWhenReady = false
                    }
                }

                var isPlaying by remember { mutableStateOf(false) }
                var userPaused by remember { mutableStateOf(false) } // Track manual pause
                var showControls by remember { mutableStateOf(true) }
                var autoPlay by remember { mutableStateOf(false) } // visibility-based

                // Update aspect ratio dynamically
                DisposableEffect(exoPlayer) {
                    val listener = object : Player.Listener {
                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            if (videoSize.height != 0) {
                                videoAspectRatio = videoSize.width.toFloat() / videoSize.height
                            }
                        }
                    }
                    exoPlayer.addListener(listener)
                    onDispose {
                        exoPlayer.removeListener(listener)
                        exoPlayer.release()
                    }
                }

                // Play video based on manual tap or auto-play
                LaunchedEffect(isPlaying, autoPlay, userPaused) {
                    exoPlayer.playWhenReady = if (userPaused) isPlaying else isPlaying || autoPlay
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(videoAspectRatio)
                        .clip(RoundedCornerShape(8.dp))
                        .onGloballyPositioned { coords ->
                            val windowBounds = coords.boundsInWindow()
                            val parentBounds = coords.parentLayoutCoordinates?.boundsInWindow()
                            if (parentBounds != null) {
                                val visibleHeight = windowBounds.height.coerceAtMost(parentBounds.height)
                                autoPlay = visibleHeight > parentBounds.height / 2
                            }
                        }
                        .clickable {
                            isPlaying = !isPlaying
                            showControls = true
                            userPaused = !isPlaying // mark manual pause
                        }
                ) {
                    // Video Player
                    AndroidView(
                        factory = {
                            PlayerView(it).apply {
                                player = exoPlayer
                                useController = false
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.matchParentSize()
                    )

                    // Centered Play/Pause Button with fade animation
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showControls,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(
                                onClick = {
                                    isPlaying = !isPlaying
                                    showControls = true
                                    userPaused = !isPlaying // manual pause
                                },
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .size(60.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    // Auto-hide controls after 2.5 seconds
                    LaunchedEffect(showControls) {
                        if (showControls) {
                            kotlinx.coroutines.delay(2500)
                            showControls = false
                        }
                    }
                }
            }
            else {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillWidth
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
                        onClick = { onCommentClick() },
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
