package com.example.xamu_wil_project.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xamu_wil_project.data.AppUser
import com.example.xamu_wil_project.data.repository.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val user: AppUser? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        _uiState.value = _uiState.value.copy(
            isAuthenticated = currentUser != null
        )

        if (currentUser != null) {
            loadUserProfile()
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()

                val user = result.user
                if (user != null) {
                    // Bootstrap user profile in Firebase
                    val appUser = AppUser(
                        email = user.email,
                        firstname = user.displayName?.split(" ")?.firstOrNull() ?: "",
                        lastname = user.displayName?.split(" ")?.lastOrNull() ?: ""
                    )

                    repository.createOrUpdateUser(user.uid, appUser)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = appUser
                    )
                } else {
                    throw Exception("Authentication failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = e.message ?: "Authentication failed"
                )
            }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val result = auth.signInWithEmailAndPassword(email, password).await()

                val user = result.user
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    loadUserProfile()
                } else {
                    throw Exception("Authentication failed")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = e.message ?: "Login failed. Please check your credentials."
                )
            }
        }
    }

    fun registerWithEmailAndPassword(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Validate inputs
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    throw Exception("Please enter a valid email address")
                }

                if (password.length < 6) {
                    throw Exception("Password must be at least 6 characters")
                }

                // Try to create the user account
                val result = auth.createUserWithEmailAndPassword(email, password).await()

                val user = result.user
                if (user != null) {
                    // Create user profile in Firebase
                    val appUser = AppUser(
                        email = email,
                        firstname = firstName,
                        lastname = lastName
                    )

                    // Save user profile
                    try {
                        repository.createOrUpdateUser(user.uid, appUser)
                    } catch (e: Exception) {
                        // Profile save failed but user is created, log them in anyway
                        android.util.Log.w("AuthViewModel", "Failed to save user profile: ${e.message}")
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = appUser
                    )
                } else {
                    throw Exception("Registration failed")
                }
            } catch (e: com.google.firebase.FirebaseNetworkException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = "Network error. Please check your internet connection and try again."
                )
            } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = "This email is already registered. Please try logging in instead."
                )
            } catch (e: com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = "Password is too weak. Please use a stronger password."
                )
            } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = "Invalid email format. Please check your email address."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = e.message ?: "Registration failed. Please check your internet connection and try again."
                )
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState(isAuthenticated = false)
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            repository.getUserProfile(uid).collect { user ->
                _uiState.value = _uiState.value.copy(user = user)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
