package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSubmissionScreen(
    user: User?,
    initialTopic: String,
    onBack: () -> Unit,
    onSubmit: (
        title: String,
        courseCode: String,
        institution: String,
        faculty: String,
        department: String,
        description: String,
        amount: Double,
        deadline: String,
        learnerType: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var learnerType by remember { mutableStateOf("Undergraduate") }
    var title by remember { mutableStateOf(initialTopic.ifEmpty { "Thermodynamics Entropy Calculations" }) }
    var courseCode by remember { mutableStateOf("MEG 201") }
    var institution by remember { mutableStateOf("University of Lagos") }
    var faculty by remember { mutableStateOf("Faculty of Engineering") }
    var department by remember { mutableStateOf("Mechanical Engineering") }
    var description by remember { mutableStateOf("Require step-by-step mathematical proof of second law entropy change for an ideal gas undergoing an irreversible adiabatic expansion.") }
    var amountText by remember { mutableStateOf("8500") }
    var deadline by remember { mutableStateOf("Tomorrow, 6:00 PM") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solve a Problem / Task", color = Color.White, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Describe your academic problem or assignment for verified tutors to solve and explain.",
                color = SlatyTextSecondary,
                fontSize = 13.sp
            )

            // Learner Type
            Text("Learner Level", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Undergraduate", "Postgraduate", "High School").forEach { type ->
                    val isSelected = learnerType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { learnerType = type },
                        label = { Text(type, color = if (isSelected) Color.Black else Color.White, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SlatyMint,
                            containerColor = Color(0xFF1F1C2F)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color(0xFF332D48),
                            selectedBorderColor = SlatyMint
                        )
                    )
                }
            }

            // Topic / Task Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Problem Topic / Title") },
                modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                colors = defaultTextFieldColors()
            )

            // Course Code & Institution
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text("Course Code") },
                    modifier = Modifier.weight(1f).testTag("task_course_input"),
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Institution / School") },
                    modifier = Modifier.weight(1.5f).testTag("task_institution_input"),
                    colors = defaultTextFieldColors()
                )
            }

            // Faculty & Department
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = faculty,
                    onValueChange = { faculty = it },
                    label = { Text("Faculty") },
                    modifier = Modifier.weight(1f),
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.weight(1f),
                    colors = defaultTextFieldColors()
                )
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Instructions / Question") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("task_desc_input"),
                colors = defaultTextFieldColors()
            )

            // Budget & Deadline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Reward Amount (₦)") },
                    modifier = Modifier.weight(1f).testTag("task_amount_input"),
                    colors = defaultTextFieldColors()
                )
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    label = { Text("Deadline") },
                    modifier = Modifier.weight(1f),
                    colors = defaultTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 5000.0
                    onSubmit(
                        title.trim(),
                        courseCode.trim(),
                        institution.trim(),
                        faculty.trim(),
                        department.trim(),
                        description.trim(),
                        amount,
                        deadline.trim(),
                        learnerType
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_task_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Post Problem & Find Tutors", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun defaultTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = SlatyMint,
    unfocusedBorderColor = Color(0xFF332D48),
    focusedLabelColor = SlatyMint,
    unfocusedLabelColor = Color(0xFFA0A3BD),
    focusedContainerColor = Color(0xFF1F1C2F),
    unfocusedContainerColor = Color(0xFF1F1C2F)
)
