package com.example.data

import android.content.Context
import com.example.data.repository.TaskSyncRepository
import com.example.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class SlatyRepository(private val context: Context) {

    private val repoScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val taskSyncRepository = TaskSyncRepository.create(context, repoScope)
    val liveClassRepository = com.example.data.repository.LiveClassRepository()
    val taskRepository = com.example.data.repository.TaskRepository(context).apply {
        repoScope.launch {
            seedInitialAcademicTasksIfEmpty()
        }
    }

    // Current User Session (backed by Firebase Authentication)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        try {
            val fUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (fUser != null) {
                repoScope.launch {
                    try {
                        val uid = fUser.uid
                        val email = fUser.email ?: ""
                        val displayName = fUser.displayName ?: email.substringBefore("@")
                        val fs = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val doc = fs.collection("users").document(uid).get().await()
                        if (doc.exists()) {
                            val roleStr = (doc.getString("role") ?: "student").lowercase()
                            val role = if (roleStr == "tutor") UserRole.TUTOR else UserRole.STUDENT
                            _currentUser.value = User(
                                id = uid,
                                name = doc.getString("name") ?: displayName,
                                username = doc.getString("username") ?: displayName.lowercase().replace(" ", "_"),
                                email = doc.getString("email") ?: email,
                                phone = doc.getString("phone") ?: "",
                                role = role,
                                specialization = doc.getString("specialization"),
                                bio = doc.getString("bio") ?: "",
                                hourlyRate = doc.getDouble("hourlyRate") ?: 3500.0,
                                rating = doc.getDouble("rating") ?: 4.9,
                                studentsCount = doc.getLong("studentsCount")?.toInt() ?: 142,
                                bankName = doc.getString("bankName") ?: "GTBank",
                                accountNumber = doc.getString("accountNumber") ?: "0123456789",
                                accountName = doc.getString("accountName") ?: displayName,
                                institution = doc.getString("institution") ?: "University of Lagos",
                                faculty = doc.getString("faculty") ?: "Science",
                                department = doc.getString("department") ?: "Computer Science",
                                academicLevel = doc.getString("academicLevel") ?: "Undergraduate"
                            )
                        } else {
                            _currentUser.value = User(
                                id = uid,
                                name = displayName,
                                username = displayName.lowercase().replace(" ", "_"),
                                email = email,
                                role = UserRole.STUDENT
                            )
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("SlatyRepo", "Could not fetch remote profile on boot: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("SlatyRepo", "Firebase not yet initialized on repo creation: ${e.message}")
        }

        // Real-time synchronization of live video classes across devices
        repoScope.launch {
            try {
                liveClassRepository.observeLiveClasses().collect { remoteList ->
                    if (remoteList.isNotEmpty()) {
                        val currentList = _liveClasses.value
                        val merged = (remoteList + currentList).distinctBy { it.roomCode }
                        _liveClasses.value = merged
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("SlatyRepo", "Live classes sync error: ${e.message}")
            }
        }
    }

    // Tutors List
    private val _tutors = MutableStateFlow<List<Tutor>>(
        listOf(
            Tutor(
                id = "tutor_1",
                name = "Dr. Emmanuel Okon",
                username = "emmanuel_okon",
                specialization = "Senior Lecturer, Physics & Engineering",
                rating = 4.95,
                reviewsCount = 128,
                hourlyRate = 4500.0,
                bio = "Ph.D. in Applied Physics with 8+ years preparing university students for exams and complex mechanics problems.",
                subjects = listOf("Physics", "Thermodynamics", "Electromagnetism", "Mathematics"),
                avatarResName = "avatar_3d_12",
                academicLevels = listOf("Undergraduate", "Postgraduate", "JAMB/UTME")
            ),
            Tutor(
                id = "tutor_2",
                name = "Amina Bello, M.Sc.",
                username = "amina_chem",
                specialization = "Organic Chemistry Specialist",
                rating = 4.88,
                reviewsCount = 94,
                hourlyRate = 3800.0,
                bio = "Specialized in General Chemistry, Organic synthesis, and JAMB/WAEC advanced preparation.",
                subjects = listOf("Chemistry", "Organic Chemistry", "Biology"),
                avatarResName = "avatar_3d_16",
                academicLevels = listOf("Secondary (WAEC/NECO)", "JAMB/UTME", "Undergraduate")
            ),
            Tutor(
                id = "tutor_3",
                name = "Chukwudi Nnamdi",
                username = "chuks_maths",
                specialization = "Pure & Applied Mathematics",
                rating = 4.92,
                reviewsCount = 210,
                hourlyRate = 4000.0,
                bio = "First class mathematics graduate with a passion for simplifying Differential Equations, Linear Algebra, and Calculus.",
                subjects = listOf("Mathematics", "Calculus", "Statistics"),
                avatarResName = "avatar_3d_19",
                academicLevels = listOf("JAMB/UTME", "Undergraduate")
            ),
            Tutor(
                id = "tutor_4",
                name = "Grace Eyo",
                username = "grace_biology",
                specialization = "Cellular Biology & Genetics",
                rating = 4.85,
                reviewsCount = 76,
                hourlyRate = 3500.0,
                bio = "Passionate tutor dedicated to helping pre-med and science students ace their courses with clear visual analogies.",
                subjects = listOf("Biology", "Genetics", "Physiology"),
                avatarResName = "avatar_3d_21",
                academicLevels = listOf("Secondary (WAEC/NECO)", "JAMB/UTME", "Undergraduate")
            ),
            Tutor(
                id = "tutor_5",
                name = "Babatunde Alabi",
                username = "tunde_econ",
                specialization = "Economics & Financial Mathematics",
                rating = 4.90,
                reviewsCount = 115,
                hourlyRate = 4200.0,
                bio = "Economist with extensive coaching background in Macroeconomics, Microeconomics, and Econometrics.",
                subjects = listOf("Economics", "Microeconomics", "Business Math"),
                avatarResName = "avatar_monogram",
                academicLevels = listOf("Undergraduate", "Postgraduate")
            )
        )
    )
    val tutors: StateFlow<List<Tutor>> = _tutors.asStateFlow()

    // Tasks List backed by Room-to-Firestore TaskSyncRepository
    val tasks: StateFlow<List<TaskItem>> = taskSyncRepository.allTasks.stateIn(
        scope = repoScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Live Classes
    private val _liveClasses = MutableStateFlow<List<LiveClassSession>>(
        listOf(
            LiveClassSession(
                id = "class_1",
                title = "Mastering Differential Equations for Engineering",
                tutorName = "Dr. Emmanuel Okon",
                tutorId = "tutor_1",
                subject = "Mathematics",
                scheduledTime = "Today, 7:00 PM",
                fee = 3500.0,
                attendeesCount = 18,
                roomCode = "slaty-diff-eq-live",
                isLive = true
            ),
            LiveClassSession(
                id = "class_2",
                title = "JAMB Chemistry High-Yield Exam Masterclass",
                tutorName = "Amina Bello, M.Sc.",
                tutorId = "tutor_2",
                subject = "Chemistry",
                scheduledTime = "Tomorrow, 4:00 PM",
                fee = 2000.0,
                attendeesCount = 34,
                roomCode = "slaty-jamb-chem-class",
                isLive = false
            ),
            LiveClassSession(
                id = "class_3",
                title = "Biology Cell Structure & Human Physiology",
                tutorName = "Grace Eyo",
                tutorId = "tutor_4",
                subject = "Biology",
                scheduledTime = "Saturday, 11:00 AM",
                fee = 2500.0,
                attendeesCount = 22,
                roomCode = "slaty-bio-physiology",
                isLive = false
            )
        )
    )
    val liveClasses: StateFlow<List<LiveClassSession>> = _liveClasses.asStateFlow()

    // Communities
    private val _communities = MutableStateFlow<List<Community>>(
        listOf(
            Community(
                id = "comm_1",
                name = "JAMB & UTME Master Prep",
                description = "Discussions, past questions analysis, and daily tips for top scores in Nigeria.",
                memberCount = 1420,
                category = "Exam Prep",
                unreadCount = 4,
                subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology"),
                academicLevels = listOf("JAMB/UTME")
            ),
            Community(
                id = "comm_2",
                name = "Engineering & Physical Sciences",
                description = "Mechanics, Circuits, Differential Equations, and MATLAB study groups for undergraduates.",
                memberCount = 890,
                category = "Higher Ed",
                unreadCount = 2,
                subjects = listOf("Engineering", "Physics", "Mathematics"),
                academicLevels = listOf("Undergraduate", "Postgraduate")
            ),
            Community(
                id = "comm_3",
                name = "Pre-Med & Biological Sciences",
                description = "Anatomy, Biochemistry, Physiology, and MBBS entry discussions.",
                memberCount = 720,
                category = "Medical",
                unreadCount = 0,
                subjects = listOf("Biology", "Chemistry"),
                academicLevels = listOf("JAMB/UTME", "Undergraduate")
            ),
            Community(
                id = "comm_4",
                name = "WAEC & NECO Senior Scholars",
                description = "Targeted practice for Senior Secondary examinations and syllabus coverage.",
                memberCount = 1150,
                category = "Secondary",
                unreadCount = 7,
                subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "Economics"),
                academicLevels = listOf("Secondary (WAEC/NECO)")
            ),
            Community(
                id = "comm_5",
                name = "General Student Lounge",
                description = "Connect, share study tips, ask peer questions, and find study buddies.",
                memberCount = 2300,
                category = "Social",
                unreadCount = 1,
                subjects = listOf("General", "Economics", "Mathematics"),
                academicLevels = listOf("Secondary (WAEC/NECO)", "JAMB/UTME", "Undergraduate", "Postgraduate")
            )
        )
    )
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    // Private Chats
    private val _privateChats = MutableStateFlow<List<PrivateConversation>>(
        listOf(
            PrivateConversation(
                id = "chat_tutor_1",
                otherUserId = "tutor_1",
                otherUserName = "Dr. Emmanuel Okon",
                otherUserRole = UserRole.TUTOR,
                lastMessage = "I've uploaded the worksheet for our thermodynamics class.",
                lastMessageTime = "10:14 AM",
                unreadCount = 1,
                avatarRes = "avatar_3d_12"
            ),
            PrivateConversation(
                id = "chat_tutor_2",
                otherUserId = "tutor_2",
                otherUserName = "Amina Bello, M.Sc.",
                otherUserRole = UserRole.TUTOR,
                lastMessage = "Let me know when you review the organic chemistry notes.",
                lastMessageTime = "Yesterday",
                unreadCount = 0,
                avatarRes = "avatar_3d_16"
            ),
            PrivateConversation(
                id = "chat_tutor_3",
                otherUserId = "tutor_3",
                otherUserName = "Chukwudi Nnamdi",
                otherUserRole = UserRole.TUTOR,
                lastMessage = "Great job on the vector calculus drill!",
                lastMessageTime = "2 days ago",
                unreadCount = 0,
                avatarRes = "avatar_3d_19"
            )
        )
    )
    val privateChats: StateFlow<List<PrivateConversation>> = _privateChats.asStateFlow()

    // Community Messages map (communityId -> list of messages)
    private val _communityMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "comm_1" to listOf(
                ChatMessage("m1", "usr_1", "Tunde Balogun", UserRole.STUDENT, "Has anyone solved question 14 on the 2024 Physics paper?"),
                ChatMessage("m2", "tutor_1", "Dr. Emmanuel Okon", UserRole.TUTOR, "Remember to use conservation of linear momentum before considering thermal dissipation."),
                ChatMessage("m3", "usr_2", "Adaeze Chukwu", UserRole.STUDENT, "Thank you Dr. Okon! That cleared up the discrepancy in my formula.")
            ),
            "comm_2" to listOf(
                ChatMessage("m4", "usr_3", "Ibrahim Sani", UserRole.STUDENT, "Anyone working on Laplace transform problems for circuit analysis?"),
                ChatMessage("m5", "tutor_3", "Chukwudi Nnamdi", UserRole.TUTOR, "I can host a quick 20-minute live session later this evening if interested!")
            )
        )
    )

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem("n1", "Class Starting Soon", "Mastering Differential Equations begins in 15 minutes.", "15m ago", false, "live"),
            NotificationItem("n2", "Task Update", "Your Thermodynamics problem has been accepted by Dr. Emmanuel Okon.", "2h ago", false, "task"),
            NotificationItem("n3", "New Message", "Dr. Emmanuel Okon sent you a new resource in private chat.", "3h ago", true, "message"),
            NotificationItem("n4", "CBT High Score!", "You scored 85% on JAMB Biology Practice Drill.", "1d ago", true, "cbt")
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // CBT Questions cache
    private val _biologyQuestions = mutableListOf<CbtQuestion>()
    private val _mathQuestions = mutableListOf<CbtQuestion>()

    init {
        loadQuestionsFromAssets()
    }

    private fun loadQuestionsFromAssets() {
        try {
            // Load Biology
            context.assets.open("questions/Biology.json").bufferedReader().use { reader ->
                val jsonStr = reader.readText()
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until minOf(jsonArray.length(), 40)) {
                    val obj = jsonArray.getJSONObject(i)
                    val qText = obj.getString("question")
                    val optObj = obj.getJSONObject("options")
                    val options = mutableListOf<String>()
                    val keys = listOf("option1", "option2", "option3", "option4", "option5")
                    for (k in keys) {
                        if (optObj.has(k)) {
                            options.add(optObj.getString(k))
                        }
                    }
                    val correctKey = obj.optString("correctOption", "option1")
                    val correctIdx = when (correctKey) {
                        "option1" -> 0
                        "option2" -> 1
                        "option3" -> 2
                        "option4" -> 3
                        "option5" -> 4
                        else -> 0
                    }
                    _biologyQuestions.add(
                        CbtQuestion(
                            id = "bio_$i",
                            subject = "Biology",
                            question = qText,
                            options = options,
                            correctIndex = correctIdx
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Provide fallback sample questions
            _biologyQuestions.addAll(getFallbackBiologyQuestions())
        }

        try {
            // Load Math
            context.assets.open("questions/math5000.json").bufferedReader().use { reader ->
                val jsonStr = reader.readText()
                val jsonArray = JSONArray(jsonStr)
                for (i in 0 until minOf(jsonArray.length(), 40)) {
                    val obj = jsonArray.getJSONObject(i)
                    val qText = obj.getString("question")
                    val optObj = obj.getJSONObject("options")
                    val options = mutableListOf<String>()
                    val keys = listOf("option1", "option2", "option3", "option4", "option5")
                    for (k in keys) {
                        if (optObj.has(k)) {
                            options.add(optObj.getString(k))
                        }
                    }
                    val correctKey = obj.optString("correctOption", "option1")
                    val correctIdx = when (correctKey) {
                        "option1" -> 0
                        "option2" -> 1
                        "option3" -> 2
                        "option4" -> 3
                        "option5" -> 4
                        else -> 0
                    }
                    _mathQuestions.add(
                        CbtQuestion(
                            id = "math_$i",
                            subject = "Mathematics",
                            question = qText,
                            options = options,
                            correctIndex = correctIdx
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _mathQuestions.addAll(getFallbackMathQuestions())
        }
    }

    fun getQuestionsForSubject(subject: String): List<CbtQuestion> {
        return if (subject.equals("Mathematics", ignoreCase = true)) {
            if (_mathQuestions.isNotEmpty()) _mathQuestions else getFallbackMathQuestions()
        } else {
            if (_biologyQuestions.isNotEmpty()) _biologyQuestions else getFallbackBiologyQuestions()
        }
    }

    private fun getFallbackBiologyQuestions(): List<CbtQuestion> = listOf(
        CbtQuestion("b1", "Biology", "What type of fruit is formed from a single flower having several free carpels?", listOf("Multiple fruit", "Simple fruit", "Aggregate fruit", "Dehiscent fruit", "Indehiscent fruit"), 2),
        CbtQuestion("b2", "Biology", "Which organelle is considered the powerhouse of the cell?", listOf("Ribosome", "Endoplasmic Reticulum", "Mitochondria", "Golgi apparatus", "Nucleolus"), 2),
        CbtQuestion("b3", "Biology", "The process by which plants lose water vapor through stomata is called:", listOf("Photosynthesis", "Respiration", "Transpiration", "Osmosis", "Gutting"), 2),
        CbtQuestion("b4", "Biology", "Which of the following blood groups is known as the universal donor?", listOf("Group A", "Group B", "Group AB", "Group O", "Group Rh-only"), 3),
        CbtQuestion("b5", "Biology", "Which of the following diseases is NOT caused by a virus?", listOf("Rinderpest", "Maize rust", "Newcastle disease", "Swine fever", "Cassava mosaic"), 1)
    )

    private fun getFallbackMathQuestions(): List<CbtQuestion> = listOf(
        CbtQuestion("m1", "Mathematics", "Evaluate the integral of (3x^2 + 2x) dx from 0 to 2.", listOf("12", "16", "8", "10", "14"), 0),
        CbtQuestion("m2", "Mathematics", "If log10(x) = 3, what is the value of x?", listOf("30", "100", "1000", "10", "300"), 2),
        CbtQuestion("m3", "Mathematics", "Find the derivative of f(x) = sin(2x) with respect to x.", listOf("cos(2x)", "2 cos(2x)", "-2 cos(2x)", "-cos(2x)", "2 sin(x)"), 1),
        CbtQuestion("m4", "Mathematics", "The sum of the interior angles of a regular hexagon is:", listOf("540 degrees", "720 degrees", "360 degrees", "900 degrees", "1080 degrees"), 1),
        CbtQuestion("m5", "Mathematics", "Solve for x: 2^(x+1) = 32.", listOf("3", "4", "5", "6", "2"), 1)
    )

    // User Operations
    fun switchUserRole(newRole: UserRole) {
        val current = _currentUser.value ?: return
        if (newRole == UserRole.TUTOR) {
            _currentUser.value = current.copy(
                role = UserRole.TUTOR,
                name = if (current.name.startsWith("Dr.") || current.name.contains("Tutor")) current.name else "Dr. ${current.name}",
                specialization = "Senior Educator & Academic Consultant",
                hourlyRate = 4500.0
            )
        } else {
            _currentUser.value = current.copy(
                role = UserRole.STUDENT,
                name = current.name.removePrefix("Dr. ")
            )
        }
    }

    fun updateProfile(name: String, email: String, phone: String, specialization: String? = null) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            name = name,
            email = email,
            phone = phone,
            specialization = specialization ?: current.specialization
        )
    }

    fun updatePayoutDetails(bankName: String, accountNumber: String, accountName: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(
            bankName = bankName,
            accountNumber = accountNumber,
            accountName = accountName
        )
    }

    // Task Operations backed by Room-to-Firestore TaskSyncRepository
    fun createTask(
        title: String,
        courseCode: String,
        institution: String,
        faculty: String,
        department: String,
        description: String,
        amount: Double,
        deadline: String,
        learnerType: String
    ): TaskItem {
        val user = _currentUser.value
        val newTask = TaskItem(
            id = "task_${System.currentTimeMillis()}",
            title = title,
            studentName = user?.name ?: "Student",
            studentId = user?.id ?: "usr_temp",
            institution = institution,
            courseCode = courseCode,
            faculty = faculty,
            department = department,
            description = description,
            amount = amount,
            deadline = deadline,
            learnerType = learnerType,
            status = TaskStatus.PENDING
        )
        repoScope.launch {
            taskSyncRepository.createTask(newTask)
        }
        return newTask
    }

    fun acceptTask(taskId: String, tutorName: String, tutorId: String): Boolean {
        repoScope.launch {
            taskSyncRepository.acceptTask(taskId, tutorName, tutorId)
        }
        return true
    }

    fun completeTask(taskId: String): Boolean {
        repoScope.launch {
            taskSyncRepository.completeTask(taskId)
        }
        return true
    }

    // Live Class Operations
    fun createLiveClass(title: String, fee: Double, subject: String = "General"): LiveClassSession {
        val user = _currentUser.value
        val code = "slaty-${title.trim().lowercase(Locale.ROOT).replace("\\s+".toRegex(), "-")}"
        val newClass = LiveClassSession(
            id = "class_${System.currentTimeMillis()}",
            title = title,
            tutorName = user?.name ?: "Tutor",
            tutorId = user?.id ?: "tutor_demo",
            subject = subject,
            scheduledTime = "Starting Now",
            fee = fee,
            attendeesCount = 1,
            roomCode = code,
            isLive = true
        )
        _liveClasses.value = listOf(newClass) + _liveClasses.value.filter { it.roomCode != code }
        repoScope.launch {
            liveClassRepository.publishLiveClass(newClass)
        }
        return newClass
    }

    // Chat Operations
    fun getCommunityMessages(communityId: String): List<ChatMessage> {
        return _communityMessages.value[communityId] ?: emptyList()
    }

    fun sendCommunityMessage(communityId: String, text: String) {
        val user = _currentUser.value ?: return
        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = user.id,
            senderName = user.name,
            senderRole = user.role,
            text = text,
            isFromMe = true
        )
        val currentMap = _communityMessages.value.toMutableMap()
        val list = (currentMap[communityId] ?: emptyList()).toMutableList()
        list.add(msg)
        currentMap[communityId] = list
        _communityMessages.value = currentMap
    }

    fun createCommunity(name: String, description: String, category: String) {
        val newComm = Community(
            id = "comm_${System.currentTimeMillis()}",
            name = name,
            description = description,
            memberCount = 1,
            category = category,
            unreadCount = 0
        )
        _communities.value = listOf(newComm) + _communities.value
    }

    fun logout() {
        _currentUser.value = null
    }

    fun setUser(user: User) {
        _currentUser.value = user
    }

    fun login(email: String, role: UserRole) {
        val displayName = if (email.contains("@")) {
            email.substringBefore("@").replace(".", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        } else {
            email
        }
        _currentUser.value = User(
            id = "user_${System.currentTimeMillis()}",
            name = if (role == UserRole.TUTOR) "Dr. $displayName" else displayName,
            username = displayName.lowercase(Locale.ROOT).replace(" ", "_"),
            email = email,
            role = role,
            specialization = if (role == UserRole.TUTOR) "Professional Instructor" else null,
            bankName = "Access Bank",
            accountNumber = "0123456789",
            accountName = displayName
        )
    }
}
