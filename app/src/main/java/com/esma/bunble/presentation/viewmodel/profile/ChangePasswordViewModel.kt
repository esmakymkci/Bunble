package com.esma.bunble.presentation.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordState(
    val isLoading: Boolean = false
)

sealed class ChangePasswordEvent {
    data class ShowMessage(val message: String) : ChangePasswordEvent()
    data object NavigateBack : ChangePasswordEvent()
}


@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ChangePasswordState())
    val state = _state.asStateFlow()

    private val _event = Channel<ChangePasswordEvent>()
    val event = _event.receiveAsFlow()

    fun changePassword(currentPass: String, newPass: String, confirmPass: String) {
        if (currentPass.isBlank() || newPass.isBlank() || confirmPass.isBlank()) {
            sendEvent(ChangePasswordEvent.ShowMessage("All fields are required."))
            return
        }
        if (newPass != confirmPass) {
            sendEvent(ChangePasswordEvent.ShowMessage("New passwords do not match."))
            return
        }
        if (newPass.length < 6) {
            sendEvent(ChangePasswordEvent.ShowMessage("New password must be at least 6 characters long."))
            return
        }

        _state.update { it.copy(isLoading = true) }

        val user = auth.currentUser
        if (user == null || user.email == null) {
            sendEvent(ChangePasswordEvent.ShowMessage("User not found. Please sign in again."))
            _state.update { it.copy(isLoading = false) }
            return
        }

        // Kullanıcıyı mevcut şifresiyle yeniden doğrula
        val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Doğrulama başarılıysa yeni şifreyi ayarla
                    user.updatePassword(newPass)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                sendEvent(ChangePasswordEvent.ShowMessage("Password updated successfully!"))
                                sendEvent(ChangePasswordEvent.NavigateBack) // Geri git
                            } else {
                                sendEvent(ChangePasswordEvent.ShowMessage(updateTask.exception?.message ?: "An error occurred."))
                            }
                            _state.update { it.copy(isLoading = false) }
                        }
                } else {
                    // Yeniden doğrulama başarısız oldu
                    val message = if (reauthTask.exception is FirebaseAuthInvalidCredentialsException) {
                        "Invalid current password."
                    } else {
                        reauthTask.exception?.message ?: "Authentication failed."
                    }
                    sendEvent(ChangePasswordEvent.ShowMessage(message))
                    _state.update { it.copy(isLoading = false) }
                }
            }
    }

    private fun sendEvent(event: ChangePasswordEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }
}

