package com.esma.bunble.presentation.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.esma.bunble.presentation.base.components.home.ActionButtonsRow
import com.esma.bunble.presentation.base.components.home.AppBottomBar
import com.esma.bunble.presentation.base.components.home.CategoryCard
import com.esma.bunble.presentation.base.components.home.HomeTopBar
import com.esma.bunble.presentation.base.components.home.StatsCard
import com.esma.bunble.presentation.theme.ui.GrayText
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {

    val state = viewModel.state.value
    val context = LocalContext.current

    LaunchedEffect(key1 = state.error) {
        state.error?.let { errorId ->
            // Composable olmayan bir yerden string almak için context.getString() kullanırız.
            val errorMessage = context.getString(errorId)
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = SurfaceLight,
        topBar = { HomeTopBar() },
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),

                contentPadding = PaddingValues(
                    horizontal = 16.dp)
            )  {
                item {
                    Text(text = "Hello, ${state.userName}!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Let's learn something new today!", color = GrayText)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    StatsCard()
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    ActionButtonsRow(navController = navController)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text(text = "Learning Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                }


                items(state.categories.chunked(2)) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (category in rowItems) {
                            Box(modifier = Modifier.weight(1f)) {
                                CategoryCard(
                                    title = category.name,
                                    imageUrl = category.imageUrl ,
                                    onClick = {
                                        navController.navigate("category_detail_screen/${category.id}")
                                    }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}



@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}