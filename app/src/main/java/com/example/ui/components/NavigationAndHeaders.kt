package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.StudentTab
import com.example.viewmodel.TutorTab

@Composable
fun SlatyStudentBottomBar(
    selectedTab: StudentTab,
    unreadConnectCount: Int,
    onTabSelected: (StudentTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("student_bottom_bar"),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0F0E17),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282538))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(StudentTab.HOME, Icons.Default.Home, Icons.Outlined.Home),
                Triple(StudentTab.FIND_TUTOR, Icons.Default.Search, Icons.Outlined.Search),
                Triple(StudentTab.LIVE_CLASSES, Icons.Default.PlayCircle, Icons.Outlined.PlayCircleOutline),
                Triple(StudentTab.CONNECT, Icons.Default.Forum, Icons.Outlined.Forum),
                Triple(StudentTab.PERSONAL, Icons.Default.Person, Icons.Outlined.PersonOutline)
            )

            tabs.forEach { (tab, filledIcon, outlinedIcon) ->
                val isSelected = tab == selectedTab
                val color = if (isSelected) SlatyMint else Color(0xFF748C94)

                Box(
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box {
                            Icon(
                                imageVector = if (isSelected) filledIcon else outlinedIcon,
                                contentDescription = tab.title,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                            if (tab == StudentTab.CONNECT && unreadConnectCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .background(SlatyError, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadConnectCount > 9) "9+" else unreadConnectCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.title,
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlatyTutorBottomBar(
    selectedTab: TutorTab,
    unreadConnectCount: Int,
    onTabSelected: (TutorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("tutor_bottom_bar"),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0F0E17),
        tonalElevation = 8.dp,
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF282538))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple(TutorTab.HOME, Icons.Default.Dashboard, Icons.Outlined.Dashboard),
                Triple(TutorTab.FIND_TOPICS, Icons.Default.WorkOutline, Icons.Outlined.WorkOutline),
                Triple(TutorTab.HOST_CLASS, Icons.Default.VideoCall, Icons.Outlined.VideoCall),
                Triple(TutorTab.CONNECT, Icons.Default.ChatBubble, Icons.Outlined.ChatBubbleOutline),
                Triple(TutorTab.PAYOUTS, Icons.Default.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
            )

            tabs.forEach { (tab, filledIcon, outlinedIcon) ->
                val isSelected = tab == selectedTab
                val color = if (isSelected) SlatyMint else Color(0xFF748C94)

                Box(
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("tutor_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box {
                            Icon(
                                imageVector = if (isSelected) filledIcon else outlinedIcon,
                                contentDescription = tab.title,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                            if (tab == TutorTab.CONNECT && unreadConnectCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .background(SlatyError, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (unreadConnectCount > 9) "9+" else unreadConnectCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = tab.title,
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlatyTopHeader(
    user: User?,
    onNotificationClick: () -> Unit,
    onRoleSwitchClick: () -> Unit,
    onProfileClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Greeting & Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = onProfileClick != null) { onProfileClick?.invoke() }
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF201C2E))
                    .border(2.dp, SlatyMint, CircleShape)
                    .testTag("top_header_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user?.name?.firstOrNull()?.uppercase() ?: "S",
                    color = SlatyMint,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Welcome back,",
                    color = SlatyTextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = user?.name ?: "Learner",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Role Switcher pill & Notification icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Role switcher pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onRoleSwitchClick() }
                    .testTag("role_switcher_pill"),
                color = if (user?.role == UserRole.TUTOR) Color(0xFF1B3B36) else Color(0xFF272142),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (user?.role == UserRole.TUTOR) SlatyMint else SlatyLavender
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (user?.role == UserRole.TUTOR) Icons.Default.School else Icons.Default.Person,
                        contentDescription = "Switch role",
                        tint = if (user?.role == UserRole.TUTOR) SlatyMint else SlatyLavender,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = user?.role?.displayName ?: "Student",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Notification button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F1C2F))
                    .clickable { onNotificationClick() }
                    .testTag("notification_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(8.dp)
                        .background(SlatyError, CircleShape)
                )
            }
        }
    }
}
