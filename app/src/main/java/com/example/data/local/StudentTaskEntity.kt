package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class StudentTaskType(val displayName: String) {
    ASSIGNMENT("Assignment"),
    TEST_PREPARATION("Test Prep"),
    PRACTICE_QUIZ("Practice Quiz"),
    EXAM_REVISION("Exam Revision");

    companion object {
        fun fromString(value: String): StudentTaskType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ASSIGNMENT
        }
    }
}

enum class StudentTaskPriority(val displayName: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent");

    companion object {
        fun fromString(value: String): StudentTaskPriority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class StudentTaskStatus(val displayName: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    companion object {
        fun fromString(value: String): StudentTaskStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

@Entity(tableName = "student_academic_tasks")
data class StudentTaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val taskType: String = StudentTaskType.ASSIGNMENT.name,
    val subject: String = "General",
    val courseCode: String = "",
    val dueDate: String = "Upcoming",
    val dueDateMillis: Long = System.currentTimeMillis() + 86400000L, // Default +1 day
    val priority: String = StudentTaskPriority.MEDIUM.name,
    val status: String = StudentTaskStatus.PENDING.name,
    val progressPercentage: Int = 0,
    val estimatedMinutes: Int = 60,
    val targetScore: Int? = null,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val typeEnum: StudentTaskType
        get() = StudentTaskType.fromString(taskType)

    val priorityEnum: StudentTaskPriority
        get() = StudentTaskPriority.fromString(priority)

    val statusEnum: StudentTaskStatus
        get() = StudentTaskStatus.fromString(status)

    val isOverdue: Boolean
        get() = !isCompleted && System.currentTimeMillis() > dueDateMillis

    companion object {
        fun createAssignment(
            title: String,
            description: String,
            subject: String,
            courseCode: String,
            dueDate: String,
            dueDateMillis: Long = System.currentTimeMillis() + 2 * 86400000L,
            priority: StudentTaskPriority = StudentTaskPriority.MEDIUM,
            estimatedMinutes: Int = 90,
            notes: String = ""
        ): StudentTaskEntity {
            return StudentTaskEntity(
                id = "assign_${UUID.randomUUID().toString().take(8)}",
                title = title,
                description = description,
                taskType = StudentTaskType.ASSIGNMENT.name,
                subject = subject,
                courseCode = courseCode,
                dueDate = dueDate,
                dueDateMillis = dueDateMillis,
                priority = priority.name,
                status = StudentTaskStatus.PENDING.name,
                progressPercentage = 0,
                estimatedMinutes = estimatedMinutes,
                notes = notes,
                isCompleted = false
            )
        }

        fun createTestPrep(
            title: String,
            description: String,
            subject: String,
            courseCode: String,
            dueDate: String,
            targetScore: Int = 85,
            dueDateMillis: Long = System.currentTimeMillis() + 3 * 86400000L,
            priority: StudentTaskPriority = StudentTaskPriority.HIGH,
            estimatedMinutes: Int = 60,
            notes: String = ""
        ): StudentTaskEntity {
            return StudentTaskEntity(
                id = "prep_${UUID.randomUUID().toString().take(8)}",
                title = title,
                description = description,
                taskType = StudentTaskType.TEST_PREPARATION.name,
                subject = subject,
                courseCode = courseCode,
                dueDate = dueDate,
                dueDateMillis = dueDateMillis,
                priority = priority.name,
                status = StudentTaskStatus.PENDING.name,
                progressPercentage = 0,
                estimatedMinutes = estimatedMinutes,
                targetScore = targetScore,
                notes = notes,
                isCompleted = false
            )
        }
    }
}
