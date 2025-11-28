package com.esma.bunble.presentation.ui.auth.splash

sealed class StartDestination {
    object LanguageSelection : StartDestination()
    object Authentication : StartDestination()
    object Home : StartDestination()
}