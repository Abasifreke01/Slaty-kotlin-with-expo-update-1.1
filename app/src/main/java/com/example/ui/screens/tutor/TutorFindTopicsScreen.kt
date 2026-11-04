package com.example.ui.screens.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.TaskItem
import com.example.model.TaskStatus
import com.example.ui.theme.*

@Composable
fun TutorFindTopicsScreen(
    tasks: List<TaskItem>,
    onAcceptTask: (TaskItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Find Academic Tasks",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${pendingTasks.size} students seeking tutor solutions",
                    color = SlatyTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (pendingTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = SlatyMint, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("All tasks currently accepted!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Check back soon for new student assignments.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 130.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(pendingTasks, key = { it.id }) { task ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF1F1C2F),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2945)),
                        modifier = Modifier.fillMaxWidth().testTag("pending_task_${task.id}")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF28233D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = task.studentName.firstOrNull()?.uppercase() ?: "S",
                                        color = SlatyMint,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${task.studentName} • ${task.institution}",
                                        color = SlatyTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₦${task.amount.toInt()}",
                                        color = SlatyMint,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Reward",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF28243A)
                                ) {
                                    Text(
                                        text = task.courseCode,
                                        color = SlatyLavender,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF28243A)
                                ) {
                                    Text(
                                        text = "Due: ${task.deadline}",
                                        color = Color(0xFFD0D0DF),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = task.description,
                                color = Color(0xFFE2E2EC),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onAcceptTask(task) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("accept_task_btn_${task.id}")
                            ) {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accept Task (Earn ₦${(task.amount * 0.8).toInt()})", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
