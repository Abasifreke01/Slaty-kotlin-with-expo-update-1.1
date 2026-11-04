package com.example.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Community
import com.example.model.LiveClassSession
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Main Dashboard Screen displaying:
 * - Upcoming and currently ongoing Live Classes
 * - Active Study Groups / Communities with live discussion indicators
 * - Quick academic action shortcuts (CBT, Ask a Tutor, Join Live Session)
 */
@Composable
fun MainDashboardScreen(
    user: User?,
    liveClasses: List<LiveClassSession>,
    communities: List<Community>,
    onJoinLiveClass: (roomCode: String, title: String) -> Unit,
    onOpenStudyGroup: (Community) -> Unit,
    onAskQuestionClick: () -> Unit,
    onStartCbtClick: (subject: String) -> Unit,
    onNotificationsClick: () -> Unit,
    onRoleSwitchClick: () -> Unit,
    onViewAllClasses: () -> Unit = {},
    onViewAllGroups: () -> Unit = {},
    onCreateStudyGroupClick: () -> Unit = {},
    academicTasks: List<com.example.data.local.StudentTaskEntity> = emptyList(),
    onToggleAcademicTask: (id: String, isCompleted: Boolean) -> Unit = { _, _ -> },
    onUpdateAcademicTaskProgress: (id: String, progress: Int) -> Unit = { _, _ -> },
    onCreateAcademicTask: (
        title: String,
        description: String,
        type: com.example.data.local.StudentTaskType,
        subject: String,
        courseCode: String,
        dueDate: String,
        priority: com.example.data.local.StudentTaskPriority,
        targetScore: Int
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onDeleteAcademicTask: (id: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedClassSubjectFilter by remember { mutableStateOf<String?>(null) }
    var selectedGroupCategoryFilter by remember { mutableStateOf<String?>(null) }

    val classSubjects = remember(liveClasses) {
        listOf("All") + liveClasses.map { it.subject }.distinct()
    }

    val groupCategories = remember(communities) {
        listOf("All") + communities.map { it.category }.distinct()
    }

    val filteredClasses = remember(liveClasses, selectedClassSubjectFilter) {
        if (selectedClassSubjectFilter == null || selectedClassSubjectFilter == "All") {
            liveClasses
        } else {
            liveClasses.filter { it.subject.equals(selectedClassSubjectFilter, ignoreCase = true) }
        }
    }

    val filteredCommunities = remember(communities, selectedGroupCategoryFilter) {
        if (selectedGroupCategoryFilter == null || selectedGroupCategoryFilter == "All") {
            communities
        } else {
            communities.filter { it.category.equals(selectedGroupCategoryFilter, ignoreCase = true) }
        }
    }

    // Pulsing animation for the Live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .testTag("main_dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Welcome Header & Quick Stats Card
        item {
            DashboardWelcomeCard(
                user = user,
                liveClassesCount = liveClasses.size,
                groupsCount = communities.size,
                onRoleSwitchClick = onRoleSwitchClick,
                onNotificationsClick = onNotificationsClick
            )
        }

        // Quick Actions Row
        item {
            DashboardQuickActionsRow(
                onJoinLive = {
                    val ongoing = liveClasses.firstOrNull { it.isLive } ?: liveClasses.firstOrNull()
                    if (ongoing != null) {
                        onJoinLiveClass(ongoing.roomCode, ongoing.title)
                    } else {
                        onViewAllClasses()
                    }
                },
                onOpenGroups = onViewAllGroups,
                onAskQuestion = onAskQuestionClick,
                onStartCbt = { onStartCbtClick("Biology") }
            )
        }

        // Upcoming Live Classes Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Section Title & View All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF4D4D).copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upcoming Live Classes",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = onViewAllClasses,
                        modifier = Modifier.testTag("dashboard_view_all_classes_button")
                    ) {
                        Text(
                            text = "View All (${liveClasses.size})",
                            color = SlatyMint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SlatyMint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Filter chips for Live Classes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    classSubjects.forEach { subject ->
                        val isSelected = (selectedClassSubjectFilter == null && subject == "All") ||
                                selectedClassSubjectFilter == subject
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedClassSubjectFilter = if (subject == "All") null else subject
                            },
                            label = { Text(subject, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SlatyPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SlatyDarkSurface,
                                labelColor = SlatyTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) SlatyMint else Color(0xFF2C273E)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }

        // List of Upcoming Live Classes
        if (filteredClasses.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "No live classes matching this filter.",
                    actionLabel = "Reset filter",
                    onAction = { selectedClassSubjectFilter = null }
                )
            }
        } else {
            items(filteredClasses, key = { it.id }) { liveSession ->
                LiveClassCard(
                    session = liveSession,
                    pulseAlpha = pulseAlpha,
                    onJoin = { onJoinLiveClass(liveSession.roomCode, liveSession.title) }
                )
            }
        }

        // Student Assignments & Test Preparation (Room Database Local Tasks)
        item {
            AcademicTasksSection(
                tasks = academicTasks,
                onToggleTask = onToggleAcademicTask,
                onUpdateProgress = onUpdateAcademicTaskProgress,
                onCreateTask = onCreateAcademicTask,
                onDeleteTask = onDeleteAcademicTask
            )
        }

        // Active Study Groups Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Section Title & Create Group
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = SlatyLavender,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Active Study Groups",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = onCreateStudyGroupClick,
                            modifier = Modifier.testTag("dashboard_create_group_button")
                        ) {
                            Text(
                                text = "+ Create",
                                color = SlatyLavender,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(
                            onClick = onViewAllGroups,
                            modifier = Modifier.testTag("dashboard_view_all_groups_button")
                        ) {
                            Text(
                                text = "View All",
                                color = SlatyMint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Filter chips for Study Groups
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupCategories.forEach { category ->
                        val isSelected = (selectedGroupCategoryFilter == null && category == "All") ||
                                selectedGroupCategoryFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedGroupCategoryFilter = if (category == "All") null else category
                            },
                            label = { Text(category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3F3563),
                                selectedLabelColor = Color.White,
                                containerColor = SlatyDarkSurface,
                                labelColor = SlatyTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) SlatyLavender else Color(0xFF2C273E)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }

        // List of Active Study Groups
        if (filteredCommunities.isEmpty()) {
            item {
                EmptyStateCard(
                    message = "No study groups found in this category.",
                    actionLabel = "Reset category",
                    onAction = { selectedGroupCategoryFilter = null }
                )
            }
        } else {
            items(filteredCommunities, key = { it.id }) { community ->
                StudyGroupCard(
                    community = community,
                    onOpen = { onOpenStudyGroup(community) }
                )
            }
        }

        // CBT Practice Promo Banner
        item {
            CbtPromoCard(
                onStartQuiz = { onStartCbtClick("Biology") }
            )
        }

        // Bottom spacing for navigation bar
        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun DashboardWelcomeCard(
    user: User?,
    liveClassesCount: Int,
    groupsCount: Int,
    onRoleSwitchClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val userName = user?.name ?: "Student"
    val userRole = user?.role ?: UserRole.STUDENT

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1E1B2C),
        border = BorderStroke(1.dp, Color(0xFF312A47)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_welcome_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF231E35),
                            Color(0xFF171424)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (userRole == UserRole.STUDENT) SlatyPrimary else Color(0xFF4C3B78)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Welcome back,",
                                color = SlatyTextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (userRole == UserRole.STUDENT) Color(0x331DE9B6) else Color(0x33AB9DFB)
                            ) {
                                Text(
                                    text = userRole.displayName,
                                    color = if (userRole == UserRole.STUDENT) SlatyMint else SlatyLavender,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = userName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Role Switch Button
                    IconButton(
                        onClick = onRoleSwitchClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF29243C))
                            .testTag("dashboard_role_switch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Switch Role",
                            tint = SlatyMint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notification Button
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF29243C))
                            .testTag("dashboard_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFFFF5252),
                                    contentColor = Color.White
                                ) {
                                    Text("3", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Counters Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF14121F))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = "Live Classes",
                    count = liveClassesCount.toString(),
                    highlightColor = SlatyMint
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFF2B263F))
                )

                StatItem(
                    label = "Study Groups",
                    count = groupsCount.toString(),
                    highlightColor = SlatyLavender
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFF2B263F))
                )

                StatItem(
                    label = "CBT Quizzes",
                    count = "120+",
                    highlightColor = SlatyAccentYellow
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: String, highlightColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = highlightColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = SlatyTextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun DashboardQuickActionsRow(
    onJoinLive: () -> Unit,
    onOpenGroups: () -> Unit,
    onAskQuestion: () -> Unit,
    onStartCbt: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            label = "Join Live",
            icon = Icons.Default.Videocam,
            iconTint = SlatyMint,
            containerColor = Color(0xFF192A28),
            borderColor = Color(0x331DE9B6),
            onClick = onJoinLive,
            modifier = Modifier.weight(1f).testTag("quick_action_join_live")
        )

        QuickActionButton(
            label = "Groups",
            icon = Icons.Default.Groups,
            iconTint = SlatyLavender,
            containerColor = Color(0xFF27213F),
            borderColor = Color(0x33AB9DFB),
            onClick = onOpenGroups,
            modifier = Modifier.weight(1f).testTag("quick_action_groups")
        )

        QuickActionButton(
            label = "Ask Tutor",
            icon = Icons.AutoMirrored.Filled.Assignment,
            iconTint = SlatyAccentYellow,
            containerColor = Color(0xFF2C281F),
            borderColor = Color(0x33FFD166),
            onClick = onAskQuestion,
            modifier = Modifier.weight(1f).testTag("quick_action_ask_tutor")
        )

        QuickActionButton(
            label = "CBT Mock",
            icon = Icons.Default.Timer,
            iconTint = Color(0xFF70D6FF),
            containerColor = Color(0xFF1A2633),
            borderColor = Color(0x3370D6FF),
            onClick = onStartCbt,
            modifier = Modifier.weight(1f).testTag("quick_action_cbt")
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    containerColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.height(72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun LiveClassCard(
    session: LiveClassSession,
    pulseAlpha: Float = 1f,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SlatyDarkSurface,
        border = BorderStroke(
            1.dp,
            if (session.isLive) Color(0xFFFF5252).copy(alpha = 0.7f) else Color(0xFF2D2840)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_class_card_${session.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top badges (Subject and Live indicator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2A243F)
                ) {
                    Text(
                        text = session.subject,
                        color = SlatyMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (session.isLive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33FF5252),
                        border = BorderStroke(1.dp, Color(0xFFFF5252))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252).copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LIVE NOW",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = SlatyTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = session.scheduledTime,
                            color = SlatyTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Title
            Text(
                text = session.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp
            )

            // Tutor info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF382F57)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = session.tutorName.take(1),
                        color = SlatyLavender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = session.tutorName,
                    color = Color(0xFFDDDDDD),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified Tutor",
                    tint = SlatyMint,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Attendees / Duration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = SlatyTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${session.attendeesCount} enrolled",
                        color = SlatyTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF241F35), thickness = 1.dp)

            // Bottom action: Fee & Join Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Access Fee",
                        color = SlatyTextSecondary,
                        fontSize = 10.sp
                    )
                    val formattedFee = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-NG"))
                        .format(session.fee)
                        .replace("NGN", "₦")
                    Text(
                        text = formattedFee,
                        color = SlatyMint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onJoin,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (session.isLive) Color(0xFFE53935) else SlatyPrimary
                    ),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("join_live_class_${session.id}")
                ) {
                    Icon(
                        imageVector = if (session.isLive) Icons.Default.PlayArrow else Icons.Default.Videocam,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (session.isLive) "Join Stream" else "Enter Class",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun StudyGroupCard(
    community: Community,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        color = SlatyDarkSurface,
        border = BorderStroke(1.dp, Color(0xFF2B253D)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_group_card_${community.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF261F3B)
                ) {
                    Text(
                        text = community.category,
                        color = SlatyLavender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Member count with green active status dot
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SlatyMint)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${community.memberCount} members",
                        color = SlatyTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Group Name & Unread badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = community.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (community.unreadCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = SlatyMint
                    ) {
                        Text(
                            text = "${community.unreadCount} new",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Description
            Text(
                text = community.description,
                color = Color(0xFFA5A3B8),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Academic levels tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                community.academicLevels.take(2).forEach { level ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1B1727)
                    ) {
                        Text(
                            text = level,
                            color = SlatyTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("open_study_group_${community.id}")
                ) {
                    Text(
                        text = "Join Discussion",
                        color = SlatyMint,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SlatyMint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CbtPromoCard(
    onStartQuiz: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF1A2633),
        border = BorderStroke(1.dp, Color(0x3370D6FF)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cbt_promo_card")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22364A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = Color(0xFF70D6FF),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Exam Practice Simulator",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Timed past questions for WAEC, JAMB & university courses.",
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onStartQuiz,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("start_cbt_button")
            ) {
                Text("Practice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SlatyDarkSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = SlatyTextSecondary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = SlatyTextSecondary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onAction) {
                Text(actionLabel, color = SlatyMint, fontSize = 12.sp)
            }
        }
    }
}
