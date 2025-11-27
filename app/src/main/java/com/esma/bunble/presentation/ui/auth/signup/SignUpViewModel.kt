package com.esma.bunble.presentation.ui.auth.signup

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.local.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class SignUpState(
    val isLoading: Boolean = false,
    val signUpSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPrefsRepo: UserPreferencesRepository
) : ViewModel() {

    private val _signUpState = mutableStateOf(SignUpState())
    val signUpState: State<SignUpState> = _signUpState

    fun signUpUser(fullName: String, email: String, pass: String, confirmPass: String) {
        if (fullName.isBlank() || email.isBlank() || pass.isBlank()) {
            _signUpState.value = SignUpState(error = "All fields are required.")
            return
        }
        if (pass != confirmPass) {
            _signUpState.value = SignUpState(error = "Passwords do not match.")
            return
        }

        viewModelScope.launch {
            _signUpState.value = SignUpState(isLoading = true)
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
                val user = result.user

                if (user != null) {

                    val profileUpdates = userProfileChangeRequest {
                        displayName = fullName
                    }
                    user.updateProfile(profileUpdates).await()
                    val langPath = userPrefsRepo.languagePath.first()

                    val userProfile = hashMapOf(
                        "uid" to user.uid,
                        "displayName" to fullName,
                        "email" to email,
                        "languagePath" to langPath,
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users").document(user.uid)
                        .set(userProfile)
                        .await()

                    _signUpState.value = SignUpState(signUpSuccess = true)
                } else {
                    _signUpState.value = SignUpState(error = "User could not be created.")
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState(error = e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }

    fun errorShown() {
        _signUpState.value = _signUpState.value.copy(error = null)
    }
}
