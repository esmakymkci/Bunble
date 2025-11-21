package com.esma.bunble.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.esma.bunble.presentation.ui.auth.signin.SignInScreen
import com.esma.bunble.presentation.ui.auth.signup.SignUpScreen
import com.esma.bunble.presentation.ui.auth.splash.SplashScreen
import com.esma.bunble.presentation.ui.category_detail.CategoryDetailScreen
import com.esma.bunble.presentation.ui.home.HomeScreen
import com.esma.bunble.presentation.ui.learn.LearningScreen

@Composable
fun Navigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen"){
        composable("splash_screen"){
            SplashScreen(navController = navController)
        }

        composable("signup_screen") {
            SignUpScreen(navController = navController)
        }


        composable("signin_screen") {
            SignInScreen(navController = navController)
        }

        composable("home_screen"){
            HomeScreen(navController = navController)
        }

        composable(
            route = "category_detail_screen/{categoryTitle}/{categoryImageRes}",
            arguments = listOf(
                navArgument("categoryTitle") { type = NavType.StringType },
                navArgument("categoryImageRes") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("categoryTitle")
            val imageRes = backStackEntry.arguments?.getInt("categoryImageRes")
            CategoryDetailScreen(
                // categoryId parametresi kaldırıldı.
                categoryTitle = title,
                categoryImageRes = imageRes,
                navController = navController
            )
        }

        // --- ÖĞRENME EKRANI ROTASI ESKİ HALİNE DÖNDÜRÜLDÜ ---
        composable(
            route = "learning_screen/{categoryTitle}",
            arguments = listOf(navArgument("categoryTitle") { type = NavType.StringType })
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("categoryTitle")
            // categoryId parametresi kaldırıldı.
            LearningScreen(navController = navController, categoryTitle = title)
        }


        composable("stories_screen"){
            //StoriesScreen(navController = navController)
        }

        composable("lists_screen"){
            //ListsScreen(navController = navController)
        }

        composable("quiz_screen"){
            //QuizScreen(navController = navController)
        }

        composable("chat_screen"){
            //ChatScreen(navController = navController)
        }

        composable("profile_screen"){
            //ProfileScreen(navController = navController)
        }


    }
}