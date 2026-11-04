package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun TopicActionDialog(
    topic: String,
    onDismiss: () -> Unit,
    onWantLiveClass: (String) -> Unit,
    onWantSolveTask: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1F1C2F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333045)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("topic_action_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOPIC: $topic",
                    color = SlatyMint,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "How would you like to proceed?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Option 1: Live Class
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2A273F),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWantLiveClass(topic) }
                        .testTag("option_live_class")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(SlatyMint)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "I want a live class",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: Solve task immediately
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2A273F),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWantSolveTask(topic) }
                        .testTag("option_solve_task")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(SlatyLavender)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "I want to solve a task immediately",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_topic_dialog")
                ) {
                    Text(
                        text = "Maybe later",
                        color = Color(0xFFAAAAAA),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CreateCommunityDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, desc: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Higher Ed") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1F1C2F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333045)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_community_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Create Study Community",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Community Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatyMint,
                        unfocusedBorderColor = Color(0xFF444055)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("community_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatyMint,
                        unfocusedBorderColor = Color(0xFF444055)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("community_desc_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Exam Prep, Higher Ed)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SlatyMint,
                        unfocusedBorderColor = Color(0xFF444055)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreate(name.trim(), description.trim(), category.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("create_community_confirm_button")
                    ) {
                        Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
