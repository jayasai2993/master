package com.example.ofmen.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ofmen.R

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
            animationSpec = tween(
                durationMillis = 1000,
                delayMillis = 600,
                easing = LinearOutSlowInEasing
            )
        )
    }

    // Background using theme colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.user1),
                contentDescription = "Logo",
                modifier = Modifier
                    .width(150.dp)
                    .height(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "OFMEN",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(140.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.onBackground,
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )

            Text(
                text = "Only For the Real Men",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .alpha(alpha.value)
                    .offset(y = offsetY.value.dp)
            )
        }
    }
}
