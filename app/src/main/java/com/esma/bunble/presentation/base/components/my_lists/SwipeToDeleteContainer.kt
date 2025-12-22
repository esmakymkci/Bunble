package com.esma.bunble.presentation.base.components.my_lists

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.esma.bunble.presentation.theme.ui.BrandWhite
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
                    contentDescription = stringResource(id = R.string.swipe_to_delete_desc),
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