package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Repository to manage student assignments and test preparation tasks locally
 * using Room Database.
 */
class TaskRepository(
    private val studentTaskDao: StudentTaskDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    constructor(context: Context) : this(
        SlatyDatabase.getInstance(context).studentTaskDao()
    )

    // Reactive streams from Room
    val allTasks: Flow<List<StudentTaskEntity>> = studentTaskDao.getAllTasks()
    val assignments: Flow<List<StudentTaskEntity>> = studentTaskDao.getAssignments()
    val testPrepTasks: Flow<List<StudentTaskEntity>> = studentTaskDao.getTestPrepTasks()
    val pendingTasks: Flow<List<StudentTaskEntity>> = studentTaskDao.getPendingTasks()
    val completedTasks: Flow<List<StudentTaskEntity>> = studentTaskDao.getCompletedTasks()

    val totalCount: Flow<Int> = studentTaskDao.getTotalCount()
    val pendingCount: Flow<Int> = studentTaskDao.getPendingCount()
    val completedCount: Flow<Int> = studentTaskDao.getCompletedCount()

    fun getTasksByType(type: StudentTaskType): Flow<List<StudentTaskEntity>> {
        return studentTaskDao.getTasksByType(type.name)
    }

    fun getTasksBySubject(subject: String): Flow<List<StudentTaskEntity>> {
        return studentTaskDao.getTasksBySubject(subject)
    }

    fun observeTaskById(id: String): Flow<StudentTaskEntity?> {
        return studentTaskDao.observeTaskById(id)
    }

    suspend fun getTaskById(id: String): StudentTaskEntity? = withContext(ioDispatcher) {
        studentTaskDao.getTaskById(id)
    }

    suspend fun insertTask(task: StudentTaskEntity) = withContext(ioDispatcher) {
        studentTaskDao.insertTask(task)
    }

    suspend fun insertTasks(tasks: List<StudentTaskEntity>) = withContext(ioDispatcher) {
        studentTaskDao.insertTasks(tasks)
    }

    suspend fun updateTask(task: StudentTaskEntity) = withContext(ioDispatcher) {
        studentTaskDao.updateTask(task.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun deleteTask(id: String) = withContext(ioDispatcher) {
        studentTaskDao.deleteTaskById(id)
    }

    suspend fun deleteTask(task: StudentTaskEntity) = withContext(ioDispatcher) {
        studentTaskDao.deleteTask(task)
    }

    /**
     * Toggles task completion.
     * When completed, progress is updated to 100% and completion timestamp is recorded.
     * When un-toggled, status reverts to PENDING and progress is restored to 0%.
     */
    suspend fun toggleTaskCompletion(id: String, isCompleted: Boolean) = withContext(ioDispatcher) {
        val newStatus = if (isCompleted) StudentTaskStatus.COMPLETED.name else StudentTaskStatus.PENDING.name
        val completedAt = if (isCompleted) "Just now" else null
        val progress = if (isCompleted) 100 else 0
        studentTaskDao.updateCompletionStatus(
            id = id,
            completed = isCompleted,
            status = newStatus,
            completedAt = completedAt,
            progress = progress,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Updates progress percentage (0 - 100) and adapts the task status accordingly.
     */
    suspend fun updateProgress(id: String, progress: Int) = withContext(ioDispatcher) {
        val clampedProgress = progress.coerceIn(0, 100)
        val status = when {
            clampedProgress >= 100 -> StudentTaskStatus.COMPLETED.name
            clampedProgress > 0 -> StudentTaskStatus.IN_PROGRESS.name
            else -> StudentTaskStatus.PENDING.name
        }
        val isCompleted = clampedProgress >= 100
        val completedAt = if (isCompleted) "Just now" else null

        studentTaskDao.updateCompletionStatus(
            id = id,
            completed = isCompleted,
            status = status,
            completedAt = completedAt,
            progress = clampedProgress,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Seeds initial academic tasks into Room if empty, providing realistic
     * Nigerian university & pre-varsity assignments and test prep drills.
     */
    suspend fun seedInitialAcademicTasksIfEmpty() = withContext(ioDispatcher) {
        val currentCount = studentTaskDao.getTotalCount().first()
        if (currentCount == 0) {
            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            val initialTasks = listOf(
                StudentTaskEntity(
                    id = "assign_001",
                    title = "Thermodynamics Carnot Cycle Equations",
                    description = "Submit derivation of Rankine and Brayton heat engine cycles with PV/TS indicator diagrams.",
                    taskType = StudentTaskType.ASSIGNMENT.name,
                    subject = "Engineering",
                    courseCode = "MEG 201",
                    dueDate = "Tomorrow, 5:00 PM",
                    dueDateMillis = now + oneDay,
                    priority = StudentTaskPriority.HIGH.name,
                    status = StudentTaskStatus.IN_PROGRESS.name,
                    progressPercentage = 45,
                    estimatedMinutes = 120,
                    notes = "Refer to Dr. Emmanuel Okon's lecture notes chapters 3 and 4.",
                    isCompleted = false
                ),
                StudentTaskEntity(
                    id = "assign_002",
                    title = "Multi-Variable Calculus Problem Set #3",
                    description = "Solve Stokes theorem and Green's theorem problems on paraboloid surfaces (Questions 1 - 12).",
                    taskType = StudentTaskType.ASSIGNMENT.name,
                    subject = "Mathematics",
                    courseCode = "MTH 301",
                    dueDate = "Friday, 11:59 PM",
                    dueDateMillis = now + (3 * oneDay),
                    priority = StudentTaskPriority.URGENT.name,
                    status = StudentTaskStatus.PENDING.name,
                    progressPercentage = 15,
                    estimatedMinutes = 150,
                    notes = "Submit via the departmental portal before midnight.",
                    isCompleted = false
                ),
                StudentTaskEntity(
                    id = "assign_003",
                    title = "Organic Chemistry Synthesis Pathways",
                    description = "Draw full electron-pushing mechanisms for Aldol Condensation and Friedel-Crafts acylation.",
                    taskType = StudentTaskType.ASSIGNMENT.name,
                    subject = "Chemistry",
                    courseCode = "CHM 221",
                    dueDate = "Next Monday, 9:00 AM",
                    dueDateMillis = now + (5 * oneDay),
                    priority = StudentTaskPriority.MEDIUM.name,
                    status = StudentTaskStatus.PENDING.name,
                    progressPercentage = 0,
                    estimatedMinutes = 90,
                    notes = "Check Amina Bello's reference handout.",
                    isCompleted = false
                ),
                StudentTaskEntity(
                    id = "prep_001",
                    title = "UTME / JAMB Physics Mock Speed Drill",
                    description = "Practice 50 timed questions on Mechanics, Wave Motion, and Direct Current Electricity.",
                    taskType = StudentTaskType.TEST_PREPARATION.name,
                    subject = "Physics",
                    courseCode = "JAMB-CBT",
                    dueDate = "Saturday, 10:00 AM",
                    dueDateMillis = now + (2 * oneDay),
                    priority = StudentTaskPriority.HIGH.name,
                    status = StudentTaskStatus.IN_PROGRESS.name,
                    progressPercentage = 60,
                    estimatedMinutes = 60,
                    targetScore = 85,
                    notes = "Target score is 85% to secure university engineering cutoff score.",
                    isCompleted = false
                ),
                StudentTaskEntity(
                    id = "prep_002",
                    title = "WAEC Chemistry Past Questions (2020-2024)",
                    description = "Revision of volumetric analysis, redox equations, and periodic trends.",
                    taskType = StudentTaskType.TEST_PREPARATION.name,
                    subject = "Chemistry",
                    courseCode = "WAEC-PREP",
                    dueDate = "Sunday, 4:00 PM",
                    dueDateMillis = now + (4 * oneDay),
                    priority = StudentTaskPriority.MEDIUM.name,
                    status = StudentTaskStatus.PENDING.name,
                    progressPercentage = 20,
                    estimatedMinutes = 90,
                    targetScore = 80,
                    notes = "Focus on titration calculations and qualitative salt analysis.",
                    isCompleted = false
                ),
                StudentTaskEntity(
                    id = "prep_003",
                    title = "General Studies Lexis & Structure Quiz Prep",
                    description = "Review concord rules, sentence completions, and idiomatic expressions.",
                    taskType = StudentTaskType.PRACTICE_QUIZ.name,
                    subject = "General Studies",
                    courseCode = "GST 111",
                    dueDate = "Completed",
                    dueDateMillis = now - oneDay,
                    priority = StudentTaskPriority.LOW.name,
                    status = StudentTaskStatus.COMPLETED.name,
                    progressPercentage = 100,
                    estimatedMinutes = 45,
                    targetScore = 90,
                    notes = "Achieved 92% on pre-test mock.",
                    isCompleted = true,
                    completedAt = "Yesterday"
                )
            )

            studentTaskDao.insertTasks(initialTasks)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TaskRepository? = null

        fun getInstance(context: Context): TaskRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = TaskRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
