package com.example.ofmen.screens

import androidx.compose.ui.graphics.Color
import com.example.ofmen.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState


// Load your Bebas Neue font
val bebasNeue = FontFamily(Font(R.font.bebas_neue_regular))

// Your brand colors (replace with your palette)
val PrimaryColor = Color(0xFF1DA1F2)   // Example blue
val TextColor = Color.Black
val SubTextColor = Color.Gray

@Composable
fun HomeScreen() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(dummyPosts) { post ->
                PostCard(post)
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
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextColor)
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextColor)
            }
        }
    )
}

@Composable
fun PostCard(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // User Row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = post.userImage),
                contentDescription = "User Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("${post.userName} in ${post.groupName}", fontSize = 14.sp, fontFamily = bebasNeue)
                Text(post.timeAgo, fontSize = 12.sp, color = SubTextColor)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.MoreVert, contentDescription = "More")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Post Image
        Image(
            painter = painterResource(id = post.postImage),
            contentDescription = "Post Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Post Description
        Text(post.description, fontSize = 14.sp, color = TextColor)

        Spacer(modifier = Modifier.height(8.dp))

        // Likes & Comments
        Row {
            Icon(Icons.Default.FavoriteBorder, contentDescription = "Like")
            Spacer(modifier = Modifier.width(4.dp))
            Text("${post.likes} likes", fontSize = 13.sp, color = SubTextColor)

            Spacer(modifier = Modifier.width(16.dp))

            Icon(painter = painterResource(R.drawable.comment), contentDescription = "Comment", modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("${post.comments} comments", fontSize = 13.sp, color = SubTextColor)
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

    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route

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
                            painter = painterResource(R.drawable.user1),
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                        )
                        else -> {}
                    }
                }
            )
        }
    }
}


// Dummy data model
data class Post(
    val userName: String,
    val groupName: String,
    val timeAgo: String,
    val userImage: Int,
    val postImage: Int,
    val description: String,
    val likes: Int,
    val comments: Int
)

// Dummy post list
val dummyPosts = listOf(
    Post("Tyler Durden", "", "3 min ago", R.drawable.user1, R.drawable.post1, "We are Consumers", 21, 4),
    Post("Daniel", "", "2 hrs ago", R.drawable.user2, R.drawable.post2, "The First Rule is :- ", 6, 18),
    Post("Fight Club", "", "3 min ago", R.drawable.user3, R.drawable.post3, "Gentlemen Welcome to Fight Club", 21, 4),
    Post("Marla", "", "2 hrs ago", R.drawable.user1, R.drawable.post4, "What are you Thinking about Life?", 6, 18),
    Post("Be a Man", "", "3 min ago", R.drawable.user2, R.drawable.post5, "You have to Fight", 21, 4),
    Post("Rocky", "", "2 hrs ago", R.drawable.user3, R.drawable.post6, "What the Hell is going on here!", 6, 18)
)
