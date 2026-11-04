package com.example.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.StudentTaskEntity
import com.example.data.local.StudentTaskPriority
import com.example.data.local.StudentTaskType
import com.example.ui.theme.*

enum class TaskFilterOption(val label: String) {
    ALL("All"),
    ASSIGNMENTS("Assignments"),
    TEST_PREP("Test Prep"),
    PENDING("Pending"),
    COMPLETED("Completed")
}

@Composable
fun AcademicTasksSection(
    tasks: List<StudentTaskEntity>,
    onToggleTask: (id: String, isCompleted: Boolean) -> Unit,
    onUpdateProgress: (id: String, progress: Int) -> Unit,
    onCreateTask: (
        title: String,
        description: String,
        type: StudentTaskType,
        subject: String,
        courseCode: String,
        dueDate: String,
        priority: StudentTaskPriority,
        targetScore: Int
    ) -> Unit,
    onDeleteTask: (id: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(TaskFilterOption.ALL) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            TaskFilterOption.ALL -> tasks
            TaskFilterOption.ASSIGNMENTS -> tasks.filter { it.typeEnum == StudentTaskType.ASSIGNMENT }
            TaskFilterOption.TEST_PREP -> tasks.filter {
                it.typeEnum == StudentTaskType.TEST_PREPARATION ||
                        it.typeEnum == StudentTaskType.PRACTICE_QUIZ ||
                        it.typeEnum == StudentTaskType.EXAM_REVISION
            }
            TaskFilterOption.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilterOption.COMPLETED -> tasks.filter { it.isCompleted }
        }
    }

    val pendingCount = remember(tasks) { tasks.count { !it.isCompleted } }
    val completedCount = remember(tasks) { tasks.count { it.isCompleted } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("academic_tasks_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlatyMint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = "Academic Tasks",
                        tint = SlatyMint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Assignments & Test Prep",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (pendingCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE65100).copy(alpha = 0.25f),
                                border = BorderStroke(1.dp, Color(0xFFFF9800))
                            ) {
                                Text(
                                    text = "$pendingCount due",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Track homework, past questions & CBT drills locally",
                        color = SlatyTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SlatyMint,
                    contentColor = Color(0xFF0F0C20)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_academic_task_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Add",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskFilterOption.entries.forEach { option ->
                val isSelected = selectedFilter == option
                val countLabel = when (option) {
                    TaskFilterOption.ALL -> tasks.size
                    TaskFilterOption.ASSIGNMENTS -> tasks.count { it.typeEnum == StudentTaskType.ASSIGNMENT }
                    TaskFilterOption.TEST_PREP -> tasks.count {
                        it.typeEnum == StudentTaskType.TEST_PREPARATION ||
                                it.typeEnum == StudentTaskType.PRACTICE_QUIZ ||
                                it.typeEnum == StudentTaskType.EXAM_REVISION
                    }
                    TaskFilterOption.PENDING -> pendingCount
                    TaskFilterOption.COMPLETED -> completedCount
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = option },
                    label = {
                        Text(
                            text = "${option.label} ($countLabel)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SlatyMint.copy(alpha = 0.2f),
                        selectedLabelColor = SlatyMint,
                        containerColor = Color(0xFF1E1B2C),
                        labelColor = SlatyTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0xFF312A47),
                        selectedBorderColor = SlatyMint
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("filter_chip_${option.name.lowercase()}")
                )
            }
        }

        // Tasks List
        if (filteredTasks.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E1B2C),
                border = BorderStroke(1.dp, Color(0xFF312A47)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "No tasks",
                        tint = SlatyMint.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No tasks in this category",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ Add' to schedule an assignment or test preparation drill",
                        color = SlatyTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredTasks.forEach { task ->
                    AcademicTaskItemCard(
                        task = task,
                        onToggle = { onToggleTask(task.id, !task.isCompleted) },
                        onUpdateProgress = { newProgress -> onUpdateProgress(task.id, newProgress) },
                        onDelete = { onDeleteTask(task.id) }
                    )
                }
            }
        }
    }

    // Create Task Modal Dialog
    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, type, subject, code, dueDate, priority, targetScore ->
                onCreateTask(title, desc, type, subject, code, dueDate, priority, targetScore)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun AcademicTaskItemCard(
    task: StudentTaskEntity,
    onToggle: () -> Unit,
    onUpdateProgress: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val isAssignment = task.typeEnum == StudentTaskType.ASSIGNMENT
    val typeBadgeColor = if (isAssignment) SlatyMint else Color(0xFFFFB74D)
    val typeBadgeBg = if (isAssignment) SlatyMint.copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f)

    val priorityColor = when (task.priorityEnum) {
        StudentTaskPriority.URGENT -> Color(0xFFFF5252)
        StudentTaskPriority.HIGH -> Color(0xFFFF7A00)
        StudentTaskPriority.MEDIUM -> Color(0xFF4FC3F7)
        StudentTaskPriority.LOW -> Color(0xFF9E9E9E)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1B2C),
        border = BorderStroke(
            1.dp,
            if (task.isCompleted) Color(0xFF2C2740) else Color(0xFF383050)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Type Badge + Priority + Course Code + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeBadgeBg,
                        border = BorderStroke(1.dp, typeBadgeColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = task.typeEnum.displayName,
                            color = typeBadgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    // Priority Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = task.priorityEnum.displayName,
                            color = priorityColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Course Code if available
                    if (task.courseCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2B253F)
                        ) {
                            Text(
                                text = task.courseCode,
                                color = SlatyLavender,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Delete Action
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp).testTag("delete_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Task",
                        tint = SlatyTextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Middle: Checkbox + Title + Description
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SlatyMint,
                        uncheckedColor = Color(0xFF5A527A),
                        checkmarkColor = Color(0xFF0F0C20)
                    ),
                    modifier = Modifier
                        .offset(x = (-8).dp, y = (-8).dp)
                        .testTag("checkbox_${task.id}")
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = (-4).dp)
                ) {
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) SlatyTextSecondary else Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = task.description,
                            color = SlatyTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Target Score & Subject Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Due Date",
                        tint = SlatyTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (task.isCompleted) "Completed" else "Due: ${task.dueDate}",
                        color = if (task.isOverdue) Color(0xFFFF5252) else SlatyTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (task.isOverdue) FontWeight.Bold else FontWeight.Normal
                    )

                    if (task.targetScore != null) {
                        Text(
                            text = "• Target: ${task.targetScore}%",
                            color = Color(0xFFFFB74D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "${task.progressPercentage}% done",
                    color = if (task.isCompleted) SlatyMint else SlatyTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Progress Bar & Quick Adjust
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { task.progressPercentage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (task.isCompleted) SlatyMint else if (isAssignment) SlatyPrimary else Color(0xFFFF9800),
                    trackColor = Color(0xFF13101E)
                )

                if (!task.isCompleted) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Update progress: ",
                            color = SlatyTextSecondary,
                            fontSize = 10.sp
                        )
                        listOf(25, 50, 75, 100).forEach { pct ->
                            Text(
                                text = "$pct%",
                                color = if (task.progressPercentage == pct) SlatyMint else SlatyLavender,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .clickable { onUpdateProgress(pct) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (
        title: String,
        description: String,
        type: StudentTaskType,
        subject: String,
        courseCode: String,
        dueDate: String,
        priority: StudentTaskPriority,
        targetScore: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StudentTaskType.ASSIGNMENT) }
    var subject by remember { mutableStateOf("Mathematics") }
    var courseCode by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Tomorrow, 5:00 PM") }
    var selectedPriority by remember { mutableStateOf(StudentTaskPriority.HIGH) }
    var targetScore by remember { mutableIntStateOf(85) }

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "Engineering", "General Studies")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1B2C),
            border = BorderStroke(1.dp, Color(0xFF383050)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Academic Task",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SlatyTextSecondary)
                    }
                }

                // Type Toggle (Assignment vs Test Prep)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF13101E))
                        .padding(4.dp)
                ) {
                    listOf(StudentTaskType.ASSIGNMENT, StudentTaskType.TEST_PREPARATION).forEach { type ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SlatyMint else Color.Transparent)
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.displayName,
                                color = if (isSelected) Color(0xFF0F0C20) else SlatyTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = {
                        Text(if (selectedType == StudentTaskType.ASSIGNMENT) "e.g. MTH 301 Assignment #3" else "e.g. JAMB Physics Mock Drill")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SlatyMint,
                        unfocusedBorderColor = Color(0xFF383050),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = SlatyMint,
                        unfocusedLabelColor = SlatyTextSecondary
                    ),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Instructions") },
                    placeholder = { Text("Topics, problem numbers, or exam syllabus") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SlatyMint,
                        unfocusedBorderColor = Color(0xFF383050),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = SlatyMint,
                        unfocusedLabelColor = SlatyTextSecondary
                    ),
                    maxLines = 3
                )

                // Subject Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Subject", color = SlatyTextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { s ->
                            FilterChip(
                                selected = subject == s,
                                onClick = { subject = s },
                                label = { Text(s, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SlatyMint.copy(alpha = 0.25f),
                                    selectedLabelColor = SlatyMint,
                                    containerColor = Color(0xFF13101E),
                                    labelColor = SlatyTextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = subject == s,
                                    borderColor = Color(0xFF312A47),
                                    selectedBorderColor = SlatyMint
                                )
                            )
                        }
                    }
                }

                // Course Code & Due Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Code") },
                        placeholder = { Text("e.g. MTH 201") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SlatyMint,
                            unfocusedBorderColor = Color(0xFF383050),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = SlatyMint,
                            unfocusedLabelColor = SlatyTextSecondary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        placeholder = { Text("e.g. Tomorrow 5 PM") },
                        modifier = Modifier.weight(1.3f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SlatyMint,
                            unfocusedBorderColor = Color(0xFF383050),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = SlatyMint,
                            unfocusedLabelColor = SlatyTextSecondary
                        ),
                        singleLine = true
                    )
                }

                // Priority Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Priority", color = SlatyTextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(StudentTaskPriority.LOW, StudentTaskPriority.MEDIUM, StudentTaskPriority.HIGH, StudentTaskPriority.URGENT).forEach { p ->
                            val isSelected = selectedPriority == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF332A50) else Color(0xFF13101E))
                                    .clickable { selectedPriority = p }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.displayName,
                                    color = if (isSelected) SlatyMint else SlatyTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Target Score (if Test Prep)
                if (selectedType == StudentTaskType.TEST_PREPARATION) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Target Exam Score:", color = SlatyTextSecondary, fontSize = 12.sp)
                        Text("$targetScore%", color = Color(0xFFFFB74D), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = targetScore.toFloat(),
                        onValueChange = { targetScore = it.toInt() },
                        valueRange = 50f..100f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFB74D),
                            activeTrackColor = Color(0xFFFFB74D),
                            inactiveTrackColor = Color(0xFF13101E)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onCreate(
                                title.trim(),
                                description.trim(),
                                selectedType,
                                subject,
                                courseCode.trim().uppercase(),
                                dueDate.trim(),
                                selectedPriority,
                                targetScore
                            )
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SlatyMint,
                        contentColor = Color(0xFF0F0C20)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save to Local Database", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
