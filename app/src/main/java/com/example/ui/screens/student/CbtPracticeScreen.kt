package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CbtQuizState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CbtPracticeScreen(
    quizState: CbtQuizState,
    onBack: () -> Unit,
    onSelectAnswer: (questionIndex: Int, optionIndex: Int) -> Unit,
    onNavigateQuestion: (Int) -> Unit,
    onSubmit: () -> Unit,
    onRestart: () -> Unit,
    onSwitchSubject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQ = quizState.questions.getOrNull(quizState.currentIndex)
    val minutes = quizState.secondsRemaining / 60
    val seconds = quizState.secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${quizState.subject} JAMB / CBT Drill",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (quizState.isFinished) "Test Finished" else "Time Remaining: $formattedTime",
                            color = if (quizState.secondsRemaining < 120 && !quizState.isFinished) SlatyError else SlatyMint,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Switch Subject Pill
                    TextButton(
                        onClick = {
                            val nextSubj = if (quizState.subject == "Biology") "Mathematics" else "Biology"
                            onSwitchSubject(nextSubj)
                        }
                    ) {
                        Text(
                            text = if (quizState.subject == "Biology") "Try Math" else "Try Bio",
                            color = SlatyLavender,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlatyDarkBackground)
            )
        },
        containerColor = SlatyDarkBackground,
        bottomBar = {
            if (!quizState.isFinished) {
                Surface(
                    color = Color(0xFF151320),
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onNavigateQuestion(quizState.currentIndex - 1) },
                            enabled = quizState.currentIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Previous")
                        }

                        Button(
                            onClick = onSubmit,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlatyError),
                            modifier = Modifier.testTag("cbt_submit_button")
                        ) {
                            Text("Submit Test", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onNavigateQuestion(quizState.currentIndex + 1) },
                            enabled = quizState.currentIndex < quizState.questions.size - 1,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
                        ) {
                            Text("Next", color = Color.White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Result Banner if finished
            if (quizState.isFinished) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF1F1C2F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SlatyMint),
                    modifier = Modifier.fillMaxWidth().testTag("cbt_score_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val percentage = if (quizState.total > 0) (quizState.score * 100) / quizState.total else 0
                        Text("Exam Completed!", color = SlatyMint, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${quizState.score} / ${quizState.total}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Score: $percentage% accuracy",
                            color = if (percentage >= 70) SlatyMint else SlatyAccentYellow,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onRestart,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
                            ) {
                                Text("Retake Test", color = Color.White)
                            }
                            OutlinedButton(
                                onClick = onBack,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Return Home")
                            }
                        }
                    }
                }
            }

            // Question Palette Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quizState.questions.forEachIndexed { idx, _ ->
                    val isCurrent = idx == quizState.currentIndex
                    val isAnswered = quizState.userAnswers.containsKey(idx)
                    val bg = when {
                        isCurrent -> SlatyMint
                        isAnswered -> Color(0xFF2E5E55)
                        else -> Color(0xFF201C2E)
                    }
                    val textColor = if (isCurrent) Color.Black else Color.White

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(bg)
                            .clickable { onNavigateQuestion(idx) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Question Card
            if (currentQ != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1F1C2F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Question ${quizState.currentIndex + 1} of ${quizState.questions.size}",
                                color = SlatyMint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = quizState.subject,
                                color = SlatyTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentQ.question,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Options list
                currentQ.options.forEachIndexed { optIndex, optText ->
                    val isSelected = quizState.userAnswers[quizState.currentIndex] == optIndex
                    val isCorrect = optIndex == currentQ.correctIndex

                    val borderColor = when {
                        quizState.isFinished && isCorrect -> SlatyMint
                        quizState.isFinished && isSelected && !isCorrect -> SlatyError
                        isSelected -> SlatyMint
                        else -> Color(0xFF332D48)
                    }

                    val containerColor = when {
                        quizState.isFinished && isCorrect -> Color(0xFF123D34)
                        quizState.isFinished && isSelected && !isCorrect -> Color(0xFF42161A)
                        isSelected -> Color(0xFF1F3832)
                        else -> Color(0xFF1F1C2F)
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = containerColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !quizState.isFinished) {
                                onSelectAnswer(quizState.currentIndex, optIndex)
                            }
                            .testTag("option_${quizState.currentIndex}_$optIndex")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val optionLetter = ('A' + optIndex).toString()
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected || (quizState.isFinished && isCorrect)) SlatyMint else Color(0xFF2C2740)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionLetter,
                                    color = if (isSelected || (quizState.isFinished && isCorrect)) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = optText,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )

                            if (quizState.isFinished) {
                                if (isCorrect) {
                                    Icon(Icons.Default.Check, contentDescription = "Correct", tint = SlatyMint)
                                } else if (isSelected) {
                                    Icon(Icons.Default.Close, contentDescription = "Wrong", tint = SlatyError)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
