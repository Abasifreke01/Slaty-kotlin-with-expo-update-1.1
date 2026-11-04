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
import com.example.model.UserRole
import com.example.ui.screens.student.defaultTextFieldColors
import com.example.ui.theme.*

@Composable
fun TutorPayoutsScreen(
    user: User?,
    tasks: List<TaskItem>,
    onSavePayoutMethod: (bank: String, accountNum: String, accountName: String) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bankName by remember(user) { mutableStateOf(user?.bankName ?: "Guaranty Trust Bank") }
    var accountNumber by remember(user) { mutableStateOf(user?.accountNumber ?: "0123456789") }
    var accountName by remember(user) { mutableStateOf(user?.accountName ?: (user?.name ?: "David Adeleke")) }

    val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }
    val totalEarnings = completedTasks.sumOf { it.amount * 0.8 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Big Earnings Banner (matching TutorPayments.tsx)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SlatyPrimary,
                modifier = Modifier.fillMaxWidth().testTag("earnings_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Your Earnings (80%)", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₦${String.format("%,.0f", totalEarnings)}",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "A 20% processing fee is applied. Payouts transferred weekly.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Payout Method form (matching TutorPayments.tsx)
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1F1C2F),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Bank Payout Method",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { accountNumber = it },
                        label = { Text("Account Number") },
                        modifier = Modifier.fillMaxWidth().testTag("account_number_input"),
                        colors = defaultTextFieldColors()
                    )

                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth().testTag("account_name_input"),
                        colors = defaultTextFieldColors()
                    )

                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        modifier = Modifier.fillMaxWidth().testTag("bank_name_input"),
                        colors = defaultTextFieldColors()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            onSavePayoutMethod(bankName.trim(), accountNumber.trim(), accountName.trim())
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_payout_button")
                    ) {
                        Text("Save Payout Method", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Completed tasks history
        item {
            Text(
                text = "Earnings History (${completedTasks.size} tasks)",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (completedTasks.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1F1C2F),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No completed tasks yet. Finish accepted tasks to earn!", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(completedTasks, key = { it.id }) { task ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1F1C2F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2945)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(task.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Student: ${task.studentName} • ${task.courseCode}", color = SlatyTextSecondary, fontSize = 12.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("₦${(task.amount * 0.8).toInt()}", color = SlatyMint, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("80% Payout", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Role Switch & Logout actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onSwitchRole(UserRole.STUDENT) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlatyLavender),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlatyLavender),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("switch_to_student_button")
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = SlatyLavender)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch Back to Student Mode", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Log Out of Slaty", color = SlatyError, fontSize = 13.sp)
                }
            }
        }
    }
}
