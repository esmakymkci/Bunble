package com.esma.bunble.presentation.ui.auth.signin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// SignIn UI'ının durumunu temsil edecek bir data class
data class SignInState(
    val isLoading: Boolean = false,
    val signInSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _signInState = mutableStateOf(SignInState())
    val signInState: State<SignInState> = _signInState

    fun signInUser(email: String, pass: String) {
        // Basit validasyonlar
        if (email.isBlank() || pass.isBlank()) {
            _signInState.value = SignInState(error = "Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _signInState.value = SignInState(isLoading = true)
            try {
                // 1. Firebase Authentication ile kullanıcı girişi yapmayı dene
                firebaseAuth.signInWithEmailAndPassword(email, pass).await()

                // 2. Giriş başarılıysa, UI'a bildir
                _signInState.value = SignInState(signInSuccess = true)

            } catch (e: Exception) {
                // 3. Hata durumunda (yanlış şifre vb.), UI'a bildir
                _signInState.value = SignInState(error = e.localizedMessage ?: "Invalid email or password.")
            }
        }
    }

    // Hata mesajı gösterildikten sonra state'i sıfırlamak için
    fun errorShown() {
        _signInState.value = _signInState.value.copy(error = null)
    }
}
