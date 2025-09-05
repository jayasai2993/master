package com.example.ofmen.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ofmen.viewmodel.FeedViewModel


@Composable
fun SavedPostsScreen(viewModel: FeedViewModel,navController: NavHostController) {
    val savedPosts by viewModel.savedPosts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSavedPosts()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(savedPosts) { post ->
            PostCard(
                post = post,
                isPlaying = false,
                onVisible = {},
                onLikeClick = { viewModel.toggleLike(post) },
                onCommentClick = { navController.navigate("comments/${post.id}") },
                onSaveClick = { viewModel.toggleSavePost(post) }
            )
        }
    }
}
