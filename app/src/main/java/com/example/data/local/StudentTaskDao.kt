package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentTaskDao {

    @Query("SELECT * FROM student_academic_tasks ORDER BY isCompleted ASC, dueDateMillis ASC, updatedAtMillis DESC")
    fun getAllTasks(): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_academic_tasks WHERE taskType = :taskType ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getTasksByType(taskType: String): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_academic_tasks WHERE taskType = 'ASSIGNMENT' ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAssignments(): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_academic_tasks WHERE taskType = 'TEST_PREPARATION' OR taskType = 'PRACTICE_QUIZ' OR taskType = 'EXAM_REVISION' ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getTestPrepTasks(): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_academic_tasks WHERE isCompleted = 0 ORDER BY dueDateMillis ASC")
    fun getPendingTasks(): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_academic_tasks WHERE isCompleted = 1 ORDER BY updatedAtMillis DESC")
    fun getCompletedTasks(): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_academic_tasks WHERE subject = :subject ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getTasksBySubject(subject: String): Flow<List<StudentTaskEntity>>

    @Query("SELECT * FROM student_academic_tasks WHERE id = :id LIMIT 1")
    fun observeTaskById(id: String): Flow<StudentTaskEntity?>

    @Query("SELECT * FROM student_academic_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): StudentTaskEntity?

    @Query("SELECT COUNT(*) FROM student_academic_tasks")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM student_academic_tasks WHERE isCompleted = 0")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM student_academic_tasks WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudentTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudentTaskEntity>)

    @Update
    suspend fun updateTask(task: StudentTaskEntity)

    @Query("DELETE FROM student_academic_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Delete
    suspend fun deleteTask(task: StudentTaskEntity)

    @Query("DELETE FROM student_academic_tasks")
    suspend fun clearAllTasks()

    @Query("""
        UPDATE student_academic_tasks 
        SET isCompleted = :completed, 
            status = :status, 
            completedAt = :completedAt, 
            progressPercentage = :progress, 
            updatedAtMillis = :updatedAt 
        WHERE id = :id
    """)
    suspend fun updateCompletionStatus(
        id: String,
        completed: Boolean,
        status: String,
        completedAt: String?,
        progress: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE student_academic_tasks 
        SET progressPercentage = :progress, 
            status = :status, 
            updatedAtMillis = :updatedAt 
        WHERE id = :id
    """)
    suspend fun updateProgress(
        id: String,
        progress: Int,
        status: String,
        updatedAt: Long = System.currentTimeMillis()
    )
}
