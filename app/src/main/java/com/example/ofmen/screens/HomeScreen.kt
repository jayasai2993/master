package com.example.ofmen.screens

import android.util.Log
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.ofmen.R
import com.example.ofmen.viewmodel.FeedPost
import com.example.ofmen.viewmodel.FeedViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoSize
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Load Bebas Neue font
val bebasNeue = FontFamily(Font(R.font.bebas_neue_regular))

@Composable
fun HomeScreen(viewModel: FeedViewModel = viewModel(), navController: NavHostController) {
    val posts by viewModel.feedPosts.collectAsState()
    var currentPlayingPostId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFeed()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        items(posts,key = { it.id }) { post ->
            PostCard(
                post = post,
                isPlaying = currentPlayingPostId == post.id,
                onVisible = { currentPlayingPostId = post.id },
                onLikeClick = { viewModel.toggleLike(post) },
                onCommentClick = { navController.navigate("comments/${post.id}") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "OFMEN",
                style = TextStyle(
                    fontFamily = bebasNeue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
@Composable
fun PostCard(
    post: FeedPost,
    isPlaying: Boolean,
    onVisible: () -> Unit,
    onLikeClick: () -> Unit,
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

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

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
                        IconButton(onClick = onLikeClick, modifier = Modifier.size(28.dp)) {
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
                            onClick = onCommentClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.comment),
                                contentDescription = "Comment",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "${post.commentsCount} comments",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Community,
        Screen.Post,
        Screen.Tasks,
        Screen.Profile
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        var profileImageUrl by remember { mutableStateOf<String?>(null) }

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
        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    when (screen) {
                        Screen.Home -> Icon(Icons.Default.Home, contentDescription = "Home")
                        Screen.Community -> Icon(
                            painter = painterResource(R.drawable.groups_svgrepo_com),
                            contentDescription = "Groups",
                            modifier = Modifier.size(26.dp)
                        )
                        Screen.Post -> Icon(Icons.Default.Add, contentDescription = "Add Post")
                        Screen.Tasks -> Icon(
                            painter = painterResource(R.drawable.task_svgrepo_com),
                            contentDescription = "Tasks",
                            modifier = Modifier.size(26.dp)
                        )
                        Screen.Profile -> Image(
                            painter = rememberAsyncImagePainter(model = profileImageUrl),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                        )
                    }
                },
                label = {
                    Text(
                        text = screen.route,
                        fontFamily = bebasNeue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

fun timeAgo(time: Long): String {
    val now = System.currentTimeMillis()
    if (time > now || time <= 0) return "Just now"

    val diff = now - time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes m ago"
        hours < 24 -> "$hours h ago"
        days < 7 -> "$days d ago"
        days < 30 -> "${days / 7} w ago"
        days < 365 -> "${days / 30} mo ago"
        else -> "${days / 365} y ago"
    }
}
