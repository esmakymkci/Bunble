package com.esma.bunble.presentation.ui.my_lists

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.presentation.base.components.home.AppBottomBar
import com.esma.bunble.presentation.base.components.my_lists.WordListCard
import com.esma.bunble.presentation.theme.ui.BrandBlack
import com.esma.bunble.presentation.theme.ui.BrandWhite
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.my_lists.WordListsViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.esma.bunble.R


//  Kaydırılabilir Kart ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val deleteButtonWidth = 80.dp // Sil butonunun genişliği
    val deleteButtonWidthPx = with(LocalDensity.current) { deleteButtonWidth.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            // Eğer yeterince sola kaydırıldıysa (butonun yarısından fazla)
                            if (offsetX.value < -deleteButtonWidthPx / 2) {
                                // butonu tam göster
                                offsetX.animateTo(-deleteButtonWidthPx)
                            } else {
                                // başlangıç pozisyonuna geri dön
                                offsetX.animateTo(0f)
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            val newOffset =
                                (offsetX.value + dragAmount).coerceIn(-deleteButtonWidthPx, 0f)
                            offsetX.snapTo(newOffset)
                        }
                    }
                )
            }
    ) {
        // Arka Plan: Sil Butonu
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Red, shape = RoundedCornerShape(24.dp))
                .align(Alignment.CenterEnd),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = onDelete, modifier = Modifier.padding(end= 20.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.word_lists_swipe_delete_desc),
                    tint = BrandWhite
                )
            }
        }

        // Ön Plan: Asıl Kart
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(Color.Transparent) // Arka planın görünmesi için
        ) {
            content()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordListsScreen(
    navController: NavController,
    viewModel: WordListsViewModel = hiltViewModel(
        navController.getBackStackEntry("main_graph")
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var listToDelete by remember { mutableStateOf<String?>(null) }
    val showDeleteDialog = listToDelete != null

    val filteredLists = remember(searchQuery, uiState.lists) {
        if (searchQuery.isBlank()) {
            uiState.lists
        } else {
            uiState.lists.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { WordListsTopBar(onToggleSearch = { isSearchActive = !isSearchActive }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_list_screen") },
                containerColor = BrandYellow,
                contentColor = BrandBlack,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.word_lists_fab_create_desc))
            }
        },
        bottomBar = { AppBottomBar(navController = navController) },
        containerColor = SurfaceLight
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            //.imePadding()
        ) {
            // Arama çubuğu
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(id = R.string.word_lists_search_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.word_lists_search_close_desc))
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

            // Onay diyaloğu...
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { listToDelete = null },
                    title = { Text(stringResource(id = R.string.dialog_delete_list_title)) },
                    text = { Text(stringResource(id = R.string.dialog_delete_list_text)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                listToDelete?.let { viewModel.deleteList(it) }
                                listToDelete = null
                            }
                        ) { Text("Delete", color = Color.Red) }
                    },
                    dismissButton = {
                        TextButton(onClick = { listToDelete = null }) { Text(stringResource(id = R.string.dialog_button_cancel)) }
                    }
                )
            }

            Box(
                modifier = Modifier.fillMaxSize().imePadding()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Text(
                        text = stringResource(id = R.string.word_lists_error_format, uiState.error!!),
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
                        items(items = filteredLists, key = { it.id }) { list ->
                            SwipeToDeleteContainer(
                                onDelete = { listToDelete = list.id }
                            ) {
                                WordListCard(
                                    list = list,
                                    onClick = { navController.navigate("list_detail_screen/${list.id}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListsTopBar(onToggleSearch: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(id = R.string.word_lists_title), fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = onToggleSearch) {
                Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(id = R.string.word_lists_search_button_desc))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
    )
}
