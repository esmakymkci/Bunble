package com.esma.bunble.presentation.base.components.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.esma.bunble.presentation.theme.ui.AuthFieldBackground

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "$label Icon"
            )
        },
        trailingIcon = trailingIcon,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AuthFieldBackground,
            unfocusedContainerColor = AuthFieldBackground,
            disabledContainerColor = AuthFieldBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}



/*


leadingIcon: ImageVector
Sol tarafta gözükecek ikon (örn: Icons.Default.Email veya Icons.Default.Lock).

keyboardActions: KeyboardActions = KeyboardActions.Default
Klavyedeki IME action (Enter, Done, Next vs.) tıklandığında yapılacaklar

visualTransformation: VisualTransformation = VisualTransformation.None
Şifre gibi özel karakterleri gizlemek için kullanılır.

trailingIcon: @Composable (() -> Unit)? = null
Sağ tarafta yer alan ikonlar Örn şifre göster/gizle butonu







 */
