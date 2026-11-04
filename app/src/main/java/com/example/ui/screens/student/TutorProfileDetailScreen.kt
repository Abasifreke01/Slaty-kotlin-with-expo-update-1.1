package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tutor
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileDetailScreen(
    tutor: Tutor,
    onBack: () -> Unit,
    onReachTutor: (Tutor) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutor Profile", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlatyDarkBackground)
            )
        },
        containerColor = SlatyDarkBackground,
        bottomBar = {
            Surface(
                color = Color(0xFF151320),
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Session Rate", color = SlatyTextSecondary, fontSize = 12.sp)
                        Text(
                            text = "₦${tutor.hourlyRate.toInt()} / hr",
                            color = SlatyMint,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onReachTutor(tutor) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("reach_tutor_action_button")
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Book Session", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF231E35)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tutor.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    color = SlatyMint,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = tutor.name,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (tutor.verified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified Tutor",
                        tint = SlatyMint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tutor.specialization,
                color = SlatyTextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1F1C2F))
                    .padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD166), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${tutor.rating}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text("Rating", color = SlatyTextSecondary, fontSize = 12.sp)
                }

                VerticalDivider(
                    color = Color(0xFF332D48),
                    modifier = Modifier.height(36.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${tutor.reviewsCount}+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Reviews", color = SlatyTextSecondary, fontSize = 12.sp)
                }

                VerticalDivider(
                    color = Color(0xFF332D48),
                    modifier = Modifier.height(36.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("100%", color = SlatyMint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Completion", color = SlatyTextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // About section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1F1C2F))
                    .padding(16.dp)
            ) {
                Text(
                    text = "About Tutor",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tutor.bio,
                    color = Color(0xFFD6D6E0),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subjects taught
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1F1C2F))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Subjects & Expertise",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tutor.subjects.forEach { subj ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E2945)
                        ) {
                            Text(
                                text = subj,
                                color = SlatyMint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
