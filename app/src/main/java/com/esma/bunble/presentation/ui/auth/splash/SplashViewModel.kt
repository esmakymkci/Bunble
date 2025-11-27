package com.esma.bunble.presentation.ui.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esma.bunble.data.local.UserPreferencesRepository
import com.google.firebase.auth.FirebaseAuth // <-- 1. Firebase Auth'u import et
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StartDestination {
    object LanguageSelection : StartDestination()
    object Authentication : StartDestination()
    object Home : StartDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPrefsRepo: UserPreferencesRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _startDestination = MutableStateFlow<StartDestination?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val isFirstLaunch = userPrefsRepo.isFirstLaunch.first()

            if (isFirstLaunch) {
                _startDestination.value = StartDestination.LanguageSelection
            } else {
                if (firebaseAuth.currentUser != null) {
                    _startDestination.value = StartDestination.Home
                } else {
                    _startDestination.value = StartDestination.Authentication
                }
            }
        }
    }
}
