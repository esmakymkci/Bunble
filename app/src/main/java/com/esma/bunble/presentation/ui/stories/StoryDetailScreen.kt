package com.esma.bunble.presentation.ui.stories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.esma.bunble.presentation.base.components.stories.TranslationPopup
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.story.StoryDetailViewModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions


@Composable
fun TappableStoryText(
    storyText: String,
    selectedWord: String,
    onWordClicked: (word: String, position: Offset) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(storyText)
        // Regex ile metni kelimelere ve aralarındaki boşluklara/noktalama işaretlerine ayır
        // Bu, kelimelerin pozisyonlarını doğru hesaplamamızı sağlar.
        // Regex ile metni kelimelere ve aralarındaki boşluklara/noktalama işaretlerine ayır
        val wordRegex = "\\w+".toRegex()
        wordRegex.findAll(storyText).forEach { matchResult ->
            val word = matchResult.value
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1

            // Bu kelime, ViewModel'den gelen seçili kelime ise sarı yap
            if (word.equals(selectedWord, ignoreCase = true)) {
                addStyle(
                    style = SpanStyle(
                        color = BrandYellow,
                        fontWeight = FontWeight.Bold
                    ),
                    start = startIndex,
                    end = endIndex
                )
            }
            addStringAnnotation(
                tag = "word_tap",
                annotation = word,
                start = startIndex,
                end = endIndex
            )
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var rootCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    ClickableText(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            rootCoordinates = coordinates
        },
        text = annotatedString,
        style = LocalTextStyle.current.copy(fontSize = 16.sp, lineHeight = 28.sp, color = LocalContentColor.current),
        onTextLayout = { layoutResult ->
            textLayoutResult = layoutResult
        },
        onClick = { offset ->
            textLayoutResult?.let { layout ->
                rootCoordinates?.let { root ->
                    annotatedString.getStringAnnotations("word_tap", offset, offset).firstOrNull()?.let { annotation ->
                        // Kelimenin, içinde bulunduğu Text bloğuna göre YEREL pozisyonunu bulduk.
                        val boundingBox = layout.getBoundingBox(annotation.start)

                        // Text bloğunun, EKRANA göre EVRENSEL pozisyonunu bulduk.
                        // Bu ikisini birleştirerek kelimenin EKRANDAKİ GERÇEK pozisyonunu hesapladık.
                        val globalPosition = root.localToWindow(boundingBox.topLeft)

                        // Bu doğru pozisyonu ViewModel'e gönderdik.
                        onWordClicked(annotation.item, globalPosition)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    navController: NavController,
    viewModel: StoryDetailViewModel = hiltViewModel()
) {

    val state = viewModel.state.value
    //val sheetState = rememberModalBottomSheetState()


    Scaffold(
        containerColor = SurfaceLight,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.storyDetail?.title ?: "Loading...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.error}")
                }
            }
            state.storyDetail != null -> {
                val detail = state.storyDetail
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(16.dp))

                    AsyncImage(
                        model = detail.imageUrl,
                        contentDescription = detail.title,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            // En-boy oranını daha dikdörtgen yapmak için (4:3)
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    TappableStoryText(
                        storyText = state.storyDetail.content,
                        selectedWord = state.selectedWord,
                        onWordClicked = { word, position ->
                            viewModel.onWordClicked(word, position)
                        }
                    )
                }
            }
        }
    }

    if (state.isSheetVisible) {
        TranslationPopup(
            popupPosition = state.wordPosition,
            selectedWord = state.selectedWord,
            translatedWord = state.translatedWord,
            onDismissRequest = { viewModel.onSheetDismiss() }
        )
    }
}


/*

1.buildAnnotatedString: Bu, metnin farklı kısımlarına farklı stiller (renk, kalınlık) veya "notlar" (StringAnnotation) eklememizi sağlar.
2.Regex (Regular Expression): "\\w+" ifadesi ile metindeki sadece kelimeleri buluruz.
3.addStyle: ViewModel'den gelen selectedWord ile eşleşen kelimeyi BrandYellow (sarı) renkte ve kalın (Bold) yapar.
4.addStringAnnotation: Görünmez bir şekilde, her kelimenin üzerine "word_tap" etiketli bir not ekleriz. Bu not, kelimenin kendisini (annotation = word) içerir. ClickableText'in onClick'i bu notları okuyabilir.

 */