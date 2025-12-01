package com.esma.bunble.presentation.ui.auth.signin

import androidx.annotation.StringRes

data class SignInState(
    val isLoading: Boolean = false,
    val signInSuccess: Boolean = false,
    @StringRes val error: Int? = null
)
