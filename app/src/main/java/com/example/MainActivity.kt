package com.example

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.UserRole
import com.example.ui.components.*
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.screens.onboarding.AnimatedOnboardingScreen
import com.example.ui.screens.profile.UserProfileScreen
import com.example.ui.screens.common.ChatScreen
import com.example.ui.screens.common.NotificationsScreen
import com.example.ui.screens.common.VideoCallScreen
import com.example.ui.screens.student.*
import com.example.ui.screens.student.MainDashboardScreen
import com.example.ui.screens.tutor.*
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.NotificationHelper
import com.example.viewmodel.*

import android.content.Intent
import com.example.service.SlatyFirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    private val viewModel: SlatyViewModel by viewModels()
    private val authViewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel for new messages and FCM updates
        NotificationHelper.createNotificationChannel(this)

        // Initialize Firebase Cloud Messaging token & topic subscriptions
        initFcm()

        // Handle any incoming notification clicks that launched the app from background
        handleFcmIntent(intent)

        setContent {
            MyApplicationTheme {
                SlatyApp(
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    activity = this
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleFcmIntent(intent)
    }

    @Suppress("DEPRECATION")
    private fun initFcm() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful) {
                        val token = task.result
                        if (!token.isNullOrBlank()) {
                            android.util.Log.i("MainActivity", "FCM Registration Token: $token")
                            SlatyFirebaseMessagingService.saveFcmToken(this, token)
                        }
                    }
                } catch (t: Throwable) {
                    android.util.Log.w("MainActivity", "Error in FCM token callback: ${t.message}")
                }
            }
            FirebaseMessaging.getInstance().subscribeToTopic("messages")
            FirebaseMessaging.getInstance().subscribeToTopic("academic_updates")
        } catch (t: Throwable) {
            android.util.Log.w("MainActivity", "FCM token registration unavailable: ${t.message}")
        }
    }

    private fun handleFcmIntent(intent: Intent?) {
        if (intent == null) return
        val isFromFcm = intent.getBooleanExtra(NotificationHelper.EXTRA_FROM_FCM, false) ||
                intent.hasExtra("google.message_id")

        if (isFromFcm) {
            val title = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TITLE)
                ?: intent.getStringExtra("title")
                ?: "Academic Message"
            val body = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_BODY)
                ?: intent.getStringExtra("body")
                ?: ""
            val type = intent.getStringExtra(NotificationHelper.EXTRA_NOTIFICATION_TYPE)
                ?: intent.getStringExtra("type")
                ?: "message"

            viewModel.showFeedback("Opened notification: $title")

            if (type == "community" || type == "message") {
                viewModel.navigateTo(SubScreen.Notifications)
            }
        }
    }
}

