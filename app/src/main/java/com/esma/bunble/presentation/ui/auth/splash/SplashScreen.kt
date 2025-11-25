package com.esma.bunble.presentation.ui.auth.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.esma.bunble.R
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
){

    val startDestination by viewModel.startDestination.collectAsState()

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("sari_kenar.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1, // Animasyon 1 kere oynasın
        speed = 1f)

    LaunchedEffect(progress, startDestination) {
        if (progress == 1f) {
            startDestination?.let { destination ->
                val route = when (destination) {
                    is StartDestination.LanguageSelection -> "language_selection_screen"
                    is StartDestination.Authentication -> "authentication_screen"
                    is StartDestination.Home -> "home_screen" // <-- YENİ ROTAYI EKLE
                }
                navController.navigate(route) {
                    popUpTo("splash_screen") { inclusive = true }
                }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFE3F2FD).copy(alpha = 0.8f),
                    Color(0xFFF8BBD0).copy(alpha = 0.6f)
                )
            )
        ),
        contentAlignment = Alignment.Center){
        if(composition != null){
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Image(
            painter = painterResource(id = R.drawable.logo_bunble),
            contentDescription = "Bunble Logo",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(40.dp))
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SplashScreenPreview(){
    SplashScreen(navController = rememberNavController())
}