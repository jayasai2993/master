package com.example.ofmen.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ofmen.DataStoreManager
import com.example.ofmen.viewmodel.FeedViewModel
import com.example.ofmen.viewmodel.ProfileViewModel
import com.example.ofmen.viewmodel.YourPostsViewModel


@Composable
fun MainScreen(){
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val context = LocalContext.current
        val dataStoreManager = DataStoreManager(context)
        val isLoggedIn by dataStoreManager.isLoggedInFlow.collectAsState(initial = false)
        val currentRoute = navBackStackEntry?.destination?.route

        Scaffold(
            topBar = {
                if (currentRoute == "home") {
                    HomeTopBar()
                }
            },
            bottomBar = {
                if (currentRoute in listOf("home", "community", "post", "tasks", "profile")) {
                    BottomNavBar(navController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) "home" else "rules",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { val feedViewModel: FeedViewModel = viewModel()
                    HomeScreen(viewModel = feedViewModel) }
                composable("community") { CommunityScreen(navController) }
                composable("post") { NewPostScreen(cloudName = "dvyfzlzzq", uploadPreset = "unsigned_posts_preset",navController) }
                composable("tasks") { TaskScreen() }
                composable("profile") { val profileViewModel: ProfileViewModel = viewModel()
                    ProfileScreen( dataStoreManager, navController, viewModel = profileViewModel) }
                composable("login") { LoginScreen(navController, dataStoreManager) }
                composable("signup") { SignupScreen(navController) }
                composable("rules") { RulesScreen(navController) }
                composable("YourPosts") { val yourPostsViewModel: YourPostsViewModel = viewModel()
                    YourPostsScreen(viewModel = yourPostsViewModel) }
            }
        }
}