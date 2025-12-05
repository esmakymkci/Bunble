package com.esma.bunble.presentation.ui.my_lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.presentation.base.components.home.AppBottomBar
import com.esma.bunble.presentation.base.components.my_lists.WordListCard
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.my_lists.WordListsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListsScreen(
    navController: NavController,
    viewModel: WordListsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLists = remember(searchQuery, uiState.lists) {
        if (searchQuery.isBlank()) {
            uiState.lists
        } else {
            uiState.lists.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            WordListsTopBar(
                onToggleSearch = { isSearchActive = !isSearchActive }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_list_screen") },
                containerColor = BrandYellow,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create new list")
            }
        },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = SurfaceLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // `AnimatedVisibility` sayesinde yumuşak bir şekilde görünüp kaybolacak.
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search in your lists...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close search")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandYellow,
                        focusedLabelColor = BrandYellow,
                        cursorColor = BrandYellow,
                        unfocusedBorderColor = Color.LightGray,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

            }
            Box(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Text(
                        text = "An error occurred: ${uiState.error}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredLists, key = { it.id }) { list ->
                            WordListCard(
                                list = list,
                                onClick = {
                                    navController.navigate("list_detail_screen/${list.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListsTopBar(
    onToggleSearch: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text("Word Dictionary", fontWeight = FontWeight.Bold)
        },
        actions = {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search lists"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
    )
}



