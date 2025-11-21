package com.esma.bunble.presentation.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.esma.bunble.R
import com.esma.bunble.presentation.base.components.home.ActionButtonsRow
import com.esma.bunble.presentation.base.components.home.AppBottomBar
import com.esma.bunble.presentation.base.components.home.CategoryCard
import com.esma.bunble.presentation.base.components.home.HomeTopBar
import com.esma.bunble.presentation.base.components.home.StatsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    Scaffold(
        containerColor = Color(0xFFF7F7F7),
        topBar = { HomeTopBar() },
        bottomBar = { AppBottomBar(navController = navController) }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

            contentPadding = PaddingValues(
                horizontal = 16.dp
            )
        )  {
            item {
                Text(text = "Hello, Sophie", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(text = "Let's learn something new today!", color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                StatsCard()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                ActionButtonsRow()
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(text = "Learning Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            val categories = listOf(
                "Food and Ordering" to R.drawable.food2,
                "Travel" to R.drawable.travel,
                "Greeting" to R.drawable.communication,
                "Work" to R.drawable.work,
                "Digital Life" to R.drawable.digital_life,
                "Daily Routines" to R.drawable.daily_routines,
                "Health" to R.drawable.health,
                "Family and Friend" to R.drawable.relationships,
                "Emotions and Feelings" to R.drawable.emotions_feelings,
                "Hobbies" to R.drawable.hobbies,
                "Home Furniture" to R.drawable.home_furniture,
                "School" to R.drawable.school,
                "Weather" to R.drawable.weather,
                "Shop Clothes" to R.drawable.shop_clothes

            )

            /*item{
                CategoriesGrid(categories = categories)
            }*/

            items(categories.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    for ((title, imageRes) in rowItems) {
                        Box(modifier = Modifier.weight(1f)) {
                            CategoryCard(title = title, imageRes = imageRes,
                                onClick = {
                                    navController.navigate("category_detail_screen/$title/$imageRes")
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



@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}