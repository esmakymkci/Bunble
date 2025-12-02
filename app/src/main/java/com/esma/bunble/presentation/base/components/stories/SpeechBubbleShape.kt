package com.esma.bunble.presentation.base.components.stories

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

//  altı ok şeklinde olan bir konuşma balonu
class SpeechBubbleShape(
    private val cornerRadius: Dp,
    private val tipSize: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density // Ekran yoğunluğu bilgisi
    ): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val tipSizePx = with(density) { tipSize.toPx() }

        val path = Path().apply {
            // Balonun ana dikdörtgenini çiz (ucu hariç)
            val rect = Rect(0f, 0f, size.width, size.height - tipSizePx)
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    rect = rect,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadiusPx)
                )
            )

            // Üçgen ucu çiz
            val tipX = size.width / 2f
            moveTo(tipX - tipSizePx, size.height - tipSizePx)
            lineTo(tipX, size.height)
            lineTo(tipX + tipSizePx, size.height - tipSizePx)
            close()
        }
        return Outline.Generic(path)
    }
}
