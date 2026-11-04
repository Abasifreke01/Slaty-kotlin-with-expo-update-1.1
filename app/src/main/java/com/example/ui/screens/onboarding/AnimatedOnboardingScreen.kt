package com.example.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val primaryIcon: ImageVector,
    val badge1: String,
    val badge2: String,
    val accentColor: Color,
    val iconBgGradient: List<Color>
)

@Composable
fun AnimatedOnboardingScreen(
    onContinueAsStudent: () -> Unit,
    onContinueAsTutor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Conquer Academic Tasks With Verified Tutors",
                subtitle = "Submit assignments, thesis calculations, and complex equations. Certified university tutors provide step-by-step verified explanations.",
                primaryIcon = Icons.AutoMirrored.Filled.MenuBook,
                badge1 = "Step-by-Step Solutions",
                badge2 = "100+ University Courses",
                accentColor = SlatyMint,
                iconBgGradient = listOf(Color(0xFF2E6F5E), Color(0xFF132A24))
            ),
            OnboardingPageData(
                title = "Live HD Masterclasses & Interactive Video",
                subtitle = "Join real-time classrooms, interactive whiteboards, and peer study halls. Engage directly with verified lecturers across Nigeria.",
                primaryIcon = Icons.Default.VideoCall,
                badge1 = "HD Live Whiteboards",
                badge2 = "Real-time Interaction",
                accentColor = SlatyPrimary,
                iconBgGradient = listOf(Color(0xFF5B4294), Color(0xFF1E1438))
            ),
            OnboardingPageData(
                title = "Timed CBT Practice & University Exam Prep",
                subtitle = "Prepare for JAMB UTME, WAEC, NECO, and Post-UTME with timed test simulations, instant scoring, and comprehensive answer rationales.",
                primaryIcon = Icons.Default.Quiz,
                badge1 = "Realistic Exam Timers",
                badge2 = "Detailed Rationales",
                accentColor = Color(0xFFFFB74D),
                iconBgGradient = listOf(Color(0xFF8D5314), Color(0xFF331D05))
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    // Floating pulse animation for main icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Logo + Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.slaty_login_logo),
                contentDescription = "Slaty",
                modifier = Modifier
                    .width(130.dp)
                    .height(40.dp)
            )

            if (pagerState.currentPage < pages.size - 1) {
                TextButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pages.size - 1)
                        }
                    },
                    modifier = Modifier.testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "Skip",
                        color = SlatyTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { pageIndex ->
            val page = pages[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Central Animated Hero Visual
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(page.iconBgGradient))
                        .border(2.dp, page.accentColor.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.primaryIcon,
                        contentDescription = page.title,
                        tint = page.accentColor,
                        modifier = Modifier.size(84.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Feature Pill Badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E1B2C),
                        border = BorderStroke(1.dp, page.accentColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = page.badge1,
                            color = page.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E1B2C),
                        border = BorderStroke(1.dp, Color(0xFF332D48))
                    ) {
                        Text(
                            text = page.badge2,
                            color = Color(0xFFCCCCCC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = page.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = page.subtitle,
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Animated Page Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.indices.forEach { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(
                    targetValue = if (isSelected) 28.dp else 8.dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "indicatorWidth"
                )
                val dotColor = if (isSelected) SlatyMint else Color(0xFF36324D)

                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons Bottom Bar
        if (pagerState.currentPage < pages.size - 1) {
            Button(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
            ) {
                Text(
                    text = "Next",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            // Last Page: Choice to continue as Student or sign in as Tutor
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onContinueAsStudent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_continue_student_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
                ) {
                    Icon(Icons.Default.School, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue as Student",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onContinueAsTutor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_continue_tutor_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(2.dp, SlatyMint)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SlatyMint)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign In as Tutor",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
