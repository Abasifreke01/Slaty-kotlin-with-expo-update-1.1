package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tutor
import com.example.ui.theme.*

@Composable
fun FindTutorScreen(
    tutors: List<Tutor>,
    searchQuery: String,
    selectedSubject: String?,
    onSearchChange: (String) -> Unit,
    onSubjectSelect: (String?) -> Unit,
    onTutorClick: (Tutor) -> Unit,
    onBookClick: (Tutor) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjects = listOf(
        "All", "Mathematics", "Physics", "Chemistry",
        "Biology", "Thermodynamics", "Economics", "Computer Science"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search by tutor name or subject...", color = Color(0xFF748C94)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = SlatyMint)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1F1C2F),
                unfocusedContainerColor = Color(0xFF1F1C2F),
                focusedBorderColor = SlatyMint,
                unfocusedBorderColor = Color(0xFF332D48),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("tutor_search_input")
        )

        // Subject Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.forEach { subject ->
                val isSelected = if (subject == "All") selectedSubject == null else selectedSubject == subject
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (subject == "All") onSubjectSelect(null) else onSubjectSelect(subject)
                    },
                    label = {
                        Text(
                            text = subject,
                            color = if (isSelected) Color(0xFF0F1E1B) else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SlatyMint,
                        containerColor = Color(0xFF201C2E)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color(0xFF332D48),
                        selectedBorderColor = SlatyMint
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tutors list
        if (tutors.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No tutors",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tutors found matching your criteria",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 130.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(tutors, key = { it.id }) { tutor ->
                    TutorCardItem(
                        tutor = tutor,
                        onClick = { onTutorClick(tutor) },
                        onBook = { onBookClick(tutor) }
                    )
                }
            }
        }
    }
}

@Composable
fun TutorCardItem(
    tutor: Tutor,
    onClick: () -> Unit,
    onBook: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("tutor_card_${tutor.id}"),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF1F1C2F),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2F2A44))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2740)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tutor.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                        color = SlatyMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tutor.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (tutor.verified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = SlatyMint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = tutor.specialization,
                        color = SlatyTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD166),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${tutor.rating}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${tutor.reviewsCount} reviews)",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }

                // Rate
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₦${tutor.hourlyRate.toInt()}",
                        color = SlatyMint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ hour",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subjects tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tutor.subjects.take(3).forEach { subj ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF28233D)
                    ) {
                        Text(
                            text = subj,
                            color = SlatySoftTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B3554))
                ) {
                    Text("View Profile", fontSize = 12.sp)
                }

                Button(
                    onClick = onBook,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("book_tutor_${tutor.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
                ) {
                    Text("Book Session", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
