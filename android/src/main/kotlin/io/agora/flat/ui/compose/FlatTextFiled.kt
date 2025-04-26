package io.agora.flat.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.agora.vuihoc.agora_native.R
import io.agora.flat.ui.theme.FlatTheme


@Composable
fun CloudDialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    placeholderValue: String,
) {
    var isFocused by remember { mutableStateOf(false) }
    val dividerColor = if (isFocused) MaterialTheme.colors.primary else FlatTheme.colors.divider

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier.onFocusChanged {
            isFocused = it.isFocused
        },
        textStyle = MaterialTheme.typography.h6.copy(
            color = FlatTheme.colors.textPrimary,
            textAlign = TextAlign.Start,
        ),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
    ) { innerTextField ->
        Box(Modifier, contentAlignment = Alignment.CenterStart) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(end = 48.dp), contentAlignment = Alignment.CenterStart
            ) {
                innerTextField()
            }
            FlatDivider(Modifier.align(Alignment.BottomCenter), color = dividerColor)
            if (value.isEmpty()) {
                FlatTextBodyOneSecondary(placeholderValue)
            }
            if (value.isNotBlank()) {
                IconButton(onClick = { onValueChange("") }, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Icon(
                        painterResource(id = R.drawable.ic_text_filed_clear),
                        "",
                        tint = FlatTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

