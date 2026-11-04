package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.TaskItem
import com.example.model.TaskStatus

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val studentName: String,
    val studentId: String,
    val institution: String,
    val courseCode: String,
    val faculty: String = "",
    val department: String = "",
    val description: String,
    val amount: Double,
    val deadline: String,
    val learnerType: String = "Undergraduate",
    val status: String = TaskStatus.PENDING.name,
    val acceptedByTutorId: String? = null,
    val acceptedByTutorName: String? = null,
    val completedAt: String? = null,
    val syncPending: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toTaskItem(): TaskItem {
        val parsedStatus = try {
            TaskStatus.valueOf(status)
        } catch (e: Exception) {
            TaskStatus.PENDING
        }
        return TaskItem(
            id = id,
            title = title,
            studentName = studentName,
            studentId = studentId,
            institution = institution,
            courseCode = courseCode,
            faculty = faculty,
            department = department,
            description = description,
            amount = amount,
            deadline = deadline,
            learnerType = learnerType,
            status = parsedStatus,
            acceptedByTutorId = acceptedByTutorId,
            acceptedByTutorName = acceptedByTutorName,
            completedAt = completedAt
        )
    }

    companion object {
        fun fromTaskItem(item: TaskItem, syncPending: Boolean = false): TaskEntity {
            return TaskEntity(
                id = item.id,
                title = item.title,
                studentName = item.studentName,
                studentId = item.studentId,
                institution = item.institution,
                courseCode = item.courseCode,
                faculty = item.faculty,
                department = item.department,
                description = item.description,
                amount = item.amount,
                deadline = item.deadline,
                learnerType = item.learnerType,
                status = item.status.name,
                acceptedByTutorId = item.acceptedByTutorId,
                acceptedByTutorName = item.acceptedByTutorName,
                completedAt = item.completedAt,
                syncPending = syncPending,
                updatedAtMillis = System.currentTimeMillis()
            )
        }
    }
}
