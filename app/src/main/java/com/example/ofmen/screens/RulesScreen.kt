package com.example.ofmen.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val BG = Color(0xFF0B0B0B)
private val PANEL = Color(0xFF0F0F0F)
private val ACCENT = Color(0xFFDC2F2F) // crimson-like
private val MUTED = Color(0xFF9A9A9A)
private val WHITE = Color(0xFFFFFFFF)


@Composable
fun RulesScreen(
    navController: NavController
) {

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(BG),
        color = BG
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🩸 The Brotherhood Oath",
                    color = ACCENT,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"You do not join this Brotherhood. You earn it.\"",
                    color = WHITE,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Rules Card / Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(listOf(PANEL, Color(0xFF0E0E0E))),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(18.dp)
            ) {
                Text(
                    text = "⚔️ The Rules",
                    color = ACCENT,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                RuleLine(" No Identity, No Ego — leave your name & status outside.")
                RuleLine(" Respect the Brotherhood — no judgment, no clout.")
                RuleLine(" Truth Over Image — no filters, no fake reels.")
                RuleLine(" Discipline > Distraction — build, don’t scroll.")
                RuleLine(" Challenge Yourself Daily — growth is forged in fire.")
                RuleLine(" Keep It Brotherhood-Only — this space is for real men.")
            }

            Spacer(modifier = Modifier.height(20.dp))


            // Question 1
            Text(
                text = "1) Honestly — what do you want to be before you die?",
                color = WHITE,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )


            Spacer(modifier = Modifier.height(14.dp))

            // Question 2
            Text(
                text = "2) What are you doing about it now?",
                color = WHITE,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )


            Spacer(modifier = Modifier.height(26.dp))

            // Enter button
            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ACCENT),
                shape = RoundedCornerShape(10.dp),
                enabled = true
            ) {
                Text(
                    text = "Next",
                    color = WHITE,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer small text
            Text(
                text = "By entering, you accept the Brotherhood rules. This space is for discipline, truth, and growth.",
                color = MUTED,
                fontSize = 12.sp,
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
            color = ACCENT,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = WHITE,
            fontSize = 13.sp
        )
    }
}
