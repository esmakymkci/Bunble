package com.esma.bunble.presentation.ui.auth.signup

import androidx.annotation.StringRes
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.R
import com.esma.bunble.data.local.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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
            _signUpState.value = SignUpState(error = R.string.error_all_fields_required)
            return
        }
        if (pass != confirmPass) {
            _signUpState.value = SignUpState(error = R.string.error_passwords_do_not_match)
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
                    _signUpState.value = SignUpState(error = R.string.error_user_creation_failed)
                }
            } catch (e: Exception) {
                _signUpState.value = SignUpState(error =  R.string.error_unknown)
            }
        }
    }

    fun errorShown() {
        _signUpState.value = _signUpState.value.copy(error = null)
    }
}
