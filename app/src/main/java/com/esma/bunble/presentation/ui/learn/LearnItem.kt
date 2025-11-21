package com.esma.bunble.presentation.ui.learn

import androidx.annotation.DrawableRes

data class LearnItem(
    val text: String,
    val phonetic: String,
    val meaning: String,
    val translation: String,
    @DrawableRes val imageRes: Int
)
