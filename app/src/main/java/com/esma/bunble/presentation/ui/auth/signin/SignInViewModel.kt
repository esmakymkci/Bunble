package com.esma.bunble.presentation.ui.auth.signin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _signInState = mutableStateOf(SignInState())
    val signInState: State<SignInState> = _signInState

    fun signInUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _signInState.value = SignInState(error = R.string.error_email_password_empty)
            return
        }

        viewModelScope.launch {
            _signInState.value = SignInState(isLoading = true)
            try {
                firebaseAuth.signInWithEmailAndPassword(email, pass).await()

                _signInState.value = SignInState(signInSuccess = true)

            } catch (e: Exception) {
                _signInState.value = SignInState(error = R.string.error_invalid_credentials)
            }
        }
    }

    fun errorShown() {
        _signInState.value = _signInState.value.copy(error = null)
    }
}
