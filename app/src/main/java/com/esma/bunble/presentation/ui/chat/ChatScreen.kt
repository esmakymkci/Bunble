package com.esma.bunble.presentation.ui.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.viewmodel.chat.Author
import com.esma.bunble.presentation.viewmodel.chat.ChatMessage
import com.esma.bunble.presentation.viewmodel.chat.ChatViewModel
import kotlinx.coroutines.launch
import com.esma.bunble.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(
        navController.getBackStackEntry("main_graph")
    )
) {
    val chatState = viewModel.chatState.value
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current


    // Yeni bir mesaj geldiğinde listenin en altına kaydır
    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chat with AI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home_screen") { // 'home_screen' rotasının doğru olduğundan emin ol
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("conversation_screen") {
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Switch to Conversation Mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }

    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .imePadding()
        ) {
            // Mesajların listelendiği alan
            LazyColumn(
                state = listState,
                reverseLayout = true,
                // İÇERİĞİN EN ALTA YAPIŞMASINI SAĞLAYAN SİHİRLİ SATIR
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                if (chatState.isLoading) {
                    item {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
                items(chatState.messages.reversed()) { message ->

                    // Mesaj Balonunu ve Yazar Adını gruplayan bir Column
                    Column {
                        Text(
                            text = if (message.author == Author.USER) "You" else "Bunble AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                // AI için başlangıçtan, Kullanıcı için bitişten boşluk bırakarak hizala
                                .padding(
                                    start = if (message.author != Author.USER) 56.dp else 0.dp, // (Resim + boşluk) kadar
                                    end = if (message.author == Author.USER) 56.dp else 0.dp,   // (Resim + boşluk) kadar
                                    top = 4.dp // Balonla arasında boşluk
                                ),
                            // Metni kendi içinde de sağa veya sola yasla
                            textAlign = if (message.author == Author.USER) TextAlign.End else TextAlign.Start
                        )
                        MessageBubble(message = message)

                    }
                }
            }

            // Mesaj giriş alanı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatState.textInput,
                    onValueChange = { viewModel.onTextInputChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendMessage()
                        keyboardController?.hide()
                    },
                    enabled = chatState.textInput.isNotBlank() && !chatState.isLoading,
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandYellow, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isFromUser = message.author == Author.USER
    val backgroundColor = if (isFromUser) BrandYellow else Color(0xFFFFF9E0)
    val textColor = Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom // Resmi balonun alt kısmına hizala
    )
    {
        if (!isFromUser) {
            Image(
                painter = painterResource(id = R.drawable.ai_avatar),
                contentDescription = "AI Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = message.text,
            color = textColor,
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .padding(16.dp)
        )

        if (isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = R.drawable.user_avatar),
                contentDescription = "User Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }


    }

}
