package com.example.ofmen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun RulesScreen(
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "OFMEN",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 35.sp,
                    fontFamily = bebasNeue,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.height(11.dp))
                Text(
                    text = "\"Only For The Real Men\"",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    fontSize = 22.sp,
                    fontFamily = bebasNeue,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Rules Panel
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.background
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "The Rules of OFMEN",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                RuleLine(" No Identity, No Ego — leave your name & status outside.")
                RuleLine(" Respect the Brotherhood — no judgment, no clout.")
                RuleLine(" Truth Over Image — no filters, no fake reels.")
                RuleLine(" Discipline > Distraction — build, don’t scroll.")
                RuleLine(" Challenge Yourself Daily — growth is forged in fire.")
                RuleLine(" Keep It Brotherhood-Only — this space is for real men.")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Questions
            QuestionItem("1) what do you want to be before you die?")
            Spacer(modifier = Modifier.height(18.dp))
            QuestionItem("2) What are you doing about it now?")

            Spacer(modifier = Modifier.height(30.dp))

            // Enter button
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .width(180.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Next",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontFamily = bebasNeue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Text(
                text = "By entering, you accept the Brotherhood rules.\nThis space is for discipline, truth, and growth.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun RuleLine(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "\u2022",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 17.sp
        )
    }
}

@Composable
private fun QuestionItem(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold
    )
}
