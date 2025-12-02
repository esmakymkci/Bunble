package com.esma.bunble.presentation.base.components.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.roundToInt

@Composable
fun TranslationPopup(
    popupPosition: Offset,
    selectedWord: String,
    translatedWord: String,
    onDismissRequest: () -> Unit
) {
    // Özel Şeklimizi Oluşturma
    val speechBubbleShape = SpeechBubbleShape(cornerRadius = 16.dp, tipSize = 12.dp)
    // Ekran Yoğunluğunu Alma. Dp birimini piksele çevirmek için gerekli olan yardımcı.
    val density = LocalDensity.current

    // Pozisyon Hesaplayıcıyı (Beyni) Oluşturma
    val popupPositionProvider = remember(popupPosition) {
        TooltipPositionProvider(
            position = popupPosition,
            verticalOffset = with(density) { 16.dp.toPx() }.roundToInt()
        )
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = speechBubbleShape)
                .background(Color.White, shape = speechBubbleShape)
                .padding(horizontal = 24.dp)
                // Alt üçgen için altta daha fazla boşluk bırak
                .padding(top = 16.dp, bottom = 16.dp + 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedWord,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = translatedWord,
                color = Color.DarkGray,
                fontSize = 18.sp
            )
        }
    }
}

// --- POZİSYON HESAPLAMA SINIFI (BEYİN) ---
class TooltipPositionProvider(
    private val position: Offset,   // Tıklamanın yapıldığı X,Y koordinatı
    private val verticalOffset: Int   // Kelime ile baloncuk arasındaki dikey boşluk (piksel cinsinden)
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // Popup'ın X pozisyonunu, tıklanan noktanın ortasına gelecek şekilde ayarla
        val popupX = position.x - (popupContentSize.width / 2)

        // Popup'ın Y pozisyonunu, tıklanan noktanın üstüne gelecek şekilde ayarla
        val popupY = position.y - popupContentSize.height - verticalOffset

        return IntOffset(popupX.roundToInt(), popupY.roundToInt())
    }
}


/*

TranslationPopup: Tıklanan kelimeyi ve çevirisini, özel SpeechBubbleShape'i kullanarak şık bir "konuşma balonu" içinde gösterir.

TooltipPositionProvider: Bu konuşma balonunun, ekranda tam olarak nereye yerleşeceğini matematiksel olarak hesaplayan beyindir.


 */
