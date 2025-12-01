package com.esma.bunble.presentation.ui.auth.signup

import androidx.annotation.StringRes

data class SignUpState(
    val isLoading: Boolean = false,
    val signUpSuccess: Boolean = false,
    @StringRes val error: Int? = null
)
