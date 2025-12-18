package com.esma.bunble.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.esma.bunble.presentation.ui.my_lists.CreateListScreen
import com.esma.bunble.presentation.ui.auth.signin.SignInScreen
import com.esma.bunble.presentation.ui.auth.signup.SignUpScreen
import com.esma.bunble.presentation.ui.auth.splash.SplashScreen
import com.esma.bunble.presentation.ui.category_detail.CategoryDetailScreen
import com.esma.bunble.presentation.ui.chat.ChatScreen
import com.esma.bunble.presentation.ui.home.HomeScreen
import com.esma.bunble.presentation.ui.language_selection.LanguageSelectionScreen
import com.esma.bunble.presentation.ui.learn.LearningScreen
import com.esma.bunble.presentation.ui.my_lists.ListDetailScreen
import com.esma.bunble.presentation.ui.my_lists.WordListsScreen
import com.esma.bunble.presentation.ui.stories.AddStoryScreen
import com.esma.bunble.presentation.ui.stories.EditStoryScreen
import com.esma.bunble.presentation.ui.stories.StoriesScreen
import com.esma.bunble.presentation.ui.stories.StoryDetailScreen
import androidx.navigation.navigation
import com.esma.bunble.presentation.ui.chat.ConversationScreen
import com.esma.bunble.presentation.ui.profile.ChangePasswordScreen
import com.esma.bunble.presentation.ui.profile.ProfileScreen


@Composable
fun Navigation(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen"){
        composable("splash_screen"){
            SplashScreen(navController = navController)
        }

        composable("language_selection_screen") {
            LanguageSelectionScreen(navController = navController)
        }

        composable("signup_screen") {
            SignUpScreen(navController = navController)
        }


        composable("signin_screen") {
            SignInScreen(navController = navController)
        }

        navigation(startDestination = "home_screen", route = "main_graph"){

            composable("home_screen"){
                HomeScreen(navController = navController)
            }

            composable("stories_screen"){
                StoriesScreen(navController = navController)
            }

            composable("lists_screen"){
                WordListsScreen(navController = navController)
            }

            composable( "quiz_screen") {
                //QuizScreen(navController = navController)
            }

            composable("chat_screen"){
                ChatScreen(navController = navController)
            }


            composable("conversation_screen") {
                ConversationScreen(navController = navController)
            }

        }


        composable(
            route = "category_detail_screen/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            CategoryDetailScreen(navController = navController)
        }

        composable(
            route = "edit_story_screen/{storyId}",
            arguments = listOf(navArgument("storyId") { type = NavType.StringType })
        ) {
            EditStoryScreen(navController = navController)
        }

        composable(
            route = "learning_screen/{categoryId}?type={type}",
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            LearningScreen(navController = navController)
        }

        composable(route = "add_story_screen") {
            AddStoryScreen(navController = navController)
        }

        composable(
            route = "story_detail_screen/{storyId}?title={title}",
            arguments = listOf(
                navArgument("storyId") { type = NavType.StringType },
                navArgument("title") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            StoryDetailScreen(navController = navController)
        }

        composable("create_list_screen") {
            CreateListScreen(navController = navController)
        }

        composable(
            route = "list_detail_screen/{listId}",
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) {
            ListDetailScreen(navController = navController)
        }

        composable("profile_screen") {
            ProfileScreen(navController = navController)
        }

        composable("change_password_screen") {
            ChangePasswordScreen(navController = navController)
        }

    }
}