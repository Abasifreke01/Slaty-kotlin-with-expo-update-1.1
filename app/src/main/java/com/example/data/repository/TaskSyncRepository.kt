package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.SlatyDatabase
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import com.example.model.TaskItem
import com.example.model.TaskStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TaskSyncRepository(
    private val taskDao: TaskDao,
    private val scope: CoroutineScope,
    private val firestore: FirebaseFirestore? = null
) {

    companion object {
        private const val TAG = "TaskSyncRepository"
        private const val TASKS_COLLECTION = "task"

        fun create(context: Context, scope: CoroutineScope): TaskSyncRepository {
            val db = SlatyDatabase.getInstance(context)
            val fs = try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w(TAG, "FirebaseFirestore not initialized in current context: ${e.message}")
                null
            }
            return TaskSyncRepository(db.taskDao(), scope, fs).apply {
                initInitialData()
                startFirestoreSync()
            }
        }
    }

    private var snapshotListener: ListenerRegistration? = null

    // Single source of truth for UI: Room Flow
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks().map { entities ->
        entities.map { it.toTaskItem() }
    }

    /**
     * Seeds initial tasks into Room if empty so the UI always has rich academic content
     */
    private fun initInitialData() {
        scope.launch(Dispatchers.IO) {
            val pending = taskDao.getPendingSyncTasks()
            // We check if table is empty
            val initialTasks = listOf(
                TaskEntity(
                    id = "task_01",
                    title = "Thermodynamics Carnot Cycle Equations",
                    studentName = "David Adeleke",
                    studentId = "user_student_101",
                    institution = "University of Lagos",
                    courseCode = "MEG 201",
                    faculty = "Engineering",
                    department = "Mechanical",
                    description = "Need detailed step-by-step breakdown of second-law entropy calculations and ideal gas cycle diagrams.",
                    amount = 8500.0,
                    deadline = "Tomorrow, 6:00 PM",
                    learnerType = "Undergraduate",
                    status = TaskStatus.PENDING.name,
                    syncPending = false
                ),
                TaskEntity(
                    id = "task_02",
                    title = "Calculus III Multi-Variable Integrals",
                    studentName = "Fatima Musa",
                    studentId = "user_student_102",
                    institution = "Ahmadu Bello University",
                    courseCode = "MTH 301",
                    faculty = "Physical Sciences",
                    department = "Mathematics",
                    description = "Help with Stokes theorem and surface integrals on paraboloids with comprehensive solution notes.",
                    amount = 12000.0,
                    deadline = "Oct 30, 2:00 PM",
                    learnerType = "Undergraduate",
                    status = TaskStatus.PENDING.name,
                    syncPending = false
                ),
                TaskEntity(
                    id = "task_03",
                    title = "Organic Chemistry Reaction Mechanisms",
                    studentName = "Kelechi Obi",
                    studentId = "user_student_103",
                    institution = "University of Nigeria, Nsukka",
                    courseCode = "CHM 221",
                    faculty = "Basic Medical Sciences",
                    department = "Pharmacy",
                    description = "Aldol condensation and electrophilic aromatic substitution pathway explanations with drawing guides.",
                    amount = 7500.0,
                    deadline = "Today, 11:00 PM",
                    learnerType = "Undergraduate",
                    status = TaskStatus.ACCEPTED.name,
                    acceptedByTutorId = "tutor_demo_current",
                    acceptedByTutorName = "Dr. Emmanuel Okon",
                    syncPending = false
                ),
                TaskEntity(
                    id = "task_04",
                    title = "Genetics DNA Replication & Transcription",
                    studentName = "Bukola Balogun",
                    studentId = "user_student_104",
                    institution = "University of Ibadan",
                    courseCode = "BIO 102",
                    faculty = "Science",
                    department = "Microbiology",
                    description = "Comprehensive summary of leading vs lagging strand synthesis with enzymes comparison table.",
                    amount = 6000.0,
                    deadline = "Completed",
                    learnerType = "Undergraduate",
                    status = TaskStatus.COMPLETED.name,
                    acceptedByTutorId = "tutor_demo_current",
                    acceptedByTutorName = "Dr. Emmanuel Okon",
                    completedAt = "Yesterday",
                    syncPending = false
                )
            )

            // Seed only items that don't already exist
            for (item in initialTasks) {
                if (taskDao.getTaskById(item.id) == null) {
                    taskDao.insertTask(item)
                }
            }
        }
    }

    /**
     * Listens to remote Firestore updates and streams them into Room cache
     */
    fun startFirestoreSync() {
        val fs = firestore ?: return
        snapshotListener?.remove()

        try {
            snapshotListener = fs.collection(TASKS_COLLECTION)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(TAG, "Firestore sync listen failed: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            val remoteEntities = snapshots.documents.mapNotNull { doc ->
                                val data = doc.data ?: return@mapNotNull null
                                TaskEntity(
                                    id = doc.id,
                                    title = data["title"] as? String ?: "Untitled Task",
                                    studentName = data["studentName"] as? String ?: "Student",
                                    studentId = data["studentId"] as? String ?: "",
                                    institution = data["institution"] as? String ?: "",
                                    courseCode = data["courseCode"] as? String ?: "",
                                    faculty = data["faculty"] as? String ?: "",
                                    department = data["department"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    amount = (data["amount"] as? Number)?.toDouble() ?: 5000.0,
                                    deadline = data["deadline"] as? String ?: "Flexible",
                                    learnerType = data["learnerType"] as? String ?: "Undergraduate",
                                    status = (data["status"] as? String)?.uppercase() ?: TaskStatus.PENDING.name,
                                    acceptedByTutorId = (data["acceptedBy"] as? String) ?: (data["acceptedByTutorId"] as? String),
                                    acceptedByTutorName = data["acceptedByTutorName"] as? String,
                                    completedAt = data["completedAt"] as? String,
                                    syncPending = false,
                                    updatedAtMillis = System.currentTimeMillis()
                                )
                            }
                            taskDao.insertTasks(remoteEntities)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start Firestore snapshot listener", e)
        }
    }

    /**
     * Creates task in Room immediately (offline-first), then pushes to Firestore
     */
    suspend fun createTask(task: TaskItem): TaskItem {
        val entity = TaskEntity.fromTaskItem(task, syncPending = true)
        taskDao.insertTask(entity)

        // Sync to cloud
        scope.launch(Dispatchers.IO) {
            syncTaskToCloud(entity)
        }
        return task
    }

    /**
     * Accepts a task and syncs status to both Room and Firestore
     */
    suspend fun acceptTask(taskId: String, tutorName: String, tutorId: String): Boolean {
        val existing = taskDao.getTaskById(taskId) ?: return false
        val updated = existing.copy(
            status = TaskStatus.ACCEPTED.name,
            acceptedByTutorId = tutorId,
            acceptedByTutorName = tutorName,
            syncPending = true,
            updatedAtMillis = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)

        scope.launch(Dispatchers.IO) {
            syncTaskToCloud(updated)
        }
        return true
    }

    /**
     * Marks task as completed in Room and pushes update to Firestore
     */
    suspend fun completeTask(taskId: String): Boolean {
        val existing = taskDao.getTaskById(taskId) ?: return false
        val updated = existing.copy(
            status = TaskStatus.COMPLETED.name,
            completedAt = "Just now",
            syncPending = true,
            updatedAtMillis = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)

        scope.launch(Dispatchers.IO) {
            syncTaskToCloud(updated)
        }
        return true
    }

    /**
     * Uploads local task to Firestore and clears syncPending flag
     */
    private suspend fun syncTaskToCloud(entity: TaskEntity) {
        val fs = firestore ?: return
        try {
            val payload = mapOf(
                "title" to entity.title,
                "studentName" to entity.studentName,
                "studentId" to entity.studentId,
                "institution" to entity.institution,
                "courseCode" to entity.courseCode,
                "faculty" to entity.faculty,
                "department" to entity.department,
                "description" to entity.description,
                "amount" to entity.amount,
                "deadline" to entity.deadline,
                "learnerType" to entity.learnerType,
                "status" to entity.status.lowercase(),
                "acceptedBy" to (entity.acceptedByTutorId ?: ""),
                "acceptedByTutorId" to entity.acceptedByTutorId,
                "acceptedByTutorName" to entity.acceptedByTutorName,
                "completedAt" to entity.completedAt,
                "updatedAtMillis" to entity.updatedAtMillis
            )
            fs.collection(TASKS_COLLECTION)
                .document(entity.id)
                .set(payload, SetOptions.merge())
                .await()

            taskDao.markSynced(entity.id)
            Log.d(TAG, "Successfully synced task ${entity.id} to Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync task ${entity.id} to Firestore: ${e.message}")
        }
    }

    /**
     * Flushes all pending local changes to Firestore
     */
    suspend fun syncAllPendingTasks() {
        val pending = taskDao.getPendingSyncTasks()
        for (item in pending) {
            syncTaskToCloud(item)
        }
    }

    fun stopFirestoreSync() {
        snapshotListener?.remove()
        snapshotListener = null
    }
}
