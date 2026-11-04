package com.example.ui.screens.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import com.example.model.TaskItem
import com.example.model.TaskStatus
import com.example.model.User
import com.example.ui.theme.*

@Composable
fun TutorDashboardScreen(
    user: User?,
    tasks: List<TaskItem>,
    onCompleteTask: (String) -> Unit,
    onFindTopicsClick: () -> Unit,
    onHostClassClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBanner by remember { mutableStateOf(true) }

    val myTasks = tasks.filter {
        it.acceptedByTutorId != null || it.status != TaskStatus.PENDING
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tutor Banner (matching TutorComponents/Banner.tsx)
        if (showBanner) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1B3B36),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlatyMint),
                    modifier = Modifier.fillMaxWidth().testTag("tutor_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SlatyMint.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = SlatyMint)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verified Tutor Dashboard",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Earn 80% on tasks and host live classes for students across universities.",
                                color = SlatySoftTeal,
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { showBanner = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Quick Stats row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1F1C2F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Active Tasks", color = SlatyTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${myTasks.count { it.status == TaskStatus.ACCEPTED }}",
                            color = SlatyMint,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1F1C2F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Completed", color = SlatyTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${myTasks.count { it.status == TaskStatus.COMPLETED }}",
                            color = SlatyLavender,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Action Quick Bar: Find More Tasks & Host Live Class
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onFindTopicsClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("tutor_browse_tasks_button")
                ) {
                    Icon(Icons.Default.WorkOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Browse Tasks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onHostClassClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlatyMint),
                    modifier = Modifier.weight(1f).height(46.dp).testTag("tutor_host_live_button")
                ) {
                    Icon(Icons.Default.VideoCall, contentDescription = null, tint = SlatyMint, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Host Class", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Active Activity Section
        item {
            Text(
                text = "My Activity & Accepted Tasks",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (myTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1F1C2F),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No active tasks yet", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Browse student requests in the Find tab to accept assignments and earn ₦.",
                            color = SlatyTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(myTasks, key = { it.id }) { task ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1F1C2F),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (task.status == TaskStatus.COMPLETED) Color(0xFF332D48) else SlatyMint.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("tutor_task_${task.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (task.status == TaskStatus.COMPLETED) Color(0xFF28243A) else Color(0xFF183B33)
                            ) {
                                Text(
                                    text = task.status.name,
                                    color = if (task.status == TaskStatus.COMPLETED) Color.Gray else SlatyMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "₦${(task.amount * 0.8).toInt()} (80%)",
                                color = SlatyMint,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = task.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Student: ${task.studentName} • ${task.institution} • ${task.courseCode}",
                            color = SlatyTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = task.description,
                            color = Color(0xFFD6D6E0),
                            fontSize = 13.sp,
                            maxLines = 2
                        )

                        if (task.status == TaskStatus.ACCEPTED) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onCompleteTask(task.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("complete_task_${task.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Solution as Completed", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
