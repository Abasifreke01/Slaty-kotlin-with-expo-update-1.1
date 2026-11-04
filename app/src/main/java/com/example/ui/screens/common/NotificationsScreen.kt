package com.example.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NotificationItem
import com.example.service.SlatyFirebaseMessagingService
import com.example.ui.theme.*
import com.example.util.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedToken = remember { SlatyFirebaseMessagingService.getSavedFcmToken(context) }
    var testFeedback by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlatyDarkBackground)
            )
        },
        containerColor = SlatyDarkBackground
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // FCM Push Notifications Status & Live Testing Panel
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1A34),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlatyMint.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("fcm_testing_panel")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(SlatyMint)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Firebase Cloud Messaging (FCM)",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SlatyMint.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Active",
                                    color = SlatyMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Push notifications are active for direct messages and study groups in both foreground & background states.",
                            color = SlatyTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        if (!savedToken.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Device Token: ${savedToken.take(16)}...${savedToken.takeLast(8)}",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        testFeedback?.let { msg ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg,
                                color = SlatyMint,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    NotificationHelper.showPushNotification(
                                        context = context,
                                        title = "New Message from Dr. Emmanuel Okon",
                                        body = "Thermodynamics homework solution has been graded! Review notes.",
                                        data = mapOf(
                                            "senderName" to "Dr. Emmanuel Okon",
                                            "type" to "message",
                                            "chatId" to "chat_tutor_1"
                                        ),
                                        senderName = "Dr. Emmanuel Okon"
                                    )
                                    testFeedback = "Foreground FCM push sent! Check system notification drawer."
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SlatyMint),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("simulate_foreground_push_button")
                            ) {
                                Text("Foreground Push", color = Color(0xFF0F0C20), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    NotificationHelper.showPushNotification(
                                        context = context,
                                        title = "JAMB & UTME Master Prep",
                                        body = "Chukwudi Nnamdi: Live problem solving starts in 10 minutes!",
                                        data = mapOf(
                                            "senderName" to "JAMB Hub",
                                            "type" to "community",
                                            "channelId" to "comm_1"
                                        ),
                                        senderName = "JAMB Hub",
                                        isCommunity = true
                                    )
                                    testFeedback = "Background data push delivered! Check heads-up banner."
                                },
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6E56CF)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("simulate_background_push_button")
                            ) {
                                Text("Background Push", color = Color(0xFFB8A6F8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Notification List Items
            items(notifications, key = { it.id }) { item ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1F1C2F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48)),
                    modifier = Modifier.fillMaxWidth().testTag("notification_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF28233D)),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when (item.type) {
                                "live" -> Icons.Default.PlayCircle
                                "task" -> Icons.AutoMirrored.Filled.Assignment
                                "message" -> Icons.Default.ChatBubble
                                else -> Icons.Default.Notifications
                            }
                            Icon(icon, contentDescription = null, tint = SlatyMint, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.timeAgo,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.message,
                                color = SlatyTextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