@Composable
fun SlatyApp(
    viewModel: SlatyViewModel,
    authViewModel: AuthenticationViewModel,
    activity: Activity
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val studentTab by viewModel.studentTab.collectAsState()
    val tutorTab by viewModel.tutorTab.collectAsState()
    val subScreen by viewModel.subScreen.collectAsState()
    val selectedTopicDialog by viewModel.selectedTopicDialog.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()

    val authUiState by authViewModel.uiState.collectAsState()

    val tutors by viewModel.filteredTutors.collectAsState()
    val searchQuery by viewModel.tutorSearchQuery.collectAsState()
    val selectedSubject by viewModel.selectedSubjectFilter.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val liveClasses by viewModel.liveClasses.collectAsState()
    val communities by viewModel.communities.collectAsState()
    val privateChats by viewModel.privateChats.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val academicTasks by viewModel.studentAcademicTasks.collectAsState()
    val cbtState by viewModel.cbtState.collectAsState()

    val homeSearchQuery by viewModel.homeSearchQuery.collectAsState()
    val homeSearchTarget by viewModel.homeSearchTarget.collectAsState()
    val homeSelectedSubject by viewModel.homeSelectedSubject.collectAsState()
    val homeSelectedLevel by viewModel.homeSelectedLevel.collectAsState()
    val homeSortOption by viewModel.homeSortOption.collectAsState()
    val filteredHomeTutors by viewModel.filteredHomeTutors.collectAsState()
    val filteredHomeCommunities by viewModel.filteredHomeCommunities.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateCommunityDialog by remember { mutableStateOf(false) }
    var showTutorAccessDialog by remember { mutableStateOf(false) }

    // Request notification permission on Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.showFeedback("Message notifications enabled")
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Sync AuthenticationViewModel with app state and manage Navigation transition
    LaunchedEffect(authUiState) {
        when (val state = authUiState) {
            is AuthUiState.Success -> {
                viewModel.loginWithFirebaseUser(state.user)
                authViewModel.resetState()
                try {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                } catch (e: Exception) {
                    // Ignore navigation state race conditions
                }
            }
            is AuthUiState.Error -> {
                viewModel.showFeedback(state.message)
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    // Auto-navigate to animated onboarding on logout
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            try {
                val currentRoute = navController.currentDestination?.route
                if (currentRoute != null && currentRoute != "onboarding" && !currentRoute.startsWith("login")) {
                    navController.navigate("onboarding") {
                        popUpTo("dashboard") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            } catch (e: Exception) {
                // Ignore navigation state race conditions
            }
        }
    }

    // Handle feedback snackbar
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // Android System Back Navigation Handler
    BackHandler(enabled = subScreen !is SubScreen.None) {
        viewModel.goBack()
    }

    // Topic Action Dialog Modal
    selectedTopicDialog?.let { topic ->
        TopicActionDialog(
            topic = topic,
            onDismiss = { viewModel.closeTopicModal() },
            onWantLiveClass = { t ->
                viewModel.closeTopicModal()
                viewModel.navigateTo(
                    SubScreen.LiveVideoRoom(
                        title = "$t Masterclass",
                        roomCode = "slaty-${t.lowercase().replace(" ", "-")}",
                        isHost = false
                    )
                )
            },
            onWantSolveTask = { t ->
                viewModel.closeTopicModal()
                viewModel.navigateTo(SubScreen.CreateTask(initialTopic = t))
            }
        )
    }

    // Create Community Dialog
    if (showCreateCommunityDialog) {
        CreateCommunityDialog(
            onDismiss = { showCreateCommunityDialog = false },
            onCreate = { name, desc, category ->
                viewModel.createNewCommunity(name, desc, category)
            }
        )
    }

    // Tutor Access Restriction Dialog
    if (showTutorAccessDialog) {
        AlertDialog(
            onDismissRequest = { showTutorAccessDialog = false },
            containerColor = SlatyDarkSurface,
            title = {
                Text(
                    text = "Tutor Dashboard Access",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "The Tutor Dashboard is strictly restricted to verified academic tutors. Only accounts authenticated as a tutor have access.\n\nWould you like to sign in with your tutor credentials?",
                    color = SlatyTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTutorAccessDialog = false
                        authViewModel.signOut()
                        viewModel.logout()
                        navController.navigate("login/TUTOR")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
                ) {
                    Text("Sign In as Tutor", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTutorAccessDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "dashboard" else "onboarding",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("onboarding") {
            AnimatedOnboardingScreen(
                onContinueAsStudent = { navController.navigate("login/STUDENT") },
                onContinueAsTutor = { navController.navigate("login/TUTOR") }
            )
        }

        composable("welcome") {
            AnimatedOnboardingScreen(
                onContinueAsStudent = { navController.navigate("login/STUDENT") },
                onContinueAsTutor = { navController.navigate("login/TUTOR") }
            )
        }

        composable(
            route = "login/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType; defaultValue = "STUDENT" })
        ) { backStackEntry ->
            val roleStr = backStackEntry.arguments?.getString("role") ?: "STUDENT"
            val targetRole = try { UserRole.valueOf(roleStr) } catch (e: Exception) { UserRole.STUDENT }

            LoginScreen(
                role = targetRole,
                uiState = authUiState,
                onBack = { navController.popBackStack() },
                onLogin = { email, pass, role ->
                    authViewModel.signInWithEmail(email, pass, role)
                },
                onRegister = { email, pass, name, role ->
                    authViewModel.signUpWithEmail(email, pass, name, role)
                },
                onForgotPassword = { email, onResult ->
                    authViewModel.sendPasswordReset(email, onResult)
                }
            )
        }

        composable("dashboard") {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (currentUser != null && subScreen is SubScreen.None) {
                        SlatyTopHeader(
                            user = currentUser,
                            onNotificationClick = { viewModel.navigateTo(SubScreen.Notifications) },
                            onProfileClick = { viewModel.navigateTo(SubScreen.UserProfile) },
                            onRoleSwitchClick = {
                                if (currentUser?.role == UserRole.STUDENT) {
                                    showTutorAccessDialog = true
                                } else {
                                    val nextRole = if (currentUser?.role == UserRole.STUDENT) UserRole.TUTOR else UserRole.STUDENT
                                    viewModel.switchRole(nextRole)
                                }
                            },
                            modifier = Modifier.statusBarsPadding()
                        )
                    }
                },
                bottomBar = {
                    if (currentUser != null && subScreen is SubScreen.None) {
                        if (currentUser?.role == UserRole.STUDENT) {
                            SlatyStudentBottomBar(
                                selectedTab = studentTab,
                                unreadConnectCount = 3,
                                onTabSelected = { viewModel.setStudentTab(it) }
                            )
                        } else {
                            SlatyTutorBottomBar(
                                selectedTab = tutorTab,
                                unreadConnectCount = 1,
                                onTabSelected = { viewModel.setTutorTab(it) }
                            )
                        }
                    }
                },
                containerColor = SlatyDarkBackground
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(SlatyDarkBackground)
                ) {
                    when {
                        // Sub-Screens
                        subScreen is SubScreen.TutorProfile -> {
                    val tutor = (subScreen as SubScreen.TutorProfile).tutor
                    TutorProfileDetailScreen(
                        tutor = tutor,
                        onBack = { viewModel.goBack() },
                        onReachTutor = { t -> viewModel.navigateTo(SubScreen.ReachTutor(t)) }
                    )
                }

                subScreen is SubScreen.ReachTutor -> {
                    val tutor = (subScreen as SubScreen.ReachTutor).tutor
                    ReachTutorScreen(
                        tutor = tutor,
                        onBack = { viewModel.goBack() },
                        onConfirmBooking = { t, _ ->
                            viewModel.showFeedback("Booking confirmed with ${t.name}!")
                            NotificationHelper.showNewMessageNotification(
                                context = context,
                                senderName = t.name,
                                messageText = "I've accepted your session booking! Let's connect in class.",
                                isCommunity = false
                            )
                            viewModel.navigateTo(
                                SubScreen.LiveVideoRoom(
                                    title = "1-on-1 Session with ${t.name}",
                                    roomCode = "slaty-session-${t.id}",
                                    isHost = false
                                )
                            )
                        }
                    )
                }

                subScreen is SubScreen.CreateTask -> {
                    val topic = (subScreen as SubScreen.CreateTask).initialTopic
                    TaskSubmissionScreen(
                        user = currentUser,
                        initialTopic = topic,
                        onBack = { viewModel.goBack() },
                        onSubmit = { title, course, school, faculty, dept, desc, amount, deadline, type ->
                            viewModel.submitTask(title, course, school, faculty, dept, desc, amount, deadline, type)
                            NotificationHelper.showNewMessageNotification(
                                context = context,
                                senderName = "Slaty Academic Board",
                                messageText = "Task for $course ($title) has been submitted for verified tutors.",
                                isCommunity = false
                            )
                        }
                    )
                }

                subScreen is SubScreen.CbtPractice -> {
                    CbtPracticeScreen(
                        quizState = cbtState,
                        onBack = { viewModel.goBack() },
                        onSelectAnswer = { qIdx, optIdx -> viewModel.selectCbtAnswer(qIdx, optIdx) },
                        onNavigateQuestion = { idx -> viewModel.setCbtQuestionIndex(idx) },
                        onSubmit = { viewModel.submitCbtQuiz() },
                        onRestart = { viewModel.restartCbtQuiz() },
                        onSwitchSubject = { nextSubject -> viewModel.startCbtQuiz(nextSubject) }
                    )
                }

                subScreen is SubScreen.CommunityChat -> {
                    val community = (subScreen as SubScreen.CommunityChat).community
                    val msgs = viewModel.repository.getCommunityMessages(community.id)
                    ChatScreen(
                        title = community.name,
                        subtitle = "${community.memberCount} members • ${community.category}",
                        messages = msgs,
                        onBack = { viewModel.goBack() },
                        onSendMessage = { text ->
                            viewModel.sendCommunityMsg(community.id, text)
                            NotificationHelper.showNewMessageNotification(
                                context = context,
                                senderName = community.name,
                                messageText = "${currentUser?.name ?: "Learner"}: $text",
                                isCommunity = true
                            )
                        }
                    )
                }

                subScreen is SubScreen.PrivateChat -> {
                    val conv = (subScreen as SubScreen.PrivateChat).conversation
                    val msgs = listOf(
                        com.example.model.ChatMessage("pm1", conv.otherUserId, conv.otherUserName, conv.otherUserRole, conv.lastMessage, isFromMe = false)
                    )
                    ChatScreen(
                        title = conv.otherUserName,
                        subtitle = "Verified Tutor • Online",
                        messages = msgs,
                        onBack = { viewModel.goBack() },
                        onSendMessage = { text ->
                            viewModel.showFeedback("Message sent to ${conv.otherUserName}")
                            NotificationHelper.showNewMessageNotification(
                                context = context,
                                senderName = conv.otherUserName,
                                messageText = "Thanks! I've received: $text",
                                isCommunity = false
                            )
                        }
                    )
                }

                subScreen is SubScreen.LiveVideoRoom -> {
                    val room = subScreen as SubScreen.LiveVideoRoom
                    VideoCallScreen(
                        title = room.title,
                        roomCode = room.roomCode,
                        isHost = room.isHost,
                        currentUser = currentUser,
                        liveClassRepo = viewModel.repository.liveClassRepository,
                        onLeaveCall = { viewModel.goBack() }
                    )
                }

                subScreen is SubScreen.Notifications -> {
                    NotificationsScreen(
                        notifications = notifications,
                        onBack = { viewModel.goBack() }
                    )
                }

                subScreen is SubScreen.UserProfile -> {
                    UserProfileScreen(
                        user = currentUser,
                        onBack = { viewModel.goBack() },
                        onLogout = {
                            authViewModel.signOut()
                            viewModel.logout()
                            try {
                                navController.navigate("onboarding") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            } catch (e: Exception) {
                                // ignore
                            }
                        },
                        onProfileSaved = { updatedUser ->
                            viewModel.updateUser(updatedUser)
                        }
                    )
                }

                // Main Student Flow
                currentUser?.role == UserRole.STUDENT -> {
                    when (studentTab) {
                        StudentTab.HOME -> {
                            MainDashboardScreen(
                                user = currentUser,
                                liveClasses = liveClasses,
                                communities = communities,
                                onJoinLiveClass = { code, title ->
                                    viewModel.navigateTo(SubScreen.LiveVideoRoom(title, code, false))
                                },
                                onOpenStudyGroup = { comm ->
                                    viewModel.navigateTo(SubScreen.CommunityChat(comm))
                                },
                                onAskQuestionClick = {
                                    viewModel.navigateTo(SubScreen.CreateTask())
                                },
                                onStartCbtClick = { subject ->
                                    viewModel.startCbtQuiz(subject)
                                },
                                onNotificationsClick = {
                                    viewModel.navigateTo(SubScreen.Notifications)
                                },
                                onRoleSwitchClick = {
                                    showTutorAccessDialog = true
                                },
                                onViewAllClasses = {
                                    viewModel.setStudentTab(StudentTab.LIVE_CLASSES)
                                },
                                onViewAllGroups = {
                                    viewModel.setStudentTab(StudentTab.CONNECT)
                                },
                                onCreateStudyGroupClick = {
                                    showCreateCommunityDialog = true
                                },
                                academicTasks = academicTasks,
                                onToggleAcademicTask = { id, completed ->
                                    viewModel.toggleAcademicTask(id, completed)
                                },
                                onUpdateAcademicTaskProgress = { id, progress ->
                                    viewModel.updateAcademicTaskProgress(id, progress)
                                },
                                onCreateAcademicTask = { title, desc, type, subject, code, dueDate, priority, targetScore ->
                                    if (type == com.example.data.local.StudentTaskType.ASSIGNMENT) {
                                        viewModel.createAcademicAssignment(title, desc, subject, code, dueDate, priority)
                                    } else {
                                        viewModel.createTestPrepTask(title, desc, subject, code, dueDate, targetScore, priority)
                                    }
                                    viewModel.showFeedback("Task added to local database!")
                                },
                                onDeleteAcademicTask = { id ->
                                    viewModel.deleteAcademicTask(id)
                                    viewModel.showFeedback("Task deleted")
                                }
                            )
                        }
                        StudentTab.FIND_TUTOR -> {
                            FindTutorScreen(
                                tutors = tutors,
                                searchQuery = searchQuery,
                                selectedSubject = selectedSubject,
                                onSearchChange = { viewModel.setTutorSearch(it) },
                                onSubjectSelect = { viewModel.setSubjectFilter(it) },
                                onTutorClick = { tutor -> viewModel.navigateTo(SubScreen.TutorProfile(tutor)) },
                                onBookClick = { tutor -> viewModel.navigateTo(SubScreen.ReachTutor(tutor)) }
                            )
                        }
                        StudentTab.LIVE_CLASSES -> {
                            StudentLiveClassesScreen(
                                classes = liveClasses,
                                onJoinClass = { roomCode, title ->
                                    viewModel.navigateTo(
                                        SubScreen.LiveVideoRoom(
                                            title = title,
                                            roomCode = roomCode,
                                            isHost = false
                                        )
                                    )
                                }
                            )
                        }
                        StudentTab.CONNECT -> {
                            ConnectScreen(
                                communities = communities,
                                privateChats = privateChats,
                                onCommunityClick = { comm -> viewModel.navigateTo(SubScreen.CommunityChat(comm)) },
                                onPrivateChatClick = { chat -> viewModel.navigateTo(SubScreen.PrivateChat(chat)) },
                                onCreateCommunityClick = { showCreateCommunityDialog = true }
                            )
                        }
                        StudentTab.PERSONAL -> {
                            UserProfileScreen(
                                user = currentUser,
                                onBack = null,
                                onLogout = {
                                    authViewModel.signOut()
                                    viewModel.logout()
                                    try {
                                        navController.navigate("onboarding") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                },
                                onProfileSaved = { updatedUser ->
                                    viewModel.updateUser(updatedUser)
                                }
                            )
                        }
                    }
                }

                // Main Tutor Flow
                currentUser?.role == UserRole.TUTOR -> {
                    when (tutorTab) {
                        TutorTab.HOME -> {
                            TutorDashboardScreen(
                                user = currentUser,
                                tasks = tasks,
                                onCompleteTask = { taskId -> viewModel.completeTaskByTutor(taskId) },
                                onFindTopicsClick = { viewModel.setTutorTab(TutorTab.FIND_TOPICS) },
                                onHostClassClick = { viewModel.setTutorTab(TutorTab.HOST_CLASS) }
                            )
                        }
                        TutorTab.FIND_TOPICS -> {
                            TutorFindTopicsScreen(
                                tasks = tasks,
                                onAcceptTask = { task -> viewModel.acceptTaskByTutor(task) }
                            )
                        }
                        TutorTab.HOST_CLASS -> {
                            TutorHostClassScreen(
                                onStartClass = { title, fee, subj ->
                                    viewModel.hostNewLiveClass(title, fee, subj)
                                }
                            )
                        }
                        TutorTab.CONNECT -> {
                            ConnectScreen(
                                communities = communities,
                                privateChats = privateChats,
                                onCommunityClick = { comm -> viewModel.navigateTo(SubScreen.CommunityChat(comm)) },
                                onPrivateChatClick = { chat -> viewModel.navigateTo(SubScreen.PrivateChat(chat)) },
                                onCreateCommunityClick = { showCreateCommunityDialog = true }
                            )
                        }
                        TutorTab.PAYOUTS -> {
                            TutorPayoutsScreen(
                                user = currentUser,
                                tasks = tasks,
                                onSavePayoutMethod = { bank, accNum, accName ->
                                    viewModel.updatePayoutMethod(bank, accNum, accName)
                                },
                                onSwitchRole = { role -> viewModel.switchRole(role) },
                                onLogout = {
                                    authViewModel.signOut()
                                    viewModel.logout()
                                    try {
                                        navController.navigate("onboarding") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        // ignore
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
    }
}
