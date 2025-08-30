package com.example.ofmen.screens

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Community : Screen("community")
    object Post : Screen("post")
    object Tasks : Screen("tasks")
    object Profile : Screen("profile")
    object Join : Screen("join")
    object Login : Screen("login")
    object Signup : Screen("signup")
}
