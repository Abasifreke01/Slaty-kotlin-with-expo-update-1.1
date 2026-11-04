package com.example.ui.screens.student

import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Community
import com.example.model.HomeSearchTarget
import com.example.model.HomeSortOption
import com.example.model.Tutor
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StudentDashboardScreen(
    searchQuery: String = "",
    selectedTarget: HomeSearchTarget = HomeSearchTarget.ALL,
    selectedSubject: String? = null,
    selectedLevel: String? = null,
    selectedSortOption: HomeSortOption = HomeSortOption.HIGHEST_RATED,
    filteredTutors: List<Tutor> = emptyList(),
    filteredCommunities: List<Community> = emptyList(),
    onSearchQueryChange: (String) -> Unit = {},
    onTargetSelect: (HomeSearchTarget) -> Unit = {},
    onSubjectSelect: (String?) -> Unit = {},
    onLevelSelect: (String?) -> Unit = {},
    onSortOptionSelect: (HomeSortOption) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onTutorClick: (Tutor) -> Unit = {},
    onBookTutorClick: (Tutor) -> Unit = {},
    onCommunityClick: (Community) -> Unit = {},
    onSolveProblemClick: () -> Unit = {},
    onCbtClick: () -> Unit = {},
    onTopicClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showFiltersPanel by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val academicLevels = listOf(
        "All Levels",
        "Secondary (WAEC/NECO)",
        "JAMB/UTME",
        "Undergraduate",
        "Postgraduate"
    )

    val subjects = listOf(
        "All Subjects",
        "Mathematics",
        "Physics",
        "Chemistry",
        "Biology",
        "Economics",
        "Engineering"
    )

    val isSearchActive = searchQuery.isNotBlank() || selectedSubject != null || selectedLevel != null || selectedTarget != HomeSearchTarget.ALL
    val totalMatches = (if (selectedTarget != HomeSearchTarget.STUDY_GROUPS) filteredTutors.size else 0) +
            (if (selectedTarget != HomeSearchTarget.TUTORS) filteredCommunities.size else 0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlatyDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Bar & Filter Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input with leading search and trailing controls
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search tutors or study groups by subject/level...",
                            color = Color(0xFF7F8B9B),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SlatyMint,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.testTag("home_search_clear_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            // Filter toggle button
                            IconButton(
                                onClick = { showFiltersPanel = !showFiltersPanel },
                                modifier = Modifier.testTag("home_search_filter_toggle")
                            ) {
                                val activeFilterCount = (if (selectedSubject != null) 1 else 0) +
                                        (if (selectedLevel != null) 1 else 0) +
                                        (if (selectedTarget != HomeSearchTarget.ALL) 1 else 0)
                                BadgedBox(
                                    badge = {
                                        if (activeFilterCount > 0) {
                                            Badge(
                                                containerColor = SlatyMint,
                                                contentColor = Color.Black
                                            ) {
                                                Text(activeFilterCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filter options",
                                        tint = if (showFiltersPanel || activeFilterCount > 0) SlatyMint else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1B2C),
                        unfocusedContainerColor = Color(0xFF1E1B2C),
                        focusedBorderColor = SlatyMint,
                        unfocusedBorderColor = Color(0xFF332D48),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input")
                )

                // Quick Target Switcher Tabs: All, Tutors, Study Groups
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF191624))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HomeSearchTarget.values().forEach { target ->
                        val isSelected = selectedTarget == target
                        val countLabel = when (target) {
                            HomeSearchTarget.ALL -> "All (${filteredTutors.size + filteredCommunities.size})"
                            HomeSearchTarget.TUTORS -> "Tutors (${filteredTutors.size})"
                            HomeSearchTarget.STUDY_GROUPS -> "Study Groups (${filteredCommunities.size})"
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onTargetSelect(target) }
                                .testTag("home_target_${target.name.lowercase()}"),
                            color = if (isSelected) SlatyMint else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = countLabel,
                                    color = if (isSelected) Color(0xFF07241E) else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Expandable/Visible Filter Chips for Academic Level & Subject
                AnimatedVisibility(
                    visible = showFiltersPanel || isSearchActive,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1A1727))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header with Clear Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter By Academic Level & Subject",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isSearchActive) {
                                Text(
                                    text = "Reset Filters",
                                    color = SlatyMint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { onClearFilters() }
                                        .testTag("home_reset_filters_btn")
                                )
                            }
                        }

                        // Academic Level Chips
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ACADEMIC LEVEL",
                                color = SlatyTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                academicLevels.forEach { level ->
                                    val isSelected = if (level == "All Levels") selectedLevel == null else selectedLevel == level
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onLevelSelect(if (level == "All Levels") null else level)
                                        },
                                        label = {
                                            Text(
                                                text = level,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF0F1E1B) else Color.White
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SlatyMint,
                                            containerColor = Color(0xFF262137)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) SlatyMint else Color(0xFF38314F)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("filter_level_${level.lowercase().replace(" ", "_")}")
                                    )
                                }
                            }
                        }

                        // Subject Chips
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "SUBJECT",
                                color = SlatyTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                subjects.forEach { subj ->
                                    val isSelected = if (subj == "All Subjects") selectedSubject == null else selectedSubject == subj
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onSubjectSelect(if (subj == "All Subjects") null else subj)
                                        },
                                        label = {
                                            Text(
                                                text = subj,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF0F1E1B) else Color.White
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SlatyMint,
                                            containerColor = Color(0xFF262137)
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = if (isSelected) SlatyMint else Color(0xFF38314F)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("filter_subject_${subj.lowercase().replace(" ", "_")}")
                                    )
                                }
                            }
                        }
                    }
                }

                // Sort Controls Row with Material 3 Dropdown Menu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort order",
                            tint = SlatyMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Sort by:",
                            color = SlatyTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { sortMenuExpanded = true }
                                .testTag("home_sort_dropdown_trigger"),
                            color = Color(0xFF201C2E),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = selectedSortOption.displayName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Open sort dropdown",
                                    tint = SlatyMint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            modifier = Modifier
                                .background(Color(0xFF201C2E))
                                .testTag("home_sort_dropdown_menu")
                        ) {
                            HomeSortOption.values().forEach { option ->
                                val isSelected = selectedSortOption == option
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            color = if (isSelected) SlatyMint else Color.White,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    },
                                    leadingIcon = {
                                        val icon = when (option) {
                                            HomeSortOption.HIGHEST_RATED -> Icons.Default.Star
                                            HomeSortOption.PRICE_LOW_TO_HIGH -> Icons.Default.Payments
                                            HomeSortOption.NEWEST -> Icons.Default.Schedule
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) SlatyMint else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = SlatyMint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSortOptionSelect(option)
                                        sortMenuExpanded = false
                                    },
                                    modifier = Modifier.testTag("sort_option_${option.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Results Section (Shown when query or filters are active)
        if (isSearchActive) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Matching Results ($totalMatches)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedSubject != null || selectedLevel != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SlatyMint.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = listOfNotNull(selectedSubject, selectedLevel).joinToString(" • "),
                                color = SlatyMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Empty state when search yields no matches
            if (totalMatches == 0) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF201C2E),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E2742)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = "No results",
                                    tint = SlatyMint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "No Tutors or Study Groups Found",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try searching for a different keyword or selecting \"All Subjects\" / \"All Levels\".",
                                color = SlatyTextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = onClearFilters,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SlatyMint,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_reset_filters_button")
                            ) {
                                Text("Reset All Filters", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Tutors Results
            if (selectedTarget != HomeSearchTarget.STUDY_GROUPS && filteredTutors.isNotEmpty()) {
                item {
                    Text(
                        text = "Tutors (${filteredTutors.size})",
                        color = SlatyMint,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(filteredTutors, key = { "home_tutor_${it.id}" }) { tutor ->
                    HomeTutorCard(
                        tutor = tutor,
                        onTutorClick = { onTutorClick(tutor) },
                        onBookClick = { onBookTutorClick(tutor) }
                    )
                }
            }

            // Study Groups Results
            if (selectedTarget != HomeSearchTarget.TUTORS && filteredCommunities.isNotEmpty()) {
                item {
                    Text(
                        text = "Study Groups (${filteredCommunities.size})",
                        color = Color(0xFFB9A9FB),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(filteredCommunities, key = { "home_comm_${it.id}" }) { comm ->
                    HomeCommunityCard(
                        community = comm,
                        onClick = { onCommunityClick(comm) }
                    )
                }
            }

            // Divider before standard sections
            item {
                HorizontalDivider(
                    color = Color(0xFF332D48),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }

        // Two big action cards (from Slaty React Native Dashboard)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Solve a Problem
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSolveProblemClick() }
                        .testTag("action_solve_problem"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFB0E4DD)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F4D44)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Solve problem",
                                tint = Color(0xFFB0E4DD),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "Solve a Problem",
                            color = Color(0xFF0B2B26),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // JAMB / CBT Practice
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(110.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onCbtClick() }
                        .testTag("action_jamb_cbt"),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFB9A9FB)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF382C6C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = "JAMB CBT",
                                tint = Color(0xFFB9A9FB),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = "JAMB / CBT",
                            color = Color(0xFF1E1442),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // My Activity / Your Progress section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Activity",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your Progress",
                        color = SlatyTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF201C2E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF3A315C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Math",
                                tint = SlatyLavender,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "General Mathematics",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { 0.42f },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = SlatyMint,
                                    trackColor = Color(0xFF332D48),
                                )
                                Text(
                                    text = "42%",
                                    color = SlatyMint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Popular Courses / Topics with Quick Search Filter Trigger
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Popular Courses",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                val popularTopics = listOf(
                    Triple("Calculus & Limits", Color(0xFFB0E4DD), Color(0xFF0F4D44)),
                    Triple("Organic Chemistry", Color(0xFFB9A9FB), Color(0xFF382C6C)),
                    Triple("Classical Mechanics", Color(0xFFFBD38D), Color(0xFF6B46C1)),
                    Triple("Thermodynamics", Color(0xFF9AE6B4), Color(0xFF22543D))
                )

                // 2x2 grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PopularTopicCard(
                            topic = popularTopics[0].first,
                            bgColor = popularTopics[0].second,
                            textColor = popularTopics[0].third,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onTopicClick(popularTopics[0].first)
                            }
                        )
                        PopularTopicCard(
                            topic = popularTopics[1].first,
                            bgColor = popularTopics[1].second,
                            textColor = popularTopics[1].third,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onTopicClick(popularTopics[1].first)
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PopularTopicCard(
                            topic = popularTopics[2].first,
                            bgColor = popularTopics[2].second,
                            textColor = popularTopics[2].third,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onTopicClick(popularTopics[2].first)
                            }
                        )
                        PopularTopicCard(
                            topic = popularTopics[3].first,
                            bgColor = popularTopics[3].second,
                            textColor = popularTopics[3].third,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onTopicClick(popularTopics[3].first)
                            }
                        )
                    }
                }
            }
        }

        // Trending Masterclass Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0D5E53), Color(0xFF1E1B2C))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth(0.75f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SlatyMint.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "TRENDING MASTERCLASS",
                                color = SlatyMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Score 300+ in JAMB with Slaty CBT Drills",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Timed questions, full explanations & tutor consultations.",
                            color = Color(0xFFD0D0E0),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Tutors & Study Groups List (Displayed during browsing, dynamically sorted)
        if (!isSearchActive) {
            if (selectedTarget != HomeSearchTarget.STUDY_GROUPS && filteredTutors.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tutors (${filteredTutors.size})",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sorted by ${selectedSortOption.displayName}",
                            color = SlatyMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(filteredTutors, key = { "home_browse_tutor_${it.id}" }) { tutor ->
                    HomeTutorCard(
                        tutor = tutor,
                        onTutorClick = { onTutorClick(tutor) },
                        onBookClick = { onBookTutorClick(tutor) }
                    )
                }
            }

            if (selectedTarget != HomeSearchTarget.TUTORS && filteredCommunities.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Study Groups (${filteredCommunities.size})",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sorted by ${selectedSortOption.displayName}",
                            color = Color(0xFFB9A9FB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(filteredCommunities, key = { "home_browse_comm_${it.id}" }) { comm ->
                    HomeCommunityCard(
                        community = comm,
                        onClick = { onCommunityClick(comm) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularTopicCard(
    topic: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("topic_${topic.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        color = bgColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = topic,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HomeTutorCard(
    tutor: Tutor,
    onTutorClick: () -> Unit,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-NG")).apply {
        maximumFractionDigits = 0
    }
    val formattedRate = currencyFormat.format(tutor.hourlyRate).replace("NGN", "₦")

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onTutorClick() }
            .testTag("home_tutor_card_${tutor.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF201C2E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Initials
                val initials = tutor.name.split(" ")
                    .filter { it.isNotBlank() && !it.startsWith("Dr.") }
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .joinToString("")
                    .ifEmpty { "TU" }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF3A315C), Color(0xFF201C2E))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = SlatyMint,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tutor.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (tutor.verified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Tutor",
                                tint = SlatyMint,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Text(
                        text = tutor.specialization,
                        color = SlatyTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Rating Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2D2743)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB800),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", tutor.rating),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Academic Levels & Subjects tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tutor.academicLevels.forEach { lvl ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF2D2743)
                    ) {
                        Text(
                            text = lvl,
                            color = SlatyLavender,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                tutor.subjects.take(3).forEach { subj ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SlatyMint.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = subj,
                            color = SlatyMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Bottom row: Rate & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$formattedRate / hr",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${tutor.reviewsCount} reviews",
                        color = SlatyTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onTutorClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3E3658)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("view_tutor_btn_${tutor.id}")
                    ) {
                        Text("Profile", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onBookClick,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlatyMint,
                            contentColor = Color(0xFF06211B)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("book_tutor_btn_${tutor.id}")
                    ) {
                        Text("Book", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeCommunityCard(
    community: Community,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("home_comm_card_${community.id}"),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF201C2E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF332D48))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF30274C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Study Group",
                    tint = SlatyLavender,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = community.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = community.description,
                    color = SlatyTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF2C2442)
                    ) {
                        Text(
                            text = "${community.memberCount} members",
                            color = SlatyMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    community.academicLevels.firstOrNull()?.let { lvl ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF382F57)
                        ) {
                            Text(
                                text = lvl,
                                color = SlatyLavender,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onClick,
                modifier = Modifier.testTag("join_comm_btn_${community.id}")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open group",
                    tint = SlatyMint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
