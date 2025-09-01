package com.example.ofmen.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ofmen.viewmodel.YourPostsViewModel

@Composable
fun YourPostsScreen(viewModel: YourPostsViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(posts) { post ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (post.mediaType == "image") {
                        Image(
                            painter = rememberAsyncImagePainter(post.mediaUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🎥 Video: ${post.mediaUrl}")
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(text = "Title: ${post.title}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Description: ${post.description}")


                    Spacer(Modifier.height(8.dp))
                    Row {
                        Button(onClick = {
                            // Example update
                            viewModel.updatePost(post.id, "Updated Title", "Updated Desc")
                        }) {
                            Text("Update")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { viewModel.deletePost(post.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
