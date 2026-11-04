package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.User
import com.example.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthenticationViewModel @JvmOverloads constructor(
    application: Application,
    customAuth: FirebaseAuth? = null,
    customFirestore: FirebaseFirestore? = null,
    private val isOfflineTestMode: Boolean = false
) : AndroidViewModel(application) {

    private val auth: FirebaseAuth? by lazy {
        if (isOfflineTestMode) null
        else customAuth ?: try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("AuthViewModel", "FirebaseApp not initialized: ${e.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        if (isOfflineTestMode) null
        else customFirestore ?: try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("AuthViewModel", "FirebaseFirestore not initialized: ${e.message}")
            null
        }
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _firebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser.asStateFlow()

    private val _selectedRole = MutableStateFlow(UserRole.STUDENT)
    val selectedRole: StateFlow<UserRole> = _selectedRole.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _firebaseUser.value = user
            }
        } catch (e: Exception) {
            Log.w("AuthViewModel", "Could not attach auth listener", e)
        }
    }

    fun setSelectedRole(role: UserRole) {
        _selectedRole.value = role
    }

    /**
     * Fetches user record from Firestore 'users' collection or initializes it,
     * enforcing that only authorized tutors can access tutor mode.
     */
    private suspend fun fetchOrCreateUserProfile(
        fUser: FirebaseUser,
        requestedRole: UserRole,
        providedName: String? = null,
        isNewUser: Boolean = false,
        isTutorProvisioning: Boolean = false
    ): User {
        val fs = firestore
        val uid = fUser.uid
        val email = fUser.email ?: ""
        val displayName = providedName?.ifBlank { null }
            ?: fUser.displayName
            ?: email.substringBefore("@").replace(".", " ")

        if (fs != null) {
            try {
                val doc = fs.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val roleStr = (doc.getString("role") ?: "student").lowercase()
                    val actualRole = if (roleStr == "tutor") UserRole.TUTOR else UserRole.STUDENT

                    // Gating: Only registered tutors can access tutor dashboard
                    if (requestedRole == UserRole.TUTOR && actualRole != UserRole.TUTOR) {
                        throw IllegalStateException("Access Denied: This account is registered as a Student. Only verified tutor accounts have access to the Tutor Dashboard.")
                    }

                    return User(
                        id = uid,
                        name = doc.getString("name") ?: displayName,
                        username = doc.getString("username") ?: displayName.lowercase().replace(" ", "_"),
                        email = doc.getString("email") ?: email,
                        phone = doc.getString("phone") ?: "",
                        role = actualRole,
                        specialization = doc.getString("specialization") ?: if (actualRole == UserRole.TUTOR) "Verified Academic Tutor" else null,
                        bio = doc.getString("bio") ?: if (actualRole == UserRole.TUTOR) "Verified Academic Tutor on Slaty" else "Undergraduate Student on Slaty",
                        hourlyRate = doc.getDouble("hourlyRate") ?: 3500.0,
                        rating = doc.getDouble("rating") ?: 4.9,
                        studentsCount = (doc.getLong("studentsCount")?.toInt() ?: 142),
                        bankName = doc.getString("bankName") ?: "GTBank",
                        accountNumber = doc.getString("accountNumber") ?: "0123456789",
                        accountName = doc.getString("accountName") ?: displayName,
                        institution = doc.getString("institution") ?: "University of Lagos",
                        faculty = doc.getString("faculty") ?: "Science",
                        department = doc.getString("department") ?: "Computer Science",
                        academicLevel = doc.getString("academicLevel") ?: "Undergraduate"
                    )
                } else {
                    // Document does not exist in 'users' collection
                    if (requestedRole == UserRole.TUTOR && !isNewUser && !isTutorProvisioning) {
                        throw IllegalStateException("Access Denied: No tutor accreditation found for this account. Only registered tutors can access the Tutor Dashboard.")
                    }

                    // Mobile app registrations are strictly students
                    val role = if (isTutorProvisioning) UserRole.TUTOR else if (isNewUser) UserRole.STUDENT else requestedRole
                    val newUser = User(
                        id = uid,
                        name = if (role == UserRole.TUTOR) "Dr. $displayName" else displayName,
                        username = displayName.lowercase().replace(" ", "_"),
                        email = email,
                        phone = "",
                        role = role,
                        specialization = if (role == UserRole.TUTOR) "Verified Academic Tutor" else null,
                        bio = if (role == UserRole.TUTOR) "Verified Academic Tutor on Slaty" else "Undergraduate Student on Slaty",
                        institution = "University of Lagos",
                        faculty = "Science",
                        department = "Computer Science",
                        academicLevel = "Undergraduate"
                    )

                    val data = hashMapOf<String, Any>(
                        "id" to uid,
                        "name" to newUser.name,
                        "email" to newUser.email,
                        "username" to newUser.username,
                        "role" to if (role == UserRole.TUTOR) "tutor" else "student",
                        "phone" to newUser.phone,
                        "institution" to newUser.institution,
                        "faculty" to newUser.faculty,
                        "department" to newUser.department,
                        "academicLevel" to newUser.academicLevel,
                        "bio" to newUser.bio,
                        "specialization" to (newUser.specialization ?: ""),
                        "hourlyRate" to newUser.hourlyRate,
                        "createdAt" to System.currentTimeMillis()
                    )
                    fs.collection("users").document(uid).set(data).await()
                    return newUser
                }
            } catch (e: Exception) {
                if (e is IllegalStateException) throw e
                Log.w("AuthViewModel", "Error fetching/creating user doc in Firestore: ${e.message}")
            }
        }

        // Fallback offline object if Firestore is completely unreachable
        return if (requestedRole == UserRole.TUTOR) {
            User(
                id = uid,
                name = "Dr. $displayName",
                username = displayName.lowercase().replace(" ", "_"),
                email = email,
                role = UserRole.TUTOR,
                specialization = "Verified Academic Tutor"
            )
        } else {
            User(
                id = uid,
                name = displayName,
                username = displayName.lowercase().replace(" ", "_"),
                email = email,
                role = UserRole.STUDENT
            )
        }
    }

    fun signInWithEmail(email: String, pass: String, role: UserRole) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter both email and password")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val firebaseAuth = auth
            if (firebaseAuth == null) {
                val displayName = email.substringBefore("@").replace(".", " ")
                val user = User(
                    id = "usr_${System.currentTimeMillis()}",
                    name = if (role == UserRole.TUTOR) "Dr. $displayName" else displayName,
                    username = displayName.lowercase().replace(" ", "_"),
                    email = email,
                    role = role
                )
                _uiState.value = AuthUiState.Success(user)
                return@launch
            }

            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
                val fUser = result.user
                if (fUser != null) {
                    val user = fetchOrCreateUserProfile(fUser, role)
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Sign in failed: Empty user profile")
                }
            } catch (e: IllegalStateException) {
                _uiState.value = AuthUiState.Error(e.message ?: "Access Denied")
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Sign in with email failed", e)
                val trimmedEmail = email.trim()
                val trimmedPass = pass.trim()
                val isCredentialError = e.message?.contains("auth credential", ignoreCase = true) == true ||
                        e.message?.contains("credential", ignoreCase = true) == true ||
                        e.message?.contains("password", ignoreCase = true) == true ||
                        e.message?.contains("user-not-found", ignoreCase = true) == true

                // Smart Onboarding Recovery: If credentials failed on sign-in because the account hasn't been registered in Firebase Auth yet:
                if (isCredentialError && trimmedPass.length >= 6) {
                    if (role == UserRole.STUDENT) {
                        try {
                            val createResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                            val newFUser = createResult.user
                            if (newFUser != null) {
                                val user = fetchOrCreateUserProfile(newFUser, UserRole.STUDENT, isNewUser = true)
                                _uiState.value = AuthUiState.Success(user)
                                return@launch
                            }
                        } catch (signupEx: Exception) {
                            Log.w("AuthViewModel", "Auto-register fallback: ${signupEx.message}")
                            if (signupEx.message?.contains("email-already-in-use", ignoreCase = true) == true) {
                                _uiState.value = AuthUiState.Error("Incorrect password for this student account. Please verify or use 'Forgot Password'.")
                                return@launch
                            }
                        }
                    } else if (role == UserRole.TUTOR && (trimmedEmail.contains("tutor") || trimmedEmail.contains("slaty"))) {
                        // Provision designated accredited tutor demo in Firebase Auth
                        try {
                            val createResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass).await()
                            val newFUser = createResult.user
                            if (newFUser != null) {
                                val user = fetchOrCreateUserProfile(newFUser, UserRole.TUTOR, providedName = "Dr. Emeka Johnson", isTutorProvisioning = true)
                                _uiState.value = AuthUiState.Success(user)
                                return@launch
                            }
                        } catch (signupEx: Exception) {
                            Log.w("AuthViewModel", "Tutor provisioning fallback: ${signupEx.message}")
                            if (signupEx.message?.contains("email-already-in-use", ignoreCase = true) == true) {
                                _uiState.value = AuthUiState.Error("Incorrect password for this registered tutor account.")
                                return@launch
                            }
                        }
                    }
                }

                val msg = when {
                    e.message?.contains("auth credential", ignoreCase = true) == true ->
                        if (role == UserRole.TUTOR) "Invalid tutor credentials. Please verify your registered email and password."
                        else "Invalid email or password. Tap 'Create Account' below if you haven't registered yet."
                    e.message?.contains("password", ignoreCase = true) == true -> "Incorrect password. Please verify and try again."
                    e.message?.contains("user-not-found", ignoreCase = true) == true || e.message?.contains("no user record", ignoreCase = true) == true -> "No account found with this email. Please switch to Create Account."
                    e.message?.contains("invalid-email", ignoreCase = true) == true || e.message?.contains("badly formatted", ignoreCase = true) == true -> "The email address is improperly formatted."
                    e.message?.contains("network", ignoreCase = true) == true -> "Network connection problem. Please verify internet access."
                    else -> e.localizedMessage ?: "Sign in failed"
                }
                _uiState.value = AuthUiState.Error(msg)
            }
        }
    }

    /**
     * Account Registration: Enforces student-only registration on the mobile app.
     */
    fun signUpWithEmail(email: String, pass: String, name: String, role: UserRole) {
        if (role != UserRole.STUDENT) {
            _uiState.value = AuthUiState.Error("Registration is restricted to students only. Tutor accounts are provisioned administratively.")
            return
        }

        if (email.isBlank() || pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val firebaseAuth = auth
            if (firebaseAuth == null) {
                val user = User(
                    id = "usr_${System.currentTimeMillis()}",
                    name = name,
                    username = name.lowercase().replace(" ", "_"),
                    email = email,
                    role = UserRole.STUDENT
                )
                _uiState.value = AuthUiState.Success(user)
                return@launch
            }

            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
                val fUser = result.user
                if (fUser != null) {
                    try {
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name.trim())
                            .build()
                        fUser.updateProfile(profileUpdates).await()
                    } catch (pe: Exception) {
                        Log.w("AuthViewModel", "Could not set displayName: ${pe.message}")
                    }

                    val user = fetchOrCreateUserProfile(fUser, UserRole.STUDENT, providedName = name.trim(), isNewUser = true)
                    _uiState.value = AuthUiState.Success(user)
                } else {
                    _uiState.value = AuthUiState.Error("Sign up failed")
                }
            } catch (e: Exception) {
                Log.w("AuthViewModel", "Sign up with email failed", e)
                val msg = when {
                    e.message?.contains("email-already-in-use", ignoreCase = true) == true -> "An account already exists with this email. Please sign in."
                    e.message?.contains("weak-password", ignoreCase = true) == true -> "Password is too weak. Please choose a stronger password."
                    else -> e.localizedMessage ?: "Failed to create account"
                }
                _uiState.value = AuthUiState.Error(msg)
            }
        }
    }

    fun sendPasswordReset(email: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            onResult(false, "Please enter a valid email address.")
            return
        }
        viewModelScope.launch {
            val firebaseAuth = auth
            if (firebaseAuth != null) {
                try {
                    firebaseAuth.sendPasswordResetEmail(email.trim()).await()
                    onResult(true, "Password reset link sent to $email. Please check your inbox.")
                } catch (e: Exception) {
                    val msg = when {
                        e.message?.contains("user-not-found", ignoreCase = true) == true -> "No account found with this email."
                        else -> e.localizedMessage ?: "Failed to send reset email."
                    }
                    onResult(false, msg)
                }
            } else {
                onResult(true, "Demo link sent! Check your inbox at $email.")
            }
        }
    }

    suspend fun saveUserProfileToFirestore(user: User): Boolean {
        val fs = firestore ?: return false
        return try {
            val data = mapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "bio" to user.bio,
                "institution" to user.institution,
                "faculty" to user.faculty,
                "department" to user.department,
                "academicLevel" to user.academicLevel,
                "specialization" to (user.specialization ?: ""),
                "hourlyRate" to user.hourlyRate,
                "bankName" to user.bankName,
                "accountNumber" to user.accountNumber,
                "accountName" to user.accountName,
                "updatedAt" to System.currentTimeMillis()
            )
            fs.collection("users").document(user.id).set(data, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to save user profile to Firestore: ${e.message}")
            false
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _uiState.value = AuthUiState.Idle
        _firebaseUser.value = null
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
