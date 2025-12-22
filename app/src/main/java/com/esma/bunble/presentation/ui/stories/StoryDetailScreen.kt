package com.esma.bunble.presentation.ui.stories

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
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
import com.esma.bunble.presentation.base.extension.findSentenceForChar
import com.esma.bunble.presentation.theme.ui.BrandYellow
import com.esma.bunble.presentation.theme.ui.SurfaceLight
import com.esma.bunble.presentation.viewmodel.story.StoryDetailViewModel
import com.esma.bunble.R


@Composable
fun TappableStoryText(
    storyText: String,
    selectedWord: String,
    onWordClicked: (word: String, position: Offset) -> Unit,
    onSentenceSelected: (sentence: String, position: Offset) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(storyText)

        if (selectedWord.isNotBlank()) {
            // Seçili metin bir cümle mi yoksa tek kelime mi olduğuna karar ver.
            // Boşluk içeriyorsa veya sonunda noktalama işareti varsa, bunu bir cümle olarak kabul edebiliriz.
            val isSentence = selectedWord.contains(" ") || selectedWord.endsWith(".") || selectedWord.endsWith("!") || selectedWord.endsWith("?")

            if (isSentence) {
                // Eğer bir cümle ise, sadece ilk bulduğun eşleşmeyi renklendir.
                val startIndex = storyText.indexOf(selectedWord, ignoreCase = true)
                if (startIndex != -1) {
                    val endIndex = startIndex + selectedWord.length
                    addStyle(
                        style = SpanStyle(
                            color = BrandYellow,
                            fontWeight = FontWeight.Bold
                        ),
                        start = startIndex,
                        end = endIndex
                    )
                }
            } else {
                // Eğer tek bir kelime ise, metindeki TÜM eşleşmelerini bul ve renklendir.
                val wordRegex = Regex(Regex.escape(selectedWord), RegexOption.IGNORE_CASE)
                wordRegex.findAll(storyText).forEach { matchResult ->
                    addStyle(
                        style = SpanStyle(
                            color = BrandYellow,
                            fontWeight = FontWeight.Bold
                        ),
                        start = matchResult.range.first,
                        end = matchResult.range.last + 1
                    )
                }
            }
        }

        // kelime bazında tıklamayı algılamak için.
        val wordRegex = "\\w+".toRegex()
        wordRegex.findAll(storyText).forEach { matchResult ->
            val word = matchResult.value
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1
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

    Text(
        text = annotatedString,
        style = LocalTextStyle.current.copy(fontSize = 16.sp, lineHeight = 28.sp, color = LocalContentColor.current),
        onTextLayout = { layoutResult ->
            textLayoutResult = layoutResult
        },
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                rootCoordinates = coordinates
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        // --- KELİME ÇEVİRİSİ (TIKLAMA)  ---
                        textLayoutResult?.let { layout ->
                            rootCoordinates?.let { root ->
                                // Tıklanan X,Y pozisyonunu (Offset) doğru karakter indeksine çevir.
                                val clickedCharIndex = layout.getOffsetForPosition(offset)

                                // Bu indeksi kullanarak doğru kelimeyi (annotation) bul.
                                annotatedString.getStringAnnotations(
                                    "word_tap",
                                    clickedCharIndex,
                                    clickedCharIndex
                                ).firstOrNull()?.let { annotation ->
                                    // Kelimenin pozisyonunu hesapla ve ViewModel'e gönder.
                                    val boundingBox = layout.getBoundingBox(annotation.start)
                                    val globalPosition = root.localToWindow(boundingBox.topLeft)
                                    onWordClicked(annotation.item, globalPosition)
                                }
                            }
                        }
                    },
                    onLongPress = { offset ->
                        // --- CÜMLE ÇEVİRİSİ (BASILI TUTMA) ---
                        textLayoutResult?.let { layout ->
                            rootCoordinates?.let { root ->
                                val pressedCharIndex = layout.getOffsetForPosition(offset)
                                // Basılan karakterin içinde bulunduğu cümleyi bul
                                val sentence = storyText.findSentenceForChar(pressedCharIndex)
                                if (sentence.isNotBlank()) {
                                    // Pozisyon olarak tıklanan yerin pozisyonunu gönderelim
                                    val globalPosition = root.localToWindow(offset)
                                    // ViewModel'e bulunan cümleyi ve pozisyonu bildir
                                    onSentenceSelected(sentence, globalPosition)
                                }
                            }
                        }
                    }
                )
            }
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    navController: NavController,
    viewModel: StoryDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.refreshStoryDetail()
    }

    val state = viewModel.state.value

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = SurfaceLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.storyDetail != null) {
                                if (state.storyDetail.isUserStory) stringResource(id = R.string.story_detail_title_user)
                                else stringResource(id = R.string.story_detail_title_public)                            } else {
                                stringResource(id = R.string.story_detail_title_loading)

                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.story_detail_back_button_desc))
                    }
                },
                actions = {
                    // Eğer hikaye detayı yüklendiyse VE bu bir kullanıcı hikayesi ise...
                    if (state.storyDetail?.isUserStory == true) {
                        TextButton(onClick = {
                            val currentStoryId = state.storyDetail.id
                            navController.navigate("edit_story_screen/$currentStoryId")
                        }) {
                            Text(
                                stringResource(id = R.string.story_detail_edit_button),
                                color = BrandYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
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
                    Text(text = stringResource(id = R.string.story_detail_error_message, state.error!!))
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

                    val titleText = @Composable {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Hikaye türüne göre yerleşimi değiştiriyoruz
                    if (detail.isUserStory) {
                        // KULLANICI HİKAYESİ (My Story Details)
                        titleText()
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        // HALKA AÇIK HİKAYE (Story Details)
                        if (!detail.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = detail.imageUrl,
                                contentDescription = stringResource(id = R.string.story_detail_image_desc, detail.title),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .aspectRatio(4f / 3f)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        titleText()
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    TappableStoryText(
                        storyText = state.storyDetail.content,
                        selectedWord = state.selectedWord,
                        onWordClicked = { word, position ->
                            viewModel.onWordClicked(word, position)
                        },
                        onSentenceSelected = { sentence, position ->
                            viewModel.onSentenceSelected(sentence, position)
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
4.addStringAnnotation: Görünmez bir şekilde, her kelimenin üzerine "word_tap" etiketli bir not ekleriz. Bu not, kelimenin kendisini (annotation = word) içerir.
ClickableText'in onClick'i bu notları okuyabilir.



onTextLayout: Compose, metni çizdiğinde bize TextLayoutResult bilgisini verir, biz de bunu değişkenimize atarız.
.onGloballyPositioned: Text bileşeni ekrana yerleştirildiğinde bize ekran koordinatlarını (LayoutCoordinates) verir, biz de bunu değişkenimize atarız.
Bu, metnin ekran üzerindeki konumunu takip eder. rootCoordinates'u günceller.


detectTapGestures: Bu, tıklama ve uzun basma olaylarını yakalar.


.pointerInput ve detectTapGestures: Burası tüm dokunma sihrinin gerçekleştiği yerdir.
Bu değiştirici, basit bir tıklamayı (onTap) ve uzun basmayı (onLongPress) ayırt etmemizi sağlar.


 */