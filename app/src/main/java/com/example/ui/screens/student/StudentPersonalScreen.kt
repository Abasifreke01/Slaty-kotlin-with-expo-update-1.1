package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.*

@Composable
fun StudentPersonalScreen(
    user: User?,
    onUpdateProfile: (name: String, email: String, phone: String) -> Unit,
    onSwitchRole: (UserRole) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var password by remember { mutableStateOf("••••••••") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Profile Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFF231E35))
                .border(2.dp, SlatyMint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user?.name?.firstOrNull()?.uppercase() ?: "S",
                color = SlatyMint,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user?.name ?: "Student",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Role: ${user?.role?.displayName ?: "Student"} • ${user?.email ?: ""}",
            color = SlatyTextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Role Switch Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1F1C2F),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlatyMint.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Switch to Tutor Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Accept tasks, host live classes & earn 80% payouts.", color = SlatyTextSecondary, fontSize = 12.sp)
                }

                Button(
                    onClick = { onSwitchRole(UserRole.TUTOR) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                    modifier = Modifier.testTag("switch_to_tutor_button")
                ) {
                    Text("Switch", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Form
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1F1C2F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Edit Personal Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_name_input"),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_email_input"),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_phone_input"),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = defaultTextFieldColors()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onUpdateProfile(name.trim(), email.trim(), phone.trim()) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_profile_button")
                ) {
                    Text("Update Profile", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout & Delete Account
        OutlinedButton(
            onClick = onLogout,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SlatyError),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlatyError),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("logout_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = SlatyError)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Slaty Version 1.0.0 • Terms of Service & Privacy Policy",
            color = Color.Gray,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}
