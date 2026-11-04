package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveClassSession
import com.example.ui.theme.*

@Composable
fun StudentLiveClassesScreen(
    classes: List<LiveClassSession>,
    onJoinClass: (roomCode: String, title: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var classLinkInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Direct Join by Link or ID Card (matching original LiveClasses.jsx)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1F1C2F),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48)),
                modifier = Modifier.fillMaxWidth().testTag("join_class_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E2442)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = SlatyMint)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Join Live Session by Link",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Class ID or Shareable Link", color = SlatyTextSecondary, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = classLinkInput,
                        onValueChange = { classLinkInput = it },
                        placeholder = { Text("Paste link or enter ID (e.g. slaty-math-live)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().testTag("class_link_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SlatyMint,
                            unfocusedBorderColor = Color(0xFF332D48),
                            focusedContainerColor = Color(0xFF151320),
                            unfocusedContainerColor = Color(0xFF151320)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter the link shared by your tutor to join the interactive video classroom directly.",
                        color = Color(0xFF88849E),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val code = classLinkInput.trim().ifEmpty { "slaty-general-live" }
                            onJoinClass(code, "Live Masterclass")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("join_now_button")
                    ) {
                        Text("Join Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Live & Upcoming Masterclasses
        item {
            Text(
                text = "Scheduled Masterclasses",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(classes, key = { it.id }) { session ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1F1C2F),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (session.isLive) SlatyMint else Color(0xFF332D48)
                ),
                modifier = Modifier.fillMaxWidth().testTag("class_item_${session.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (session.isLive) SlatyMint.copy(alpha = 0.2f) else Color(0xFF2A263D)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (session.isLive) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SlatyMint)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (session.isLive) "LIVE NOW" else session.scheduledTime,
                                    color = if (session.isLive) SlatyMint else SlatyLavender,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "₦${session.fee.toInt()}",
                            color = SlatyMint,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = session.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Instructor: ${session.tutorName} • ${session.subject}",
                        color = SlatyTextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onJoinClass(session.roomCode, session.title) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (session.isLive) SlatyPrimary else Color(0xFF2F2948)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = if (session.isLive) Icons.Default.PlayArrow else Icons.Default.Event,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (session.isLive) "Enter Live Classroom" else "Reserve Spot",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
