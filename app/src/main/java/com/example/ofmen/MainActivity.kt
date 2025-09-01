package com.example.ofmen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.ofmen.screens.MainScreen
import com.example.ofmen.screens.SplashScreen
import com.example.ofmen.ui.theme.OFMENTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            OFMENTheme {
                var currentScreen by remember { mutableStateOf("splash") }

                LaunchedEffect(Unit) {
                    delay(5000)
                    currentScreen = "main"
                }
                when (currentScreen) {
                    "splash" -> SplashScreen()
                    "main" -> MainScreen()
                }
            }
        }
    }
}
