package com.example.model

enum class UserRole {
    STUDENT,
    TUTOR;

    val displayName: String
        get() = when (this) {
            STUDENT -> "Student"
            TUTOR -> "Tutor"
        }
}

data class User(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val phone: String = "",
    val role: UserRole = UserRole.STUDENT,
    val profilePicture: String? = null,
    val specialization: String? = null,
    val bio: String = "",
    val hourlyRate: Double = 3500.0,
    val rating: Double = 4.9,
    val studentsCount: Int = 142,
    val bankName: String = "GTBank",
    val accountNumber: String = "0123456789",
    val accountName: String = "User Account",
    val institution: String = "University of Lagos",
    val faculty: String = "Science",
    val department: String = "Computer Science",
    val academicLevel: String = "Undergraduate"
)

data class Tutor(
    val id: String,
    val name: String,
    val username: String,
    val specialization: String,
    val rating: Double,
    val reviewsCount: Int,
    val hourlyRate: Double,
    val bio: String,
    val subjects: List<String>,
    val verified: Boolean = true,
    val avatarResName: String = "avatar_monogram",
    val location: String = "Lagos, Nigeria",
    val academicLevels: List<String> = listOf("Undergraduate", "JAMB/UTME")
)

enum class HomeSearchTarget(val title: String) {
    ALL("All"),
    TUTORS("Tutors"),
    STUDY_GROUPS("Study Groups")
}

enum class HomeSortOption(val displayName: String) {
    HIGHEST_RATED("Highest Rated"),
    PRICE_LOW_TO_HIGH("Price (Low to High)"),
    NEWEST("Newest")
}

enum class TaskStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED
}

data class TaskItem(
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
    val status: TaskStatus = TaskStatus.PENDING,
    val acceptedByTutorId: String? = null,
    val acceptedByTutorName: String? = null,
    val completedAt: String? = null,
    val isRead: Boolean = false
)

data class LiveClassSession(
    val id: String,
    val title: String,
    val tutorName: String,
    val tutorId: String,
    val subject: String,
    val scheduledTime: String,
    val durationMinutes: Int = 60,
    val fee: Double = 2500.0,
    val attendeesCount: Int = 0,
    val roomCode: String = "slaty-live-room",
    val isLive: Boolean = false
)

data class Community(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val category: String,
    val unreadCount: Int = 0,
    val iconName: String = "ic_community",
    val subjects: List<String> = listOf("General"),
    val academicLevels: List<String> = listOf("Undergraduate", "JAMB/UTME")
)

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderRole: UserRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = false
)

data class PrivateConversation(
    val id: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserRole: UserRole,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0,
    val avatarRes: String = "avatar_3d_12"
)

data class CbtQuestion(
    val id: String,
    val subject: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

data class CbtQuizResult(
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val date: String
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val isRead: Boolean = false,
    val type: String = "general"
)

data class LiveRoomParticipant(
    val userId: String,
    val name: String,
    val role: UserRole,
    val isHost: Boolean = false,
    val isHandRaised: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)

data class LiveClassMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: UserRole = UserRole.STUDENT,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isQuestion: Boolean = false
)

