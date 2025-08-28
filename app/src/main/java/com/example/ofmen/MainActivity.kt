package com.example.ofmen

import android.os.Bundle
import android.view.animation.OvershootInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ofmen.screens.HomeScreen
import com.example.ofmen.ui.theme.OFMENTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OFMENTheme {
                var currentScreen = remember { mutableStateOf("splash") }

                LaunchedEffect(Unit) {
                    delay(4000)
                    currentScreen.value = "home"
                }
                when (currentScreen.value) {
                    "splash" -> SplashScreen()
                    "home" -> HomeScreen()
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(100f) }

    LaunchedEffect(Unit) {
        // Scale with overshoot (bounce effect)
        scale.animateTo(
            targetValue = 1.2f,
            animationSpec = tween(durationMillis = 800, easing = {
                OvershootInterpolator(2f).getInterpolation(it)
            })
        )
        scale.animateTo(1f, tween(300))

        // Fade in
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, delayMillis = 400)
        )

        // Slide-up for tagline
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1000, delayMillis = 600, easing = LinearOutSlowInEasing)
        )
    }

    // Background gradient for a masculine, premium feel
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black,Color(0xFF0F0C29), Color(0xFFf7971e))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Title
            Text(
                text = "OFMEN",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            )

            // Shimmer underline effect (masculine bold detail)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(140.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(Color.White, Color(0xFF9C27B0), Color(0xFF3F51B5))
                        )
                    )
            )

            // Tagline with smooth slide-up
            Text(
                text = "Only For the Real Men",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = Color(0xFFB0BEC5),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .alpha(alpha.value)
                    .offset(y = offsetY.value.dp)
            )
        }
    }
}
