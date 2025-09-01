package com.example.ofmen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ofmen.R

// Load Bebas Neue font
val bebasNeue = FontFamily(Font(R.font.bebas_neue_regular))

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
fun PostCard(post: Post) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .shadow(8.dp, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // User Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = post.userImage),
                    contentDescription = "User Image",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${post.userName} ${if (post.groupName.isNotEmpty()) "in ${post.groupName}" else ""}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = post.timeAgo,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Image
            Image(
                painter = painterResource(id = post.postImage),
                contentDescription = "Post Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Post Description
            Text(
                text = post.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Likes & Comments
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Like", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("${post.likes} likes", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.width(18.dp))

                Icon(
                    painter = painterResource(R.drawable.comment),
                    contentDescription = "Comment",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("${post.comments} comments",fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
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
                    }
                },
                label = {
                    when (screen) {
                        Screen.Home -> Text(
                            text = "Home",
                            fontFamily = bebasNeue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Screen.Community -> Text(
                            text = "Community",
                            fontFamily = bebasNeue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Screen.Post -> Text(
                            text = "Post",
                            fontFamily = bebasNeue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Screen.Tasks -> Text(
                            text = "Tasks",
                            fontFamily = bebasNeue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Screen.Profile -> Text(
                            text = "Profile",
                            fontFamily = bebasNeue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
