package com.example.ui.screens.profile

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.screens.student.defaultTextFieldColors
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: User?,
    onBack: (() -> Unit)? = null,
    onLogout: () -> Unit,
    onProfileSaved: ((User) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var bio by remember(user) { mutableStateOf(user?.bio ?: "") }
    var institution by remember(user) { mutableStateOf(user?.institution ?: "University of Lagos") }
    var faculty by remember(user) { mutableStateOf(user?.faculty ?: "Science") }
    var department by remember(user) { mutableStateOf(user?.department ?: "Computer Science") }
    var academicLevel by remember(user) { mutableStateOf(user?.academicLevel ?: "Undergraduate (300L)") }
    var specialization by remember(user) { mutableStateOf(user?.specialization ?: "Verified Academic Tutor") }
    var hourlyRate by remember(user) { mutableStateOf(user?.hourlyRate?.toInt()?.toString() ?: "3500") }
    var bankName by remember(user) { mutableStateOf(user?.bankName ?: "GTBank") }
    var accountNumber by remember(user) { mutableStateOf(user?.accountNumber ?: "0123456789") }
    var accountName by remember(user) { mutableStateOf(user?.accountName ?: (user?.name ?: "")) }

    var isSaving by remember { mutableStateOf(false) }
    var isLoadingDoc by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val isTutor = user?.role == UserRole.TUTOR

    // Fetch latest user document from Firestore 'users' collection
    LaunchedEffect(user?.id) {
        val uid = user?.id ?: try { FirebaseAuth.getInstance().currentUser?.uid } catch (e: Exception) { null }
        if (uid != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    doc.getString("name")?.let { name = it }
                    doc.getString("email")?.let { email = it }
                    doc.getString("phone")?.let { phone = it }
                    doc.getString("bio")?.let { bio = it }
                    doc.getString("institution")?.let { institution = it }
                    doc.getString("faculty")?.let { faculty = it }
                    doc.getString("department")?.let { department = it }
                    doc.getString("academicLevel")?.let { academicLevel = it }
                    doc.getString("specialization")?.let { specialization = it }
                    doc.getString("bankName")?.let { bankName = it }
                    doc.getString("accountNumber")?.let { accountNumber = it }
                    doc.getString("accountName")?.let { accountName = it }
                    doc.getDouble("hourlyRate")?.let { hourlyRate = it.toInt().toString() }
                }
            } catch (e: Exception) {
                android.util.Log.w("UserProfileScreen", "Could not fetch remote user doc: ${e.message}")
            } finally {
                isLoadingDoc = false
            }
        } else {
            isLoadingDoc = false
        }
    }

    // Save updated profile back to Firestore 'users' collection
    fun saveProfile() {
        val uid = user?.id ?: try { FirebaseAuth.getInstance().currentUser?.uid } catch (e: Exception) { null }
        if (uid.isNullOrBlank()) {
            Toast.makeText(context, "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isBlank()) {
            statusMessage = "Name cannot be empty."
            return
        }

        isSaving = true
        statusMessage = null

        scope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val updates = hashMapOf<String, Any>(
                    "name" to name.trim(),
                    "email" to email.trim(),
                    "phone" to phone.trim(),
                    "bio" to bio.trim(),
                    "institution" to institution.trim(),
                    "faculty" to faculty.trim(),
                    "department" to department.trim(),
                    "academicLevel" to academicLevel.trim(),
                    "specialization" to specialization.trim(),
                    "hourlyRate" to (hourlyRate.toDoubleOrNull() ?: 3500.0),
                    "bankName" to bankName.trim(),
                    "accountNumber" to accountNumber.trim(),
                    "accountName" to accountName.trim(),
                    "updatedAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid)
                    .set(updates, SetOptions.merge())
                    .await()

                val updatedUser = (user ?: User(id = uid, name = name, username = name.lowercase().replace(" ", "_"), email = email)).copy(
                    name = name.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    bio = bio.trim(),
                    institution = institution.trim(),
                    faculty = faculty.trim(),
                    department = department.trim(),
                    academicLevel = academicLevel.trim(),
                    specialization = specialization.trim(),
                    hourlyRate = hourlyRate.toDoubleOrNull() ?: 3500.0,
                    bankName = bankName.trim(),
                    accountNumber = accountNumber.trim(),
                    accountName = accountName.trim()
                )

                onProfileSaved?.invoke(updatedUser)
                statusMessage = "Profile updated successfully in Firestore!"
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                statusMessage = "Error saving profile: ${e.localizedMessage}"
                Toast.makeText(context, "Save failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                isSaving = false
            }
        }
    }

    // Password reset dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = SlatyDarkSurface,
            title = {
                Text(
                    text = "Password Reset",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "A password reset link will be sent to:\n$email",
                    color = SlatyTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        try {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
                            Toast.makeText(context, "Reset link sent to $email", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
                ) {
                    Text("Send Email", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isTutor) "Tutor Profile & Credentials" else "Student Profile & Details",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("profile_logout_icon_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Log out",
                            tint = SlatyError
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlatyDarkBackground)
            )
        },
        containerColor = SlatyDarkBackground
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Loading indicator for remote Firestore doc
            AnimatedVisibility(visible = isLoadingDoc) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color = SlatyMint,
                    trackColor = Color(0xFF282538)
                )
            }

            // Avatar Header Card
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (isTutor) Color(0xFF1B4D3E) else Color(0xFF3B2F63),
                                Color(0xFF13101E)
                            )
                        )
                    )
                    .border(2.dp, if (isTutor) SlatyMint else SlatyLavender, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.firstOrNull()?.uppercase() ?: (user?.name?.firstOrNull()?.uppercase() ?: "S"),
                    color = if (isTutor) SlatyMint else Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = name.ifBlank { user?.name ?: "Slaty User" },
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Role Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isTutor) Color(0xFF1A382E) else Color(0xFF231C3D),
                border = BorderStroke(1.dp, if (isTutor) SlatyMint else SlatyLavender)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isTutor) Icons.Default.Verified else Icons.Default.School,
                        contentDescription = null,
                        tint = if (isTutor) SlatyMint else SlatyLavender,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTutor) "Verified Academic Tutor" else "Enrolled Student",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = email.ifBlank { user?.email ?: "" },
                color = SlatyTextSecondary,
                fontSize = 13.sp
            )

            // Status message banner
            statusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (msg.contains("success", ignoreCase = true)) Color(0xFF143026) else Color(0xFF381418),
                    border = BorderStroke(1.dp, if (msg.contains("success", ignoreCase = true)) SlatyMint else SlatyError)
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: Personal Information
            SectionCard(title = "Personal Information", icon = Icons.Default.Person) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Legal Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    colors = defaultTextFieldColors(),
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = SlatyMint) }
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Firebase Registered Email") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_email_input"),
                    colors = defaultTextFieldColors(),
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SlatyMint) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified with Firebase",
                            tint = SlatyMint
                        )
                    }
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+234 800 000 0000") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_phone_input"),
                    colors = defaultTextFieldColors(),
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SlatyMint) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: Academic Details
            SectionCard(title = "Academic Details", icon = Icons.Default.AccountBalance) {
                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("Institution / University") },
                    placeholder = { Text("e.g. University of Lagos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_institution_input"),
                    colors = defaultTextFieldColors(),
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = SlatyMint) }
                )

                if (!isTutor) {
                    OutlinedTextField(
                        value = faculty,
                        onValueChange = { faculty = it },
                        label = { Text("Faculty") },
                        placeholder = { Text("e.g. Engineering / Science") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_faculty_input"),
                        colors = defaultTextFieldColors()
                    )

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Department") },
                        placeholder = { Text("e.g. Computer Science") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_department_input"),
                        colors = defaultTextFieldColors()
                    )

                    OutlinedTextField(
                        value = academicLevel,
                        onValueChange = { academicLevel = it },
                        label = { Text("Current Academic Level") },
                        placeholder = { Text("e.g. 300 Level / JAMB Candidate") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_level_input"),
                        colors = defaultTextFieldColors()
                    )
                } else {
                    OutlinedTextField(
                        value = specialization,
                        onValueChange = { specialization = it },
                        label = { Text("Teaching Specialization") },
                        placeholder = { Text("e.g. Differential Calculus & Thermodynamics") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_specialization_input"),
                        colors = defaultTextFieldColors(),
                        leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, tint = SlatyMint) }
                    )

                    OutlinedTextField(
                        value = hourlyRate,
                        onValueChange = { hourlyRate = it },
                        label = { Text("Hourly Rate (₦)") },
                        placeholder = { Text("3500") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_hourly_rate_input"),
                        colors = defaultTextFieldColors(),
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = SlatyMint) }
                    )
                }

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(if (isTutor) "Academic Bio & Experience" else "About Me & Learning Goals") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_bio_input"),
                    colors = defaultTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: Payout Details
            SectionCard(title = if (isTutor) "Payout & Bank Details" else "Refund Bank Details", icon = Icons.Default.AccountBalanceWallet) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    placeholder = { Text("e.g. Guaranty Trust Bank / Access Bank") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_bank_name_input"),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Account Number") },
                    placeholder = { Text("10-digit NUBAN") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_account_number_input"),
                    colors = defaultTextFieldColors()
                )

                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Holder Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_account_name_input"),
                    colors = defaultTextFieldColors()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Save Action Button
            Button(
                onClick = { saveProfile() },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("profile_save_changes_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SlatyPrimary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Saving to Firebase...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Save Changes to Firestore",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Password Reset / Account Security Button
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_reset_password_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF3D3654))
            ) {
                Icon(Icons.Default.LockReset, contentDescription = null, tint = SlatyMint)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send Password Reset Email", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Log Out Button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("profile_logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SlatyError),
                border = BorderStroke(1.dp, SlatyError.copy(alpha = 0.6f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = SlatyError)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Account", color = SlatyError, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B1728),
        border = BorderStroke(1.dp, Color(0xFF2C2640))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = SlatyMint, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            HorizontalDivider(color = Color(0xFF2C2640), thickness = 1.dp)
            content()
        }
    }
}
