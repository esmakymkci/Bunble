package com.esma.bunble.presentation.theme.ui
import androidx.compose.ui.graphics.Color

// --- Ana Marka Renkleri ---
val BrandYellow = Color(0xFFFDD835) // Ana butonlar, vurgular, seçili öğeler
val BrandYellowDark = Color(0xFFD4A000) // Ses butonu ikonu gibi daha koyu tonlar
val BrandYellowLight = Color(0xFFFFC107) // Ses butonu arkaplanı gibi daha açık tonlar
val BrandYellowLighter = Color(0xFFFFFBEB) // İkincil butonlar, kart arkaplanları
val BrandYellowTransparent = BrandYellow.copy(alpha = 0.2f) // Seçili item arka planı için
val BrandBlack = Color.Black
val BrandWhite = Color.White

// --- Nötr Renkler ---
val GrayText = Color.Gray // Genel gri metinler, ikonlar
val BorderGray = Color.LightGray // Seçili olmayan kenarlıklar için
val SurfaceLight = Color(0xFFF7F7F7) // TopAppBar arkaplanı, genel açık arkaplanlar
val AuthFieldBackground = Color(0xFFFAF8F2) // TextField arkaplanları

// --- Durum Renkleri (Quiz için) ---
val CorrectGreen = Color(0xFF4CAF50)
val IncorrectRed = Color(0xFFF44336)
// Bu yarı saydam renkleri doğrudan kullanım anında .copy(alpha=) ile oluşturmak daha esnek olabilir,
// ama tutarlılık için burada da tutabiliriz.
val LightCorrectGreen = CorrectGreen.copy(alpha = 0.1f)
val LightIncorrectRed = IncorrectRed.copy(alpha = 0.1f)
val DarkPurple = Color(0xFF4A148C)

// --- Degrade Renkleri (Gradient) ---
// Bu renkleri doğrudan Brush içinde tanımlamak daha mantıklı olabilir,
// ama istersen buradan da çekebilirsin.
val GradientBlue = Color(0xFFE3F2FD)
val GradientPink = Color(0xFFF8BBD0)

// --- Material 3 Varsayılanları (Bunlar genellikle tema içindedir ama referans için kalabilir) ---
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
