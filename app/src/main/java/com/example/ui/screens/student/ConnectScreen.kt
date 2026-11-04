package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.Community
import com.example.model.PrivateConversation
import com.example.ui.theme.*

@Composable
fun ConnectScreen(
    communities: List<Community>,
    privateChats: List<PrivateConversation>,
    onCommunityClick: (Community) -> Unit,
    onPrivateChatClick: (PrivateConversation) -> Unit,
    onCreateCommunityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSegment by remember { mutableStateOf("communities") } // "communities" or "chats"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Segmented Control (matching original Connect.jsx)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1F1C2F))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Communities tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedSegment = "communities" }
                    .testTag("segment_communities"),
                color = if (selectedSegment == "communities") SlatyMint else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Communities (${communities.size})",
                        color = if (selectedSegment == "communities") Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Private Chats tab
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedSegment = "chats" }
                    .testTag("segment_chats"),
                color = if (selectedSegment == "chats") SlatyMint else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Direct Chats (${privateChats.size})",
                        color = if (selectedSegment == "chats") Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Sub-header with Create action for communities
        if (selectedSegment == "communities") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Study Groups & Hubs",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onCreateCommunityClick,
                    modifier = Modifier.testTag("create_community_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = SlatyMint, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Group", color = SlatyMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Content List
        if (selectedSegment == "communities") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 130.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(communities, key = { it.id }) { comm ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onCommunityClick(comm) }
                            .testTag("community_item_${comm.id}"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1F1C2F),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E2442)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = SlatyMint, modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comm.name,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (comm.unreadCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(SlatyMint)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = comm.description,
                                    color = SlatyTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${comm.memberCount} active learners • ${comm.category}",
                                    color = Color(0xFF748C94),
                                    fontSize = 11.sp
                                )
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        } else {
            // Private chats list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 130.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(privateChats, key = { it.id }) { chat ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onPrivateChatClick(chat) }
                            .testTag("chat_item_${chat.id}"),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1F1C2F),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B263F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chat.otherUserName.firstOrNull()?.uppercase() ?: "T",
                                    color = SlatyLavender,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = chat.otherUserName,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = chat.lastMessageTime,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = chat.lastMessage,
                                    color = SlatyTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            if (chat.unreadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(SlatyMint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${chat.unreadCount}",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
